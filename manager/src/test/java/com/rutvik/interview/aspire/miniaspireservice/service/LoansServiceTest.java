package com.rutvik.interview.aspire.miniaspireservice.service;

import com.rutvik.interview.aspire.miniaspireservice.api.enums.Frequency;
import com.rutvik.interview.aspire.miniaspireservice.api.enums.LoanRepaymentStatus;
import com.rutvik.interview.aspire.miniaspireservice.api.exception.ServiceException;
import com.rutvik.interview.aspire.miniaspireservice.api.filter.RequestMetadata;
import com.rutvik.interview.aspire.miniaspireservice.repo.LoanRepository;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import com.rutvik.interview.aspire.miniaspireservice.api.contract.LoanCreationRequest;
import com.rutvik.interview.aspire.miniaspireservice.api.contract.LoanRepaymentRequest;
import com.rutvik.interview.aspire.miniaspireservice.api.enums.LoanStatus;
import com.rutvik.interview.aspire.miniaspireservice.api.exception.BadRequestException;
import com.rutvik.interview.aspire.miniaspireservice.repo.LoanRepository;
import com.rutvik.interview.aspire.miniaspireservice.repo.entity.Loan;
import com.rutvik.interview.aspire.miniaspireservice.repo.entity.LoanRepaymentDetail;
import com.rutvik.interview.aspire.miniaspireservice.api.filter.RequestMetadata;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;


@RunWith(MockitoJUnitRunner.class)
public class LoansServiceTest {

    @InjectMocks
    private LoansService loansService;

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private RequestMetadata requestMetadata;

    @Test
    public void testCreateLoanSuccess() {
        // Create a mock LoanCreationRequest
        LoanCreationRequest request = new LoanCreationRequest();
        request.setTotalTerm(12);
        request.setAmount(BigDecimal.valueOf(1000.0));

        // Mock behavior of requestMetadata to return a user ID
        when(requestMetadata.customerId()).thenReturn("userId");

        // Mock behavior of loanRepository
        Loan savedLoan = getLoan();
        when(loanRepository.save(any(Loan.class))).thenReturn(savedLoan);

        // Perform the test
        Loan result = loansService.createLoan(request);

        // Verify that the returned loan has the expected values
        assertEquals("loan-id", result.getId());
        assertEquals("user-id", result.getUserId());
        assertEquals(3, (int) result.getLoanTerm());
        assertEquals(BigDecimal.valueOf(1000.0), result.getAmount());
        assertEquals(BigDecimal.ZERO, result.getAmountRepaid());
    }

    private Loan getLoan() {
        Loan savedLoan = new Loan();
        savedLoan.setLoanTerm(3);
        savedLoan.setAmountRepaid(BigDecimal.ZERO);
        savedLoan.setAmount(BigDecimal.valueOf(1000.0));
        savedLoan.setId("loan-id");
        savedLoan.setUserId("user-id");
        savedLoan.setFrequency(Frequency.WEEKLY);


        List<LoanRepaymentDetail> loanRepaymentDetailList = new ArrayList<>();
        for(int i=0; i<3; i++) {
            LoanRepaymentDetail lrd = LoanRepaymentDetail.builder()
                    .id(""+i)
                    .repaymentNumber(i+1)
                    .installmentAmount(i==2 ? BigDecimal.valueOf(333.34) : BigDecimal.valueOf(333.33))
                    .amountRepaid(BigDecimal.ZERO)
                    .build();
            loanRepaymentDetailList.add(lrd);
        }
        savedLoan.setRepaymentDetails(loanRepaymentDetailList);
        savedLoan.setNextRepaymentDetail(loanRepaymentDetailList.get(0));

        return savedLoan;
    }

    @Test(expected = BadRequestException.class)
    public void testCreateLoanInvalidTerm() {
        // Create a mock LoanCreationRequest with an invalid term
        LoanCreationRequest request = new LoanCreationRequest();
        request.setTotalTerm(-1);
        request.setAmount(BigDecimal.valueOf(1000.0));

        // Mock behavior of requestMetadata to return a user ID
        when(requestMetadata.customerId()).thenReturn("userId");

        // Perform the test, should throw a BadRequestException
        loansService.createLoan(request);
    }

    @Test(expected = BadRequestException.class)
    public void testCreateLoanInvalidAmount() {
        // Create a mock LoanCreationRequest with an invalid amount
        LoanCreationRequest request = new LoanCreationRequest();
        request.setTotalTerm(12);
        request.setAmount(BigDecimal.valueOf(-1000.0));

        // Mock behavior of requestMetadata to return a user ID
        when(requestMetadata.customerId()).thenReturn("userId");

        // Perform the test, should throw a BadRequestException
        loansService.createLoan(request);
    }

