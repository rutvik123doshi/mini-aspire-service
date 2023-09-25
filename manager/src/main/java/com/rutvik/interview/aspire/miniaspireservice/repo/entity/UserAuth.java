package com.rutvik.interview.aspire.miniaspireservice.repo.entity;

import com.rutvik.interview.aspire.miniaspireservice.api.enums.UserAuthStatus;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;


@Entity
@EqualsAndHashCode(callSuper = false)
@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
@Table(name = "user_auth")
public class UserAuth extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Or another appropriate strategy
    private Long id;

    @Column
    private String userId;

    @Column
    private String authToken;

    @Column
    private Integer otp;

    @Column
    @Enumerated(EnumType.STRING)
    private UserAuthStatus status;

    @Column
    private LocalDateTime expiresAt;
}
