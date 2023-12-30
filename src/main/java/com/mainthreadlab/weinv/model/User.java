package com.mainthreadlab.weinv.model;


import com.mainthreadlab.weinv.model.enums.EventType;
import com.mainthreadlab.weinv.model.enums.Language;
import com.mainthreadlab.weinv.model.base.BaseEntity;
import lombok.*;

import javax.persistence.*;
import java.util.Date;


@Entity
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "ws_users")
public class User extends BaseEntity {

    @Id
//    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_seq")
//    @SequenceGenerator(name = "user_seq")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true, nullable = false)
    private String uuid;

    @Column(unique = true, nullable = false, length = 20)
    private String username;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    private String email;

    private String phoneNumber;

    /**
     * useless in this server
     * it's necessary only in authorization-server
     */
//    @Column(nullable = false)
//    private String password;

    @Column(nullable = false)
    private String roles;            // "role1,roles2..."

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Language language;

    @Temporal(TemporalType.TIMESTAMP)
    private Date lastLoginDate;

    private Double price;

    @Enumerated(EnumType.STRING)
    private EventType eventType;  // for responsible

    private boolean enabled = true;

}
