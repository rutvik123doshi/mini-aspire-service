package com.rutvik.interview.aspire.miniaspireservice.api.contract;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticateUserResponse {
    private Integer otp;
    private String authToken;
}
