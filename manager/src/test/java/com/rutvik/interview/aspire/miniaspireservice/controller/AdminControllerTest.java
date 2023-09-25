package com.rutvik.interview.aspire.miniaspireservice.controller;

import com.rutvik.interview.aspire.miniaspireservice.api.GenericResponse;
import com.rutvik.interview.aspire.miniaspireservice.api.contract.LoanUpdateRequest;
import com.rutvik.interview.aspire.miniaspireservice.api.enums.LoanStatus;
import com.rutvik.interview.aspire.miniaspireservice.api.exception.BadRequestException;
import com.rutvik.interview.aspire.miniaspireservice.repo.entity.Loan;
import com.rutvik.interview.aspire.miniaspireservice.service.AdminLoanService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AdminControllerTest {

    private static final String VALID_TOKEN = "validToken";
    private static final String INVALID_TOKEN = "invalidToken";
    @InjectMocks
    private AdminController adminController;
    @Mock
    private AdminLoanService adminLoanService;

    @Before
    public void before() {
        ReflectionTestUtils.setField(adminController, "apiAdminToken", VALID_TOKEN);
    }

    private LoanUpdateRequest getLoanUpdateRequest() {
        return LoanUpdateRequest.builder().status(LoanStatus.APPROVED).build();
    }

    @Test
    public void testUpdateLoanStatusWithValidToken() throws Exception {
        // Prepare test data
        String loanId = "123";
        LoanUpdateRequest loanUpdateRequest = getLoanUpdateRequest();
        String token = VALID_TOKEN;

        // Mock behavior
        when(adminLoanService.updateLoanStatus(loanId, loanUpdateRequest))
                .thenReturn(Loan.builder().status(LoanStatus.APPROVED).build());

        // Perform the test
        ResponseEntity<GenericResponse<Object>> response = adminController.updateLoanStatus(loanId, loanUpdateRequest, token);

        // Verify the results
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void testUpdateLoanStatusWithInvalidToken() throws Exception {
        // Prepare test data
        String loanId = "123";
        LoanUpdateRequest loanUpdateRequest = getLoanUpdateRequest();
        String token = INVALID_TOKEN;

        // Perform the test
        ResponseEntity<GenericResponse<Object>> response = adminController.updateLoanStatus(loanId, loanUpdateRequest, token);

        // Verify the results for an invalid token scenario
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid auth token!", response.getBody().getError());
    }

    @Test
    public void testUpdateLoanStatusWithBadRequestException() throws Exception {
        // Prepare test data
        String loanId = "123";
        LoanUpdateRequest loanUpdateRequest = new LoanUpdateRequest();
        String token = VALID_TOKEN;

        // Mock behavior to simulate a BadRequestException
        when(adminLoanService.updateLoanStatus(loanId, loanUpdateRequest))
                .thenThrow(new BadRequestException("Bad request"));

        // Perform the test
        ResponseEntity<GenericResponse<Object>> response = adminController.updateLoanStatus(loanId, loanUpdateRequest, token);

        // Verify the results for a BadRequestException scenario
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("[User Error] Failed to update loan status:: Bad request", response.getBody().getError());
    }

    @Test
    public void testUpdateLoanStatusWithInternalServerError() throws Exception {
        // Prepare test data
        String loanId = "123";
        LoanUpdateRequest loanUpdateRequest = new LoanUpdateRequest();
        String token = VALID_TOKEN;

        // Mock behavior to simulate an internal server error
        when(adminLoanService.updateLoanStatus(loanId, loanUpdateRequest))
                .thenThrow(new RuntimeException("Internal server error"));

        // Perform the test
        ResponseEntity<GenericResponse<Object>> response = adminController.updateLoanStatus(loanId, loanUpdateRequest, token);

        // Verify the results for an internal server error scenario
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("[Internal Error] Failed to update loan status:: Internal server error", response.getBody().getError());
    }
}
