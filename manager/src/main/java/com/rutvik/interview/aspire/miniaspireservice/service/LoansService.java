package com.rutvik.interview.aspire.miniaspireservice.service;

import com.rutvik.interview.aspire.miniaspireservice.api.contract.LoanCreationRequest;
import com.rutvik.interview.aspire.miniaspireservice.api.contract.LoanRepaymentRequest;
import com.rutvik.interview.aspire.miniaspireservice.api.enums.Frequency;
import com.rutvik.interview.aspire.miniaspireservice.api.enums.LoanRepaymentStatus;
import com.rutvik.interview.aspire.miniaspireservice.api.enums.LoanStatus;
import com.rutvik.interview.aspire.miniaspireservice.api.exception.BadRequestException;
import com.rutvik.interview.aspire.miniaspireservice.api.exception.ServiceException;
import com.rutvik.interview.aspire.miniaspireservice.api.filter.RequestMetadata;
import com.rutvik.interview.aspire.miniaspireservice.repo.LoanRepository;
import com.rutvik.interview.aspire.miniaspireservice.repo.entity.Loan;
import com.rutvik.interview.aspire.miniaspireservice.repo.entity.LoanRepaymentDetail;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoansService {
    private final LoanRepository loanRepository;
    private final RequestMetadata requestMetadata;


    @Transactional
    public Loan repayLoan(String loanId, LoanRepaymentRequest request) {
        String userId = getUserIdOrElseThrow();
        Optional<Loan> loanOptional = loanRepository.findById(loanId);
        if (loanOptional.isPresent()) {
            Loan loan = loanOptional.get();
            validateLoanRepaymentRequest(request, loan, userId);

            Map<Integer, LoanRepaymentDetail> termToLoanRepaymentDetailMap = loan.getRepaymentDetails().stream()
                    .collect(Collectors.toMap(
                            LoanRepaymentDetail::getRepaymentNumber, // Key: repaymentCount
                            repaymentDetail -> repaymentDetail,     // Value: LoanRepaymentDetail
                            (existing, replacement) -> existing      // Merge function (in case of duplicate keys)
                    ));


            repayLoan(loan, request, LocalDateTime.now(), termToLoanRepaymentDetailMap);
            loanRepository.save(loan);
            return loan;
        }
        throw new BadRequestException("No loans present with given loan id!");
    }

    private void repayLoan(Loan loan, LoanRepaymentRequest request, LocalDateTime currTime,
                           Map<Integer, LoanRepaymentDetail> termToLoanRepaymentDetailMap
    ) {
        LoanRepaymentDetail nextLoanRepaymentDetail = loan.getNextRepaymentDetail();
        BigDecimal remainingInstallmentAmount = nextLoanRepaymentDetail.getInstallmentAmount().subtract(nextLoanRepaymentDetail.getAmountRepaid());
        int compareRepaymentAmountAndInstallmentAmount = request.getRepaymentAmount().compareTo(remainingInstallmentAmount);


        if (compareRepaymentAmountAndInstallmentAmount == 0) {
            loan.setAmountRepaid(loan.getAmountRepaid().add(request.getRepaymentAmount()));
            nextLoanRepaymentDetail.setAmountRepaid(nextLoanRepaymentDetail.getAmountRepaid().add(request.getRepaymentAmount()));

            nextLoanRepaymentDetail.setRepaymentTime(currTime);
            nextLoanRepaymentDetail.addPaymentReferenceId(request.getPaymentReferenceId());
            nextLoanRepaymentDetail.setStatus(LoanRepaymentStatus.PAID);
            if (loan.getLoanTerm().equals(nextLoanRepaymentDetail.getRepaymentNumber())) {
                loan.setStatus(LoanStatus.PAID);
                loan.setNextRepaymentDetail(null);
            } else {
                loan.setNextRepaymentDetail(termToLoanRepaymentDetailMap.get(nextLoanRepaymentDetail.getRepaymentNumber() + 1));
            }
        } else if (compareRepaymentAmountAndInstallmentAmount < 0) {
            loan.setAmountRepaid(loan.getAmountRepaid().add(request.getRepaymentAmount()));
            nextLoanRepaymentDetail.setAmountRepaid(nextLoanRepaymentDetail.getAmountRepaid().add(request.getRepaymentAmount()));

            nextLoanRepaymentDetail.setRepaymentTime(currTime);
            nextLoanRepaymentDetail.addPaymentReferenceId(request.getPaymentReferenceId());
            nextLoanRepaymentDetail.setStatus(LoanRepaymentStatus.PARTIAL);
        } else {
            loan.setAmountRepaid(loan.getAmountRepaid().add(remainingInstallmentAmount));

            nextLoanRepaymentDetail.setAmountRepaid(nextLoanRepaymentDetail.getInstallmentAmount());


            // decrease amount
            request.setRepaymentAmount(request.getRepaymentAmount().subtract(remainingInstallmentAmount));

            nextLoanRepaymentDetail.setRepaymentTime(currTime);
            nextLoanRepaymentDetail.addPaymentReferenceId(request.getPaymentReferenceId());
            nextLoanRepaymentDetail.setStatus(LoanRepaymentStatus.PAID);
            loan.setNextRepaymentDetail(termToLoanRepaymentDetailMap.get(nextLoanRepaymentDetail.getRepaymentNumber() + 1));
            repayLoan(loan, request, currTime, termToLoanRepaymentDetailMap);
        }
    }

    private void validateLoanRepaymentRequest(LoanRepaymentRequest request, Loan loan, String userId) {
        // validate customer owns loan
        if (!StringUtils.equals(loan.getUserId(), userId)) {
            throw new BadRequestException("User does not have access to this loan!");
        }

        if (request.getRepaymentAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Repayment amount can't be zero");
        }
        if (StringUtils.isBlank(request.getPaymentReferenceId())) {
            throw new BadRequestException("Repayment payment ref id can't be null!");
        }

        if (loan.getStatus() != LoanStatus.APPROVED) {
            throw new BadRequestException(String.format("Repayment can't be done as loan is in %s state!", loan.getStatus()));
        }

        if (request.getRepaymentAmount().scale() > 2) {
            throw new BadRequestException("Please enter amount till two decimal places only!");
        }

        BigDecimal remainingAmount = loan.getAmount().subtract(loan.getAmountRepaid());
        if (request.getRepaymentAmount().compareTo(remainingAmount) > 0) {
            throw new BadRequestException("Repayment amount is greater than the balance repayment amount!");
        }
    }

    public Loan createLoan(LoanCreationRequest request) {
        String userId = requestMetadata.customerId();
        validateCreateLoanRequest(request);
        return loanRepository.save(createLoan(request, userId));
    }

    private void validateCreateLoanRequest(LoanCreationRequest request) {
        int term = request.getTotalTerm();
        if (term <= 0 || term > 1000) {
            throw new BadRequestException("Please select a term between 1 and 1000!");
        }

        BigDecimal loanAmount = request.getAmount();
        if (loanAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Please select a positive loan amount!");
        }

        if (loanAmount.scale() > 2) {
            throw new BadRequestException("Please enter amount till two decimal places only!");
        }

    }

    private Loan createLoan(LoanCreationRequest request, String userId) {
        return Loan.builder()
                .id(UUID.randomUUID().toString())
                .userId(userId)
                .status(LoanStatus.CREATED)
                .loanTerm(request.getTotalTerm())
                .amount(request.getAmount())
                .amountRepaid(BigDecimal.ZERO)
                .frequency(Frequency.WEEKLY)
                .build();
    }

    public List<Loan> getAllLoans() {
        String userId = getUserIdOrElseThrow();
        return loanRepository.findAllByUserId(userId);
    }

    public Loan getLoan(String loanId) {
        if (StringUtils.isBlank(loanId)) {
            throw new BadRequestException("Loan id cant be blank");
        }
        String userId = getUserIdOrElseThrow();
        Optional<Loan> loanOptional = loanRepository.findById(loanId);
        if (loanOptional.isEmpty()) {
            throw new BadRequestException("Invalid loan id");
        }

        Loan loan = loanOptional.get();
        if (!loan.getUserId().equals(userId)) {
            throw new BadRequestException("User is not allowed to access this loan!");
        }
        return loan;
    }

    private String getUserIdOrElseThrow() {
        String userId = requestMetadata.customerId();
        if (StringUtils.isBlank(userId)) {
            throw new ServiceException("Unable to find user id!");
        }
        return userId;
    }
}
