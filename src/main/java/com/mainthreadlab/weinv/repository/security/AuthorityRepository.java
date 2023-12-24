package com.mainthreadlab.weinv.repository.security;

import com.mainthreadlab.weinv.model.security.Authority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthorityRepository extends JpaRepository<Authority, Integer> {

    Authority findByName(String name);
}
