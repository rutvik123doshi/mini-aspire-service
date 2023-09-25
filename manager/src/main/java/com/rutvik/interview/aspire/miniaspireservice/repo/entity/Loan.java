package com.rutvik.interview.aspire.miniaspireservice.repo.entity;


import com.rutvik.interview.aspire.miniaspireservice.api.enums.Frequency;
import com.rutvik.interview.aspire.miniaspireservice.api.enums.LoanStatus;
import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.List;

@Entity
@EqualsAndHashCode(callSuper = false)
@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
@Table(name = "loans")
public class Loan extends BaseEntity{

    @Id
    private String id;

    @Column
    private String userId;

    @Column
    @Enumerated(EnumType.STRING)
    private LoanStatus status;

    @Column
    private Integer loanTerm;

    @Column
    private BigDecimal amount;


    @Column
    private BigDecimal amountRepaid;

    @ManyToOne
    @JoinColumn(name = "next_loan_repayment_id")
    private LoanRepaymentDetail nextRepaymentDetail;

    @Column
    @Enumerated(EnumType.STRING)
    private Frequency frequency;

    @OneToMany(mappedBy = "loan", cascade = CascadeType.ALL, orphanRemoval=false)
    private List<LoanRepaymentDetail> repaymentDetails;
}
