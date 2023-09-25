package com.rutvik.interview.aspire.miniaspireservice.service;


import com.rutvik.interview.aspire.miniaspireservice.api.contract.LoanUpdateRequest;
import com.rutvik.interview.aspire.miniaspireservice.api.enums.LoanRepaymentStatus;
import com.rutvik.interview.aspire.miniaspireservice.api.enums.LoanStatus;
import com.rutvik.interview.aspire.miniaspireservice.api.exception.BadRequestException;
import com.rutvik.interview.aspire.miniaspireservice.repo.LoanRepository;
import com.rutvik.interview.aspire.miniaspireservice.repo.entity.Loan;
import com.rutvik.interview.aspire.miniaspireservice.repo.entity.LoanRepaymentDetail;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminLoanService {

    private final LoanRepository loanRepository;


    @Transactional
    public Loan updateLoanStatus(String loanId, LoanUpdateRequest request) {
        Optional<Loan> loanOptional = loanRepository.findById(loanId);
        if (loanOptional.isPresent()) {
            Loan loan = loanOptional.get();
            if (loan.getStatus() == LoanStatus.CREATED) {
                loan.setStatus(request.getStatus());
                addLoanRepayments(loan);
                return saveLoan(loan);
            } else {
                throw new BadRequestException(
                        String.format("Can't update loan status as it is already in %s state",
                                loanOptional.get().getStatus()));
            }
        }
        throw new BadRequestException("Invalid loan id provided!");
    }

    private Loan saveLoan(Loan loan) {
        return loanRepository.save(loan);
    }

    private void addLoanRepayments(Loan loan) {
        int term = loan.getLoanTerm();
        BigDecimal loanAmount = loan.getAmount();
        LocalDate currentDate = LocalDate.now();
        List<BigDecimal> installmentAmounts = distributeLoanAmount(loanAmount, term);
        List<LoanRepaymentDetail> loanRepaymentDetailList = new ArrayList<>();
        for (int i = 1; i <= term; i++) {
            loanRepaymentDetailList.add(createLoanRepayment(i, installmentAmounts.get(i - 1), loan, currentDate));
        }
        loan.setRepaymentDetails(loanRepaymentDetailList);
        loan.setNextRepaymentDetail(loanRepaymentDetailList.get(0));
    }

    private List<BigDecimal> distributeLoanAmount(BigDecimal loanAmount, int n) {
        List<BigDecimal> installmentAmounts = new ArrayList<>();

        // Calculate the base installment amount (loanAmount / n)
        BigDecimal baseInstallment = loanAmount.divide(BigDecimal.valueOf(n), 2, RoundingMode.HALF_UP);

        // Calculate the remaining amount for the last installment
        BigDecimal remainingAmount = loanAmount.subtract(baseInstallment.multiply(BigDecimal.valueOf(n - 1)));

        // Add n-1 equal installments
        for (int i = 0; i < n - 1; i++) {
            installmentAmounts.add(baseInstallment);
        }

        // Add the last installment with the remaining amount
        installmentAmounts.add(remainingAmount);

        return installmentAmounts;
    }

    private LoanRepaymentDetail createLoanRepayment(int count, BigDecimal installmentAmount, Loan loan, LocalDate currDate) {
        LocalDate scheduledDate = currDate.plusDays((long) loan.getFrequency().getNumberOfDays() * count);
        return LoanRepaymentDetail.builder()
                .id(UUID.randomUUID().toString())
                .loan(loan)
                .status(LoanRepaymentStatus.UNPAID)
                .repaymentNumber(count)
                .paymentReferenceIds("")
                .amountRepaid(BigDecimal.ZERO)
                .installmentAmount(installmentAmount)
                .scheduledAt(scheduledDate)
                .build();
    }

}
