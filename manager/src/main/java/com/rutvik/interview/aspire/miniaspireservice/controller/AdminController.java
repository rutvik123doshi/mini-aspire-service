package com.rutvik.interview.aspire.miniaspireservice.controller;


import com.rutvik.interview.aspire.miniaspireservice.api.GenericResponse;
import com.rutvik.interview.aspire.miniaspireservice.api.contract.LoanUpdateRequest;
import com.rutvik.interview.aspire.miniaspireservice.api.exception.BadRequestException;
import com.rutvik.interview.aspire.miniaspireservice.service.AdminLoanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

@Slf4j
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminLoanService adminLoanService;

    @Value("${api.admin.token}")
    private String apiAdminToken;

    @PatchMapping("/loans/{loanId}")
    public ResponseEntity<GenericResponse<Object>> updateLoanStatus(
            @PathVariable("loanId") String loanId,
            @RequestBody LoanUpdateRequest loanUpdateRequest,
            @RequestHeader(value = "internalApiToken", required = false, defaultValue = "") String token
    ) {
        try {
            if (StringUtils.equalsIgnoreCase(token, apiAdminToken)) {
                return GenericResponse.successResponse(adminLoanService.updateLoanStatus(loanId, loanUpdateRequest));
            }
            return GenericResponse.sendBadRequestResponse(HttpStatus.FORBIDDEN.value(),
                    "Invalid auth token!", null);
        } catch (BadRequestException e) {
            log.error(">> BadRequestException while udpating loan status: {}", e.getMessage(), e);
            return GenericResponse.sendBadRequestResponse(
                    HttpStatus.BAD_REQUEST.value(),
                    "[User Error] Failed to update loan status:: " + e.getMessage(),
                    Arrays.toString(e.getStackTrace()));
        } catch (Exception e) {
            log.error(">> Exception while updating loan status: {}", e.getMessage(), e);
            return GenericResponse.sendResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "[Internal Error] Failed to update loan status:: " + e.getMessage(),
                    Arrays.toString(e.getStackTrace()),
                    null);
        }

    }
}
