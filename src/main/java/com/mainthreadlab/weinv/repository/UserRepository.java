package com.mainthreadlab.weinv.repository;

import com.mainthreadlab.weinv.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Integer>, JpaSpecificationExecutor<User> {

    User findByUsernameAndEnabledFalse(String username);

    User findByUsernameAndEnabledTrue(String username);

    User findByUsername(String username);

    User findByUuidAndEnabledTrue(String uuid);
}
