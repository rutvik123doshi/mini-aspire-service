package com.rutvik.interview.aspire.miniaspireservice.api.contract;


import com.rutvik.interview.aspire.miniaspireservice.api.enums.LoanStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoanUpdateRequest {
    private LoanStatus status;
}
