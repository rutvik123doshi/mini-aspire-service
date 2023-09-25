package com.rutvik.interview.aspire.miniaspireservice.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.rutvik.interview.aspire.miniaspireservice.api.GenericResponse;
import com.rutvik.interview.aspire.miniaspireservice.api.contract.LoanCreationRequest;
import com.rutvik.interview.aspire.miniaspireservice.api.contract.LoanRepaymentRequest;
import com.rutvik.interview.aspire.miniaspireservice.api.exception.BadRequestException;
import com.rutvik.interview.aspire.miniaspireservice.service.LoansService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Arrays;

@Slf4j
@RestController
@RequestMapping("/loans")
@RequiredArgsConstructor
@Validated
public class LoanRequestController {

    private final LoansService loansService;
    private final ObjectMapper objectMapper;

    @PostMapping("/{loanId}/repay")
    public ResponseEntity<?> repayLoan(
            @PathVariable("loanId") String loanId,
            @Valid @RequestBody LoanRepaymentRequest loanRepaymentRequest
    ) {
        try {
            log.info(">> Received request for loan repayment: {}", objectMapper.writeValueAsString(loanRepaymentRequest));
            return GenericResponse.successResponse(loansService.repayLoan(loanId, loanRepaymentRequest));

        } catch (BadRequestException e) {
            log.error(">> BadRequestException while creating loan: {}", e.getMessage(), e);
            return GenericResponse.sendBadRequestResponse(
                    HttpStatus.BAD_REQUEST.value(),
                    "[User Error] Failed to create loan:: " + e.getMessage(),
                    Arrays.toString(e.getStackTrace()));
        } catch (Exception e) {
            log.error(">> Exception while creating loan: {}", e.getMessage(), e);
            return GenericResponse.sendResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "[Internal Error] Failed to create loan:: " + e.getMessage(),
                    Arrays.toString(e.getStackTrace()),
                    null);
        }
    }

    @PostMapping("/")
    public ResponseEntity<?> createLoan(
            @RequestBody LoanCreationRequest loanCreationRequest
    ) {
        try {
            log.info(">> Received request for loan creation: {}", objectMapper.writeValueAsString(loanCreationRequest));
            return GenericResponse.successResponse(loansService.createLoan(loanCreationRequest));
        } catch (BadRequestException e) {
            log.error(">> BadRequestException while creating loan: {}", e.getMessage(), e);
            return GenericResponse.sendBadRequestResponse(
                    HttpStatus.BAD_REQUEST.value(),
                    "[User Error] Failed to create loan:: " + e.getMessage(),
                    Arrays.toString(e.getStackTrace()));
        } catch (Exception e) {
            log.error(">> Exception while creating loan: {}", e.getMessage(), e);
            return GenericResponse.sendResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "[Internal Error] Failed to create loan:: " + e.getMessage(),
                    Arrays.toString(e.getStackTrace()),
                    null);
        }
    }

    @GetMapping("/")
    public ResponseEntity<?> getCustomerLoans() {
        try {
            return GenericResponse.successResponse(loansService.getAllLoans());
        } catch (BadRequestException e) {
            log.error(">> BadRequestException while getting loans: {}", e.getMessage(), e);
            return GenericResponse.sendBadRequestResponse(
                    HttpStatus.BAD_REQUEST.value(),
                    "[User Error] Failed to getting all loans:: " + e.getMessage(),
                    Arrays.toString(e.getStackTrace()));
        } catch (Exception e) {
            log.error(">> Exception while getting all loans: {}", e.getMessage(), e);
            return GenericResponse.sendResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "[Internal Error] Failed getting all loans:: " + e.getMessage(),
                    Arrays.toString(e.getStackTrace()),
                    null);
        }
    }


    @GetMapping("/{loanId}")
    public ResponseEntity<?> getLoan(@PathVariable("loanId") String loanId) {
        try {
            return GenericResponse.successResponse(loansService.getLoan(loanId));
        } catch (BadRequestException e) {
            log.error(">> BadRequestException while getting loan: {}", e.getMessage(), e);
            return GenericResponse.sendBadRequestResponse(
                    HttpStatus.BAD_REQUEST.value(),
                    "[User Error] Failed to getting loan:: " + e.getMessage(),
                    Arrays.toString(e.getStackTrace()));
        } catch (Exception e) {
            log.error(">> Exception while getting loan: {}", e.getMessage(), e);
            return GenericResponse.sendResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "[Internal Error] Failed getting loan:: " + e.getMessage(),
                    Arrays.toString(e.getStackTrace()),
                    null);
        }
    }

}
