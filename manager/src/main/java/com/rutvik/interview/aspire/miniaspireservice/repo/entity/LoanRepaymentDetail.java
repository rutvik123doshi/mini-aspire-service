package com.rutvik.interview.aspire.miniaspireservice.repo.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.rutvik.interview.aspire.miniaspireservice.api.enums.LoanRepaymentStatus;
import lombok.*;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Entity
@EqualsAndHashCode(callSuper = false)
@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
@Table(name = "loan_repayments")
public class LoanRepaymentDetail extends BaseEntity {

    @Id
    private String id;

    @ManyToOne
    @JoinColumn(name = "loan_id")
    @JsonIgnore
    private Loan loan;

    @Column
    @Enumerated(EnumType.STRING)
    private LoanRepaymentStatus status;

    // Add a reference to the next LoanRepaymentDetail entity
    @JsonIgnore
    @OneToOne(mappedBy = "nextRepaymentDetail", cascade = CascadeType.ALL)
    private Loan nextLoanRepaymentDetail;


    @Column
    private Integer repaymentNumber;

    @Column(name = "payment_reference_ids")
    private String paymentReferenceIds;


    // When setting the list:
    public void setPaymentReferenceIds(List<String> paymentReferenceIds) {
        this.paymentReferenceIds = String.join(",", paymentReferenceIds);
    }

    // When getting the list:
    public List<String> getPaymentReferenceIds() {
        return Arrays.asList(paymentReferenceIds.split(","));
    }

    public void addPaymentReferenceId(String newId) {
        if (StringUtils.isBlank(this.paymentReferenceIds)) {
            this.paymentReferenceIds = newId;
        } else {
            this.paymentReferenceIds = this.paymentReferenceIds + "," + newId;
        }

    }

    @Column
    private BigDecimal amountRepaid;

    @Column
    private BigDecimal installmentAmount;

    @Column
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate scheduledAt;

    @Column
    @JsonIgnore
    private LocalDateTime repaymentTime;
}
