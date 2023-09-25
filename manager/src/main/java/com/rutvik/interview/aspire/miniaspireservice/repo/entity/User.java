package com.rutvik.interview.aspire.miniaspireservice.repo.entity;


import lombok.*;

import javax.persistence.*;

@Entity
@EqualsAndHashCode(callSuper = false)
@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
@Table(name = "users")
public class User extends BaseEntity {

    @Id
    private String id;

    @Column
    private String phoneNumber;

}
