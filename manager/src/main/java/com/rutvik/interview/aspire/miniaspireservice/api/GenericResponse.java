package com.rutvik.interview.aspire.miniaspireservice.api;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Builder
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GenericResponse<T> {

    private Integer status;
    private String error;
    private String message;
    private T data;

    public static <T> ResponseEntity<GenericResponse<T>> successResponse(T data) {
        return ResponseEntity.status(HttpStatus.OK).body(GenericResponse.<T>builder().data(data).build());
    }

    public static <T> ResponseEntity<GenericResponse<T>> sendBadRequestResponse(int status, String error, String message) {
        return sendResponse(status, HttpStatus.BAD_REQUEST, error, message, null);
    }

    public static <T> ResponseEntity<GenericResponse<T>> sendResponse(int status, HttpStatus httpStatus, String error, String message, T data) {
        return ResponseEntity.status(httpStatus)
                .body(GenericResponse.<T>builder()
                        .status(status)
                        .error(error)
                        .message(message)
                        .data(data)
                        .build());
    }

    @Override
    public String toString() {
        return "Response{" + "status=" + status + ", error='" + error + '\'' + ", message='" + message + '\'' + ", data=" + data + '}';
    }

//    public ResponseEntity<Response<T>> sendBadRequestResponse(String error, String message) {
//        return sendErrorResponse(HttpStatus.BAD_REQUEST, error, message);
//    }
}