    @Test(expected = BadRequestException.class)
    public void testCreateLoanWithAmountIn3DecimalPlace() {
        // Create a mock LoanCreationRequest with an invalid amount
        LoanCreationRequest request = new LoanCreationRequest();
        request.setTotalTerm(12);
        request.setAmount(BigDecimal.valueOf(1000.123));

        // Mock behavior of requestMetadata to return a user ID
        when(requestMetadata.customerId()).thenReturn("userId");

        // Perform the test, should throw a BadRequestException
        loansService.createLoan(request);
    }

    @Test
    public void testGetLoan() {
        String loanId = "loan-id";
        when(requestMetadata.customerId()).thenReturn("user-id");
        Loan loan = getLoan();
        when(loanRepository.findById(loanId)).thenReturn(Optional.of(loan));

        Loan response = loansService.getLoan(loanId);
        assertEquals(loan, response);
    }

    @Test(expected = BadRequestException.class)
    public void testGetLoanWithEmptyId() {
        String loanId = "";

        loansService.getLoan(loanId);
    }

    @Test(expected = ServiceException.class)
    public void testGetLoanWithoutUserId() {
        String loanId = "asdf";

        when(requestMetadata.customerId()).thenReturn("");

        loansService.getLoan(loanId);
    }

    @Test(expected = BadRequestException.class)
    public void testGetLoanWithInvalidLoanId() {
        String loanId = "asdf";

        when(requestMetadata.customerId()).thenReturn("user-id");
        when(loanRepository.findById(loanId)).thenReturn(Optional.empty());

        loansService.getLoan(loanId);
    }

    @Test(expected = BadRequestException.class)
    public void testGetLoanWithoutUserHavingPermission() {
        String loanId = "loan-id";
        when(requestMetadata.customerId()).thenReturn("some-other-user");

        Loan loan = getLoan();
        when(loanRepository.findById(loanId)).thenReturn(Optional.of(loan));

        loansService.getLoan(loanId);
    }

    @Test
    public void testGetAllLoansSuccess() {
        when(requestMetadata.customerId()).thenReturn("user");
        Loan loan = getLoan();
        when(loanRepository.findAllByUserId("user")).thenReturn(List.of(loan));
        List<Loan> response = loansService.getAllLoans();
        assertEquals(loan, response.get(0));
    }


    @Test
    public void testRepayLoanSuccess() {
        // Create a mock LoanRepaymentRequest
        LoanRepaymentRequest request = new LoanRepaymentRequest();
        request.setRepaymentAmount(new BigDecimal("500.00"));
        request.setPaymentReferenceId("payment123");

        // Mock behavior of requestMetadata to return a user ID
        when(requestMetadata.customerId()).thenReturn("user-id");

        // Mock behavior of loanRepository
        Loan existingLoan = getLoan();
        existingLoan.setStatus(LoanStatus.APPROVED);

        // Mock behavior of loanRepository to find an existing loan
        when(loanRepository.findById("loan-id")).thenReturn(Optional.of(existingLoan));

        // Perform the test
        Loan result = loansService.repayLoan("loan-id", request);

        // Verify that the returned loan has the expected values
        assertEquals(new BigDecimal("500.00"), result.getAmountRepaid());

        // Verify that the repayment details are updated
        List<LoanRepaymentDetail> repaymentDetails = result.getRepaymentDetails();
        assertEquals(3, repaymentDetails.size());
        assertEquals(new BigDecimal("500.00"), result.getAmountRepaid());

        assertEquals(LoanRepaymentStatus.PAID, repaymentDetails.get(0).getStatus());
        assertEquals(LoanRepaymentStatus.PARTIAL, repaymentDetails.get(1).getStatus());
        assertEquals(new BigDecimal("166.67"), repaymentDetails.get(1).getAmountRepaid());
    }

    @Test(expected = BadRequestException.class)
    public void testRepayLoan_WhenUserMismatch() {
        // Create a mock LoanRepaymentRequest
        LoanRepaymentRequest request = new LoanRepaymentRequest();
        request.setRepaymentAmount(new BigDecimal("500.00"));
        request.setPaymentReferenceId("payment123");

        // Mock behavior of requestMetadata to return a user ID
        when(requestMetadata.customerId()).thenReturn("userid");

        // Mock behavior of loanRepository
        Loan existingLoan = getLoan();
        existingLoan.setStatus(LoanStatus.APPROVED);

        // Mock behavior of loanRepository to find an existing loan
        when(loanRepository.findById("loan-id")).thenReturn(Optional.of(existingLoan));

        loansService.repayLoan("loan-id", request);
    }

