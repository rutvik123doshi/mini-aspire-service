package com.rutvik.interview.aspire.miniaspireservice.service;

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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AuthenticationServiceTest {

    private static final String VALID_PHONE_NUMBER = "1234567890";
    private static final String USER_ID = "user-id";

    @InjectMocks
    private AuthenticationService authenticationService;

    @Mock
    private ValidationUtil validationUtil;

    @Mock
    private UserAuthRepository userAuthRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ThreadUtil threadUtil;

    @Mock
    private RequestMetadata requestMetadata;

    private User getUser() {
        return User.builder()
                .phoneNumber(VALID_PHONE_NUMBER)
                .id("user-id")
                .build();
    }

    @Test
    public void testGenerateOtpSuccess() {
        // Create a mock AuthenticateUserRequest
        AuthenticateUserRequest request = new AuthenticateUserRequest();
        request.setPhoneNumber(VALID_PHONE_NUMBER);

        // Mock behavior of validationUtil and userRepository
        when(validationUtil.isValidPhoneNumber(VALID_PHONE_NUMBER)).thenReturn(true);
        when(userRepository.findByPhoneNumber(VALID_PHONE_NUMBER)).thenReturn(Optional.empty());

        // Mock behavior of userAuthRepository
        User user = getUser();
        when(userRepository.save(any())).thenReturn(user);

        // Perform the test
        AuthenticateUserResponse response = authenticationService.generateOtp(request);

        // Verify that the response contains a non-empty auth token
        assertNotNull(response.getOtp());
    }

    @Test(expected = BadRequestException.class)
    public void testGenerateOtpInvalidPhoneNumber() {
        // Create a mock AuthenticateUserRequest with an invalid phone number
        AuthenticateUserRequest request = new AuthenticateUserRequest();
        request.setPhoneNumber("invalidPhoneNumber");

        // Mock behavior of validationUtil to return false for invalid phone number
        when(validationUtil.isValidPhoneNumber("invalidPhoneNumber")).thenReturn(false);

        // Perform the test, should throw a BadRequestException
        authenticationService.generateOtp(request);
    }

    @Test(expected = BadRequestException.class)
    public void testGenerateOtpWithoutPhoneNumber() {
        // Create a mock AuthenticateUserRequest with an invalid phone number
        AuthenticateUserRequest request = new AuthenticateUserRequest();
        request.setPhoneNumber("");

        // Perform the test, should throw a BadRequestException
        authenticationService.generateOtp(request);
    }

    @Test
    public void testLoginSuccess() {
        ReflectionTestUtils.setField(authenticationService, "authTokenExpirationTimeInMinutes", 10);
        // Create a mock AuthenticateUserRequest
        AuthenticateUserRequest request = new AuthenticateUserRequest();
        request.setPhoneNumber(VALID_PHONE_NUMBER);
        request.setOtp(123456);

        // Mock behavior of userRepository
        User user = new User();
        user.setId(USER_ID);
        user.setPhoneNumber(VALID_PHONE_NUMBER);
        when(validationUtil.isValidPhoneNumber(VALID_PHONE_NUMBER)).thenReturn(true);
        when(userRepository.findByPhoneNumber(VALID_PHONE_NUMBER)).thenReturn(Optional.of(user));

        // Mock behavior of userAuthRepository
        UserAuth userAuth = new UserAuth();
        userAuth.setStatus(UserAuthStatus.CREATED);
        userAuth.setExpiresAt(LocalDateTime.now().plusMinutes(10L));
        userAuth.setUserId(USER_ID);
        userAuth.setAuthToken("some-auth-token");
        userAuth.setOtp(123456);
        List<UserAuth> userAuthList = Collections.singletonList(userAuth);
        when(userAuthRepository.findAllByUserIdAndExpiresAtGreaterThan(eq(USER_ID), any())).thenReturn(userAuthList);
        when(userAuthRepository.save(any(UserAuth.class))).thenReturn(userAuth);

        // Perform the test
        AuthenticateUserResponse response = authenticationService.login(request);

        // Verify that the response contains a non-empty auth token
        assertNotNull(response.getAuthToken());
    }

    @Test(expected = BadRequestException.class)
    public void testLoginNoUserFound() {
        // Create a mock AuthenticateUserRequest
        AuthenticateUserRequest request = new AuthenticateUserRequest();
        request.setPhoneNumber(VALID_PHONE_NUMBER);
        request.setOtp(123456);

        when(validationUtil.isValidPhoneNumber(VALID_PHONE_NUMBER)).thenReturn(true);

        // Mock behavior of userRepository to return no user
        when(userRepository.findByPhoneNumber(VALID_PHONE_NUMBER)).thenReturn(Optional.empty());

        // Perform the test, should throw a BadRequestException
        authenticationService.login(request);
    }

    @Test(expected = BadRequestException.class)
    public void testLoginWithoutPhoneNumber() {
        // Create a mock AuthenticateUserRequest
        AuthenticateUserRequest request = new AuthenticateUserRequest();
        request.setPhoneNumber("");
        request.setOtp(123456);

        // Perform the test, should throw a BadRequestException
        authenticationService.login(request);
    }

    @Test(expected = BadRequestException.class)
    public void testLoginNoValidUserAuthFound() {
        // Create a mock AuthenticateUserRequest
        AuthenticateUserRequest request = new AuthenticateUserRequest();
        request.setPhoneNumber(VALID_PHONE_NUMBER);
        request.setOtp(123456);

        when(validationUtil.isValidPhoneNumber(VALID_PHONE_NUMBER)).thenReturn(true);

        // Mock behavior of userRepository
        User user = new User();
        user.setId(UUID.randomUUID().toString());
        when(userRepository.findByPhoneNumber(VALID_PHONE_NUMBER)).thenReturn(Optional.of(user));

        // Perform the test, should throw a BadRequestException
        authenticationService.login(request);
    }

    @Test
    public void testLogoutSuccess() {
        when(requestMetadata.authToken()).thenReturn("auth-token");
        UserAuth userAuth = new UserAuth();
        userAuth.setStatus(UserAuthStatus.CREATED);
        userAuth.setExpiresAt(LocalDateTime.now().plusMinutes(10L));
        userAuth.setUserId(USER_ID);
        userAuth.setAuthToken("some-auth-token");
        userAuth.setOtp(123456);

        when(userAuthRepository.findByAuthToken("auth-token")).thenReturn(Optional.of(userAuth));

        authenticationService.logout();
    }

    @Test(expected = ServiceException.class)
    public void testLogoutWithoutAuth() {
        when(requestMetadata.authToken()).thenReturn("");

        authenticationService.logout();
    }

    @Test(expected = ServiceException.class)
    public void testLogoutWhenUserAuthNotFound() {
        when(requestMetadata.authToken()).thenReturn("auth-token");

        when(userAuthRepository.findByAuthToken("auth-token")).thenReturn(Optional.empty());

        authenticationService.logout();
    }
}
