package com.rutvik.interview.aspire.miniaspireservice.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.rutvik.interview.aspire.miniaspireservice.api.GenericResponse;
import com.rutvik.interview.aspire.miniaspireservice.api.contract.AuthenticateUserRequest;
import com.rutvik.interview.aspire.miniaspireservice.api.exception.BadRequestException;
import com.rutvik.interview.aspire.miniaspireservice.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/authentication")
@RequiredArgsConstructor
public class AuthenticationController {

    private final ObjectMapper objectMapper;
    private final AuthenticationService authenticationService;

    @PostMapping("/generate-otp")
    public ResponseEntity<?> generateOtp(
            @RequestBody AuthenticateUserRequest authenticateUserRequest
    ) {
        try {
            log.info(">> Received generate otp request for phone: {}", authenticateUserRequest.getPhoneNumber());
            return GenericResponse.successResponse(authenticationService.generateOtp(authenticateUserRequest));
        } catch (BadRequestException e) {
            log.error(">> BadRequestException while generating otp: {}", e.getMessage(), e);
            return GenericResponse.sendBadRequestResponse(
                    HttpStatus.BAD_REQUEST.value(),
                    "[User Error] Failed to generate otp:: " + e.getMessage(),
                    Arrays.toString(e.getStackTrace()));
        } catch (Exception e) {
            log.error(">> Exception while generating otp: {}", e.getMessage(), e);
            return GenericResponse.sendResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "[Internal Error] Failed to generate otp:: " + e.getMessage(),
                    Arrays.toString(e.getStackTrace()),
                    null);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody AuthenticateUserRequest authenticateUserRequest
    ) {
        try {
            log.info(">> Received login request for phone: {}", authenticateUserRequest.getPhoneNumber());
            return GenericResponse.successResponse(authenticationService.login(authenticateUserRequest));
        } catch (BadRequestException e) {
            log.error(">> BadRequestException while login: {}", e.getMessage(), e);
            return GenericResponse.sendBadRequestResponse(
                    HttpStatus.BAD_REQUEST.value(),
                    "[User Error] Failed to login:: " + e.getMessage(),
                    Arrays.toString(e.getStackTrace()));
        } catch (Exception e) {
            log.error(">> Exception while login: {}", e.getMessage(), e);
            return GenericResponse.sendResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "[Internal Error] Failed to login:: " + e.getMessage(),
                    Arrays.toString(e.getStackTrace()),
                    null);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        try {

            authenticationService.logout();
            return GenericResponse.successResponse(Map.of("success", "true"));
        } catch (Exception e) {
            log.error(">> Exception while logout: {}", e.getMessage(), e);
            return GenericResponse.sendResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "[Internal Error] Failed to logout:: " + e.getMessage(),
                    Arrays.toString(e.getStackTrace()),
                    null);
        }
    }


}
