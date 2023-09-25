package com.rutvik.interview.aspire.miniaspireservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rutvik.interview.aspire.miniaspireservice.api.contract.LoanCreationRequest;
import com.rutvik.interview.aspire.miniaspireservice.api.contract.LoanRepaymentRequest;
import com.rutvik.interview.aspire.miniaspireservice.api.exception.BadRequestException;
import com.rutvik.interview.aspire.miniaspireservice.repo.entity.Loan;
import com.rutvik.interview.aspire.miniaspireservice.service.LoansService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class LoanRequestControllerTest {

    @Mock
    private LoansService loansService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private LoanRequestController loanRequestController;


    private LoanCreationRequest validLoanCreationRequest;
    private LoanRepaymentRequest validLoanRepaymentRequest;

    @Before
    public void setUp() {

        validLoanCreationRequest = new LoanCreationRequest();
        // Initialize validLoanCreationRequest fields here

        validLoanRepaymentRequest = new LoanRepaymentRequest();
        // Initialize validLoanRepaymentRequest fields here
    }

    private Loan getLoan() {
        return Loan.builder().id("1234").loanTerm(5).amount(BigDecimal.valueOf(1000L)).build();
    }

    @Test
    public void testCreateLoanWithValidRequest() throws Exception {

        Loan loan = getLoan();
        when(loansService.createLoan(validLoanCreationRequest)).thenReturn(loan);

        ResponseEntity<?> response = loanRequestController.createLoan(validLoanCreationRequest);

        // Verify the results
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("1234"));
    }

    @Test
    public void testCreateLoanWithBadRequestException() throws Exception {
        // Mock behavior to simulate a BadRequestException
        when(loansService.createLoan(validLoanCreationRequest)).thenThrow(new BadRequestException("Bad request"));

        // Perform the test
        ResponseEntity<?> response = loanRequestController.createLoan(validLoanCreationRequest);

        // Verify the results for a BadRequestException scenario
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("[User Error] Failed to create loan:: Bad request"));
    }

    @Test
    public void testCreateLoanWithInternalServerError() throws Exception {
        // Mock behavior to simulate an internal server error
        when(loansService.createLoan(validLoanCreationRequest)).thenThrow(new RuntimeException("Internal server error"));

        // Perform the test
        ResponseEntity<?> response = loanRequestController.createLoan(validLoanCreationRequest);

        // Verify the results for an internal server error scenario
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("[Internal Error] Failed to create loan:: Internal server error"));
    }


    @Test
    public void testRepayLoanWithValidRequest() throws Exception {
        // Mock behavior
        Loan loan = getLoan();
        when(loansService.repayLoan("loanId", validLoanRepaymentRequest)).thenReturn(loan);

        // Perform the test
        ResponseEntity<?> response = loanRequestController.repayLoan("loanId", validLoanRepaymentRequest);

        // Verify the results
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("1234"));
    }

    @Test
    public void testRepayLoanWithBadRequestException() throws Exception {
        // Mock behavior to simulate a BadRequestException
        when(loansService.repayLoan("loanId", validLoanRepaymentRequest)).thenThrow(new BadRequestException("Bad request"));

        // Perform the test
        ResponseEntity<?> response = loanRequestController.repayLoan("loanId", validLoanRepaymentRequest);

        // Verify the results for a BadRequestException scenario
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("[User Error] Failed to create loan:: Bad request"));
    }

    @Test
    public void testRepayLoanWithInternalServerError() throws Exception {
        // Mock behavior to simulate an internal server error
        when(loansService.repayLoan("loanId", validLoanRepaymentRequest)).thenThrow(new RuntimeException("Internal server error"));

        // Perform the test
        ResponseEntity<?> response = loanRequestController.repayLoan("loanId", validLoanRepaymentRequest);

        // Verify the results for an internal server error scenario
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("[Internal Error] Failed to create loan:: Internal server error"));
    }

    @Test
    public void testGetCustomerLoans() throws Exception {
        // Mock behavior
        Loan loan = getLoan();
        when(loansService.getAllLoans()).thenReturn(List.of(loan));

        // Perform the test
        ResponseEntity<?> response = loanRequestController.getCustomerLoans();

        // Verify the results
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("1234"));
    }

    @Test
    public void testGetAllLoansWithBadRequestException() throws Exception {
        // Mock behavior to simulate a BadRequestException
        when(loansService.getAllLoans()).thenThrow(new BadRequestException("Bad request"));

        // Perform the test
        ResponseEntity<?> response = loanRequestController.getCustomerLoans();

        // Verify the results for a BadRequestException scenario
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("[User Error] Failed to getting all loans:: Bad request"));
    }

    @Test
    public void testGetAllLoansWithException() throws Exception {
        // Mock behavior to simulate a BadRequestException
        when(loansService.getAllLoans()).thenThrow(new RuntimeException("ISE"));

        // Perform the test
        ResponseEntity<?> response = loanRequestController.getCustomerLoans();

        // Verify the results for a BadRequestException scenario
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("[Internal Error] Failed getting all loans:: ISE"));
    }

    @Test
    public void testGetLoanWithValidLoanId() throws Exception {
        // Mock behavior
        when(loansService.getLoan("loanId")).thenReturn(getLoan());

        // Perform the test
        ResponseEntity<?> response = loanRequestController.getLoan("loanId");

        // Verify the results
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("1234"));
    }

    @Test
    public void testGetLoanWithInvalidLoanId() throws Exception {
        // Mock behavior to simulate a BadRequestException
        when(loansService.getLoan("invalidLoanId")).thenThrow(new BadRequestException("Invalid loan ID"));

        // Perform the test
        ResponseEntity<?> response = loanRequestController.getLoan("invalidLoanId");

        // Verify the results for a BadRequestException scenario
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("[User Error] Failed to getting loan:: Invalid loan ID"));
    }

    @Test
    public void testGetLoanWithInternalServerError() throws Exception {
        // Mock behavior to simulate an internal server error
        when(loansService.getLoan("loanId")).thenThrow(new RuntimeException("Internal server error"));

        // Perform the test
        ResponseEntity<?> response = loanRequestController.getLoan("loanId");

        // Verify the results for an internal server error scenario
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("[Internal Error] Failed getting loan:: Internal server error"));
    }


}
