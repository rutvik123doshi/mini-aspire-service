package com.rutvik.interview.aspire.miniaspireservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rutvik.interview.aspire.miniaspireservice.api.contract.AuthenticateUserRequest;
import com.rutvik.interview.aspire.miniaspireservice.api.contract.AuthenticateUserResponse;
import com.rutvik.interview.aspire.miniaspireservice.api.exception.BadRequestException;
import com.rutvik.interview.aspire.miniaspireservice.service.AdminLoanService;
import com.rutvik.interview.aspire.miniaspireservice.service.AuthenticationService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;


@RunWith(MockitoJUnitRunner.class)
public class AuthenticationControllerTest {
    @Mock
    private AdminLoanService adminLoanService;

    @InjectMocks
    private AuthenticationController authenticationController;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private AuthenticationService authenticationService;

    private AuthenticateUserRequest validRequest;

    @Before
    public void setUp() {
        validRequest = new AuthenticateUserRequest();
        validRequest.setPhoneNumber("1234567890");
        validRequest.setOtp(123123);
    }

    private AuthenticateUserResponse getAuthenticateUserResponse() {
        return AuthenticateUserResponse.builder().otp(123123).authToken("auth-token").build();
    }

    @Test
    public void testGenerateOtpWithValidRequest() throws Exception {
        // Mock behavior
        AuthenticateUserResponse authenticateUserResponse = getAuthenticateUserResponse();
        when(authenticationService.generateOtp(validRequest)).thenReturn(authenticateUserResponse);

        // Perform the test
        ResponseEntity<?> response = authenticationController.generateOtp(validRequest);

        // Verify the results
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("123123"));
    }

    @Test
    public void testGenerateOtpWithBadRequestException() throws Exception {
        // Mock behavior to simulate a BadRequestException
        when(authenticationService.generateOtp(validRequest)).thenThrow(new BadRequestException("Bad request"));

        // Perform the test
        ResponseEntity<?> response = authenticationController.generateOtp(validRequest);

        // Verify the results for a BadRequestException scenario
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("[User Error] Failed to generate otp:: Bad request"));
    }

    @Test
    public void testGenerateOtpWithInternalServerError() throws Exception {
        // Mock behavior to simulate an internal server error
        when(authenticationService.generateOtp(validRequest)).thenThrow(new RuntimeException("Internal server error"));

        // Perform the test
        ResponseEntity<?> response = authenticationController.generateOtp(validRequest);

        // Verify the results for an internal server error scenario
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("[Internal Error] Failed to generate otp:: Internal server error"));
    }


    @Test
    public void testLoginWithValidRequest() throws Exception {

        AuthenticateUserResponse authenticateUserResponse = getAuthenticateUserResponse();
        // Mock behavior
        when(authenticationService.login(validRequest)).thenReturn(authenticateUserResponse);

        // Perform the test
        ResponseEntity<?> response = authenticationController.login(validRequest);

        // Verify the results
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("auth-token"));
    }

    @Test
    public void testLoginWithBadRequestException() throws Exception {
        // Mock behavior to simulate a BadRequestException
        when(authenticationService.login(validRequest)).thenThrow(new BadRequestException("Bad request"));

        // Perform the test
        ResponseEntity<?> response = authenticationController.login(validRequest);

        // Verify the results for a BadRequestException scenario
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("[User Error] Failed to login:: Bad request"));
    }

    @Test
    public void testLoginWithInternalServerError() throws Exception {
        // Mock behavior to simulate an internal server error
        when(authenticationService.login(validRequest)).thenThrow(new RuntimeException("Internal server error"));

        // Perform the test
        ResponseEntity<?> response = authenticationController.login(validRequest);

        // Verify the results for an internal server error scenario
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("[Internal Error] Failed to login:: Internal server error"));
    }

    @Test
    public void testLogout() throws Exception {
        // Perform the test
        ResponseEntity<?> response = authenticationController.logout();

        // Verify the results for a successful logout
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(authenticationService, times(1)).logout();
    }

    @Test
    public void testLogoutWithInternalServerError() throws Exception {
        // Mock behavior to simulate an internal server error
        doThrow(new RuntimeException("Internal server error")).when(authenticationService).logout();


        // Perform the test
        ResponseEntity<?> response = authenticationController.logout();

        // Verify the results for an internal server error scenario
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("[Internal Error] Failed to logout:: Internal server error"));
    }
}
