package com.rutvik.interview.aspire.miniaspireservice.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.rutvik.interview.aspire.miniaspireservice.api.contract.AuthenticateUserRequest;
import com.rutvik.interview.aspire.miniaspireservice.api.contract.AuthenticateUserResponse;
import com.rutvik.interview.aspire.miniaspireservice.api.enums.UserAuthStatus;
import com.rutvik.interview.aspire.miniaspireservice.api.exception.BadRequestException;
import com.rutvik.interview.aspire.miniaspireservice.api.exception.ServiceException;
import com.rutvik.interview.aspire.miniaspireservice.api.filter.RequestMetadata;
import com.rutvik.interview.aspire.miniaspireservice.api.utils.ThreadUtil;
import com.rutvik.interview.aspire.miniaspireservice.api.utils.ValidationUtil;
import com.rutvik.interview.aspire.miniaspireservice.repo.UserAuthRepository;
import com.rutvik.interview.aspire.miniaspireservice.repo.UserRepository;
import com.rutvik.interview.aspire.miniaspireservice.repo.entity.User;
import com.rutvik.interview.aspire.miniaspireservice.repo.entity.UserAuth;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;


@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final ValidationUtil validationUtil;
    private final UserAuthRepository userAuthRepository;
    private final UserRepository userRepository;
    private final ThreadUtil threadUtil;
    private final RequestMetadata requestMetadata;

    @Value("${authentication.otp.expiry.duration.in.minutes}")
    private int otpExpirationTimeInMinutes;

    @Value("${authentication.auth.token.expiry.duration.in.minutes}")
    private int authTokenExpirationTimeInMinutes;

    public AuthenticateUserResponse generateOtp(AuthenticateUserRequest authenticateUserRequest) {
        validateGenerateOtpRequest(authenticateUserRequest);
        int otp = generateOTP();
        User user = createOrGetUser(authenticateUserRequest.getPhoneNumber());
        UserAuth userAuth = createUserAuthEntity(authenticateUserRequest, otp, user);

        // return otp now. this can be sent via comms channel so that it can be verified correctly
        // for demo purpose only

        userAuthRepository.save(userAuth);
        // TODO: clean up old created entity when multiple generate otp calls are done
        authenticateUserRequest.setUserId(user.getId());
        threadUtil.getCompletableFuture(this::markMultipleGeneratedUserAuthsAsInvalid, authenticateUserRequest);
        return mapUserAuthToResponse(userAuth);
    }

    private User findUserByPhoneNumberWithValidation(String phoneNumber) {
        Optional<User> userIdOptional = userRepository.findByPhoneNumber(phoneNumber);
        if (userIdOptional.isEmpty()) {
            throw new BadRequestException("First generate otp to create a new user");
        }
        return userIdOptional.get();
    }
    public AuthenticateUserResponse login(AuthenticateUserRequest authenticateUserRequest) {
        validateGenerateOtpRequest(authenticateUserRequest);
        int otp = authenticateUserRequest.getOtp();
        String phoneNumber = authenticateUserRequest.getPhoneNumber();
        User user = findUserByPhoneNumberWithValidation(phoneNumber);
        List<UserAuth> savedUserAuths = userAuthRepository.findAllByUserIdAndExpiresAtGreaterThan(user.getId(), LocalDateTime.now());
        Optional<UserAuth> matchedUserAuthOptional = savedUserAuths.stream()
                .filter(userAuth -> userAuth.getStatus() == UserAuthStatus.CREATED)
                .max(Comparator.comparing(UserAuth::getExpiresAt));

        if (matchedUserAuthOptional.isEmpty()) {
            throw new BadRequestException("Please generate a new otp!");
        }

        UserAuth latestUserAuth = matchedUserAuthOptional.get();
        if (!latestUserAuth.getOtp().equals(otp)) {
            throw new BadRequestException("Invalid otp!");
        }
        latestUserAuth.setAuthToken(UUID.randomUUID().toString());
        latestUserAuth.setStatus(UserAuthStatus.VALID);
        latestUserAuth.setExpiresAt(LocalDateTime.now().plusMinutes(authTokenExpirationTimeInMinutes));
        UserAuth savedUserAuth = userAuthRepository.save(latestUserAuth);

        authenticateUserRequest.setUserId(user.getId());
        // TODO: clean up old created entity when multiple generate otp calls are done
        threadUtil.getCompletableFuture(this::markMultipleGeneratedUserAuthsAsInvalid, authenticateUserRequest);
        threadUtil.getCompletableFuture(this::markOlderValidTokensAsInvalid, authenticateUserRequest);
        return mapUserAuthToResponse(savedUserAuth);
    }

    public void logout() {
        String authToken = requestMetadata.authToken();

        if (authToken.isEmpty()) {
            throw new ServiceException("Something went wrong! Unable to fine authToken in request metadata");
        }
        Optional<UserAuth> userAuthOptional = userAuthRepository.findByAuthToken(authToken);
        if (userAuthOptional.isEmpty()) {
            throw new ServiceException("Something when wrong! Auth token not found in DB");
        }

        UserAuth userAuth = userAuthOptional.get();
        userAuth.setStatus(UserAuthStatus.INVALID);
        userAuthRepository.save(userAuth);
    }

    public Optional<UserAuth> validateToken(String authToken) {
        Optional<UserAuth> userAuthOptional = userAuthRepository.findByAuthToken(authToken);
        if (userAuthOptional.isPresent()) {
            UserAuth userAuth = userAuthOptional.get();
            boolean isTokenValid = userAuth.getStatus() == UserAuthStatus.VALID;
            boolean isTokenExpired = userAuth.getExpiresAt().isBefore(LocalDateTime.now());

            if (isTokenValid && !isTokenExpired) {
                return Optional.of(userAuth);
            }
            if (isTokenExpired) {
                userAuth.setStatus(UserAuthStatus.INVALID);
                userAuthRepository.save(userAuth);
            }
        }
        return Optional.empty();
    }

    private void validateGenerateOtpRequest(AuthenticateUserRequest authenticateUserRequest) {
        String phoneNumber = authenticateUserRequest.getPhoneNumber();
        if (StringUtils.isBlank(phoneNumber)) {
            throw new BadRequestException("No phone number provided!");
        }

        if (!validationUtil.isValidPhoneNumber(phoneNumber)) {
            throw new BadRequestException("Invalid phone number provided!");
        }
    }

    private UserAuth createUserAuthEntity(AuthenticateUserRequest request, int otp, User user
    ) {
        return UserAuth.builder()
                .status(UserAuthStatus.CREATED)
                .userId(user.getId())
                .otp(otp)
                .expiresAt(LocalDateTime.now().plusMinutes(otpExpirationTimeInMinutes))
                .build();
    }

    private User createOrGetUser(String phoneNumber) {
        Optional<User> user = userRepository.findByPhoneNumber(phoneNumber);
        return user.orElseGet(() -> userRepository.save(User.builder().id(UUID.randomUUID().toString()).phoneNumber(phoneNumber).build()));


    }

    private AuthenticateUserResponse mapUserAuthToResponse(UserAuth userAuth) {
        return AuthenticateUserResponse.builder()
                .authToken(userAuth.getAuthToken())
                .otp(userAuth.getOtp())
                .build();
    }

    private int generateOTP() {
        // Create a random number generator
        Random random = new Random();

        // Generate a random 6-digit OTP
        // Random number between 100,000 and 999,999
        return 100000 + random.nextInt(900000);
    }

    public Void markMultipleGeneratedUserAuthsAsInvalid(AuthenticateUserRequest request) {
        try {
            log.info(">>>>> trying to markMultipleGeneratedUserAuthsAsInvalid");
            List<UserAuth> userAuthList = userAuthRepository.findAllByUserIdAndStatus(request.getUserId(), UserAuthStatus.CREATED);

            if (userAuthList.size() > 1) {
                userAuthList.sort(Comparator.comparing(UserAuth::getExpiresAt));

                // Determine the index of the last element
                int lastIndex = userAuthList.size() - 1;

                // Stream through the list and mark as INVALID if not the last element
                userAuthList.forEach(auth -> {
                    if (userAuthList.indexOf(auth) != lastIndex) {
                        auth.setStatus(UserAuthStatus.INVALID);
                    }
                });

                userAuthRepository.saveAll(userAuthList);
                log.info(">> markMultipleGeneratedUserAuthsAsInvalid success");
            }
        } catch (Exception e) {
            log.error("Failed to markMultipleGeneratedUserAuthsAsInvalid, reason: {}", e.getMessage(), e);
        }
        return null;
    }

    public Void markOlderValidTokensAsInvalid(AuthenticateUserRequest request) {
        try {
            log.info(">>>>> trying to markOlderValidTokensAsInvalid");
            List<UserAuth> userAuthList = userAuthRepository.findAllByUserIdAndStatus(request.getUserId(), UserAuthStatus.VALID);

            if (userAuthList.size() > 1) {
                userAuthList.sort(Comparator.comparing(UserAuth::getExpiresAt));

                // Determine the index of the last element
                int lastIndex = userAuthList.size() - 1;

                // Stream through the list and mark as INVALID if not the last element
                userAuthList.forEach(auth -> {
                    if (userAuthList.indexOf(auth) != lastIndex) {
                        auth.setStatus(UserAuthStatus.INVALID);
                    }
                });

                userAuthRepository.saveAll(userAuthList);
            }
        } catch (Exception e) {
            log.error("Failed to markOlderValidTokensAsInvalid, reason: {}", e.getMessage(), e);
        }
        return null;
    }
}