    @Test(expected = BadRequestException.class)
    public void testRepayLoan_WhenRepaymentAmountIsNegative() {
        // Create a mock LoanRepaymentRequest
        LoanRepaymentRequest request = new LoanRepaymentRequest();
        request.setRepaymentAmount(new BigDecimal("-500.00"));
        request.setPaymentReferenceId("payment123");

        // Mock behavior of requestMetadata to return a user ID
        when(requestMetadata.customerId()).thenReturn("user-id");

        // Mock behavior of loanRepository
        Loan existingLoan = getLoan();
        existingLoan.setStatus(LoanStatus.APPROVED);

        // Mock behavior of loanRepository to find an existing loan
        when(loanRepository.findById("loan-id")).thenReturn(Optional.of(existingLoan));

        loansService.repayLoan("loan-id", request);
    }

    @Test(expected = BadRequestException.class)
    public void testRepayLoan_WhenRepaymentPaymentRefIdIsBlank() {
        // Create a mock LoanRepaymentRequest
        LoanRepaymentRequest request = new LoanRepaymentRequest();
        request.setRepaymentAmount(new BigDecimal("500.00"));
        request.setPaymentReferenceId("");

        // Mock behavior of requestMetadata to return a user ID
        when(requestMetadata.customerId()).thenReturn("user-id");

        // Mock behavior of loanRepository
        Loan existingLoan = getLoan();
        existingLoan.setStatus(LoanStatus.APPROVED);

        // Mock behavior of loanRepository to find an existing loan
        when(loanRepository.findById("loan-id")).thenReturn(Optional.of(existingLoan));

        loansService.repayLoan("loan-id", request);
    }

    @Test(expected = BadRequestException.class)
    public void testRepayLoan_WhenLoanStatusIsNotApproved() {
        // Create a mock LoanRepaymentRequest
        LoanRepaymentRequest request = new LoanRepaymentRequest();
        request.setRepaymentAmount(new BigDecimal("500.00"));
        request.setPaymentReferenceId("asdf");

        // Mock behavior of requestMetadata to return a user ID
        when(requestMetadata.customerId()).thenReturn("user-id");

        // Mock behavior of loanRepository
        Loan existingLoan = getLoan();
        existingLoan.setStatus(LoanStatus.REJECTED);

        // Mock behavior of loanRepository to find an existing loan
        when(loanRepository.findById("loan-id")).thenReturn(Optional.of(existingLoan));

        loansService.repayLoan("loan-id", request);
    }

    @Test(expected = BadRequestException.class)
    public void testRepayLoan_WhenRepaymentAmountIs3DecimalPlaces() {
        // Create a mock LoanRepaymentRequest
        LoanRepaymentRequest request = new LoanRepaymentRequest();
        request.setRepaymentAmount(new BigDecimal("500.003"));
        request.setPaymentReferenceId("asdf");

        // Mock behavior of requestMetadata to return a user ID
        when(requestMetadata.customerId()).thenReturn("user-id");

        // Mock behavior of loanRepository
        Loan existingLoan = getLoan();
        existingLoan.setStatus(LoanStatus.APPROVED);

        // Mock behavior of loanRepository to find an existing loan
        when(loanRepository.findById("loan-id")).thenReturn(Optional.of(existingLoan));

        loansService.repayLoan("loan-id", request);
    }

    @Test(expected = BadRequestException.class)
    public void testRepayLoan_WhenRepaymentAmountIsGreaterThanRemainingAmount() {
        // Create a mock LoanRepaymentRequest
        LoanRepaymentRequest request = new LoanRepaymentRequest();
        request.setRepaymentAmount(new BigDecimal("1500.00"));
        request.setPaymentReferenceId("asdf");

        // Mock behavior of requestMetadata to return a user ID
        when(requestMetadata.customerId()).thenReturn("user-id");

        // Mock behavior of loanRepository
        Loan existingLoan = getLoan();
        existingLoan.setStatus(LoanStatus.APPROVED);

        // Mock behavior of loanRepository to find an existing loan
        when(loanRepository.findById("loan-id")).thenReturn(Optional.of(existingLoan));

        loansService.repayLoan("loan-id", request);
    }


    @Test(expected = BadRequestException.class)
    public void testRepayLoanInvalidLoanId() {
        // Create a mock LoanRepaymentRequest
        LoanRepaymentRequest request = new LoanRepaymentRequest();
        request.setRepaymentAmount(BigDecimal.valueOf(500.0));
        request.setPaymentReferenceId("payment123");

        when(requestMetadata.customerId()).thenReturn("user-id");

        // Mock behavior of loanRepository to not find an existing loan
        when(loanRepository.findById("loanId")).thenReturn(Optional.empty());

        // Perform the test, should throw a BadRequestException
        loansService.repayLoan("loanId", request);
    }

}
