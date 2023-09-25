package com.rutvik.interview.aspire.miniaspireservice.repo;

import com.rutvik.interview.aspire.miniaspireservice.api.enums.UserAuthStatus;
import com.rutvik.interview.aspire.miniaspireservice.repo.entity.UserAuth;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserAuthRepository extends CrudRepository<UserAuth, String> {

    List<UserAuth> findAllByUserIdAndExpiresAtGreaterThan(String phoneNumber, LocalDateTime now);

    List<UserAuth> findAllByUserIdAndStatus(String phoneNumber, UserAuthStatus status);

    Optional<UserAuth> findByAuthToken(String authToken);

}
