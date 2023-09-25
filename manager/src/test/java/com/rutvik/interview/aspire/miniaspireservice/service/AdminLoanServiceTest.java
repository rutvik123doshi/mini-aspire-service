package com.rutvik.interview.aspire.miniaspireservice.service;

import com.rutvik.interview.aspire.miniaspireservice.api.contract.LoanUpdateRequest;
import com.rutvik.interview.aspire.miniaspireservice.api.enums.Frequency;
import com.rutvik.interview.aspire.miniaspireservice.api.enums.LoanStatus;
import com.rutvik.interview.aspire.miniaspireservice.api.exception.BadRequestException;
import com.rutvik.interview.aspire.miniaspireservice.repo.LoanRepository;
import com.rutvik.interview.aspire.miniaspireservice.repo.entity.Loan;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AdminLoanServiceTest {

    @InjectMocks
    private AdminLoanService adminLoanService;

    @Mock
    private LoanRepository loanRepository;

    private Loan getLoan() {
        // Create a mock Loan object
        Loan loan = new Loan();
        loan.setId("loanId");
        loan.setStatus(LoanStatus.CREATED);
        loan.setLoanTerm(5);
        loan.setFrequency(Frequency.WEEKLY);
        loan.setAmount(BigDecimal.valueOf(1000L));

        return loan;
    }

    @Test
    public void testUpdateLoanStatusSuccess() {
        Loan loan = getLoan();

        // Create a LoanUpdateRequest
        LoanUpdateRequest request = new LoanUpdateRequest();
        request.setStatus(LoanStatus.APPROVED);

        // Mock behavior of loanRepository
        when(loanRepository.findById("loanId")).thenReturn(Optional.of(loan));
        when(loanRepository.save(loan)).thenReturn(loan);

        // Perform the test
        Loan updatedLoan = adminLoanService.updateLoanStatus("loanId", request);

        // Verify that the loan status is updated
        assertEquals(LoanStatus.APPROVED, updatedLoan.getStatus());
    }

    @Test(expected = BadRequestException.class)
    public void testUpdateLoanStatusInvalidLoanId() {
        // Mock behavior of loanRepository to return an empty Optional
        when(loanRepository.findById("invalidLoanId")).thenReturn(Optional.empty());

        // Create a LoanUpdateRequest
        LoanUpdateRequest request = new LoanUpdateRequest();
        request.setStatus(LoanStatus.APPROVED);

        // Perform the test, should throw a BadRequestException
        adminLoanService.updateLoanStatus("invalidLoanId", request);
    }

    @Test(expected = BadRequestException.class)
    public void testUpdateLoanStatusLoanAlreadyApproved() {
        // Create a mock Loan object with an approved status
        Loan loan = new Loan();
        loan.setId("loanId");
        loan.setStatus(LoanStatus.APPROVED);

        // Create a LoanUpdateRequest
        LoanUpdateRequest request = new LoanUpdateRequest();
        request.setStatus(LoanStatus.APPROVED);

        // Mock behavior of loanRepository
        when(loanRepository.findById("loanId")).thenReturn(Optional.of(loan));

        // Perform the test, should throw a BadRequestException
        adminLoanService.updateLoanStatus("loanId", request);
    }

}
