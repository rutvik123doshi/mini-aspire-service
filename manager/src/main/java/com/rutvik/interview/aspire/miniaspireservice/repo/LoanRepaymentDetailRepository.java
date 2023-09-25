package com.rutvik.interview.aspire.miniaspireservice.repo;

import com.rutvik.interview.aspire.miniaspireservice.repo.entity.Loan;
import com.rutvik.interview.aspire.miniaspireservice.repo.entity.LoanRepaymentDetail;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface LoanRepaymentDetailRepository extends CrudRepository<LoanRepaymentDetail, String> {

    List<Loan> findAllByLoanId(String userId);
}
