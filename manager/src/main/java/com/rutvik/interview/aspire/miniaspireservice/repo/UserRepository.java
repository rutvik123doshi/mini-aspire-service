package com.rutvik.interview.aspire.miniaspireservice.repo;

import com.rutvik.interview.aspire.miniaspireservice.repo.entity.User;
import com.rutvik.interview.aspire.miniaspireservice.repo.entity.UserAuth;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends CrudRepository<User, String> {
    Optional<User> findByPhoneNumber(String phoneNumber);

}
