package com.mainthreadlab.weinv.model;


import com.mainthreadlab.weinv.model.base.BaseEntity;
import com.mainthreadlab.weinv.model.enums.EventType;
import com.mainthreadlab.weinv.model.enums.Language;
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
    //@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_seq")
    //@SequenceGenerator(name = "user_seq")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true, nullable = false)
    private String uuid;

    @Column(unique = true, nullable = false, length = 20)
    private String username;

    //@Column(nullable = false)
    private String firstName;

    //@Column(nullable = false)
    private String lastName;

    /** couple information */
    private String wife;
    private String husband;
    private boolean couple;

    private String email;
    private String phoneNumber;

    //"role1,roles2..."
    @Column(nullable = false)
    private String roles;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Language language;

    @Temporal(TemporalType.TIMESTAMP)
    private Date lastLoginDate;

    private Double price;

    //for responsible
    @Enumerated(EnumType.STRING)
    private EventType eventType;

    private Integer totalInvitations;

    private boolean enabled;

}
