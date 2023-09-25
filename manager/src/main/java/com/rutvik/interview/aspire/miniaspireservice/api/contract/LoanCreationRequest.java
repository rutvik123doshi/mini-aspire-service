package com.rutvik.interview.aspire.miniaspireservice.api.contract;


import com.rutvik.interview.aspire.miniaspireservice.api.enums.Frequency;
import com.rutvik.interview.aspire.miniaspireservice.api.enums.LoanStatus;
import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoanCreationRequest {

    private String id;
    @NonNull
    private BigDecimal amount;

    @NonNull
    private Integer totalTerm;

    private LoanStatus loanStatus;
    private Frequency frequency;

}
