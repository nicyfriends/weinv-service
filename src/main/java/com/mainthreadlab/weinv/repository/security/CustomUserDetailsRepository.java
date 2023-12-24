package com.mainthreadlab.weinv.repository.security;

import com.mainthreadlab.weinv.model.security.UserAuth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomUserDetailsRepository extends JpaRepository<UserAuth, Integer> {

    UserAuth findByUsername(String username);
}
