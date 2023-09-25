package com.rutvik.interview.aspire.miniaspireservice.repo;

import com.rutvik.interview.aspire.miniaspireservice.repo.entity.Loan;
import com.rutvik.interview.aspire.miniaspireservice.repo.entity.UserAuth;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface LoanRepository extends CrudRepository<Loan, String> {

    List<Loan> findAllByUserId(String userId);
}
