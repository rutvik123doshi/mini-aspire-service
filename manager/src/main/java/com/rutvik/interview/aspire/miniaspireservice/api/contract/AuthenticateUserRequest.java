package com.rutvik.interview.aspire.miniaspireservice.api.contract;


import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticateUserRequest {
    private String phoneNumber;
    private Integer otp;
    private String userId;
}
