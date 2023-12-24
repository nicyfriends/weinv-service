package com.mainthreadlab.weinv.model.security;


import com.mainthreadlab.weinv.model.base.BaseEntity;
import lombok.Data;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

@Data
@Entity
@Table(name = "users")
public class UserAuth extends BaseEntity {

    @Id
//    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "users_seq")
//    @SequenceGenerator(name = "users_seq")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "email")
    private String email;

    @Column(name = "username", unique = true, nullable = false)
    private String username;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "account_expired", columnDefinition = "boolean default false")
    private boolean accountExpired = false;

    @Column(name = "account_locked", columnDefinition = "boolean default false")
    private boolean accountLocked = false;

    @Column(name = "credentials_expired", columnDefinition = "boolean default false")
    private boolean credentialsExpired = false;

    @Column(name = "enabled", nullable = false, columnDefinition = "boolean default true")
    private boolean enabled = true;

    @ManyToMany(cascade = {CascadeType.ALL}, fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_authority",
            joinColumns = {@JoinColumn(name = "username", referencedColumnName = "username")},
            inverseJoinColumns = {@JoinColumn(name = "authority_name", referencedColumnName = "name")}
    )
    private Collection<Authority> authorities = new ArrayList<>();


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserAuth user = (UserAuth) o;
        return email.equals(user.email) && username.equals(user.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email, username);
    }
}