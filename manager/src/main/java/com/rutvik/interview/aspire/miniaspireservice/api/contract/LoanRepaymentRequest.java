package com.rutvik.interview.aspire.miniaspireservice.api.contract;


import lombok.*;

import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoanRepaymentRequest {

    @NonNull
    private BigDecimal repaymentAmount;

    @NotBlank
    private String paymentReferenceId;
}
