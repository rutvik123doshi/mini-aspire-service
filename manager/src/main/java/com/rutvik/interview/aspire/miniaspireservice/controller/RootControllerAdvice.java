package com.rutvik.interview.aspire.miniaspireservice.controller;

import com.rutvik.interview.aspire.miniaspireservice.api.GenericResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.validation.ConstraintViolationException;
import java.util.Arrays;

@ControllerAdvice
public class RootControllerAdvice {

    @ExceptionHandler({ConstraintViolationException.class})
    public ResponseEntity<?> handleConstraintViolationException(ConstraintViolationException ex) {
        return GenericResponse.sendBadRequestResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request: " + ex.getMessage(),
                Arrays.toString(ex.getStackTrace())
        );
    }

    @ExceptionHandler({Exception.class})
    public ResponseEntity<?> handleException(Exception ex) {
        return GenericResponse.sendBadRequestResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server error: " + ex.getMessage(),
                Arrays.toString(ex.getStackTrace())
        );
    }
}
