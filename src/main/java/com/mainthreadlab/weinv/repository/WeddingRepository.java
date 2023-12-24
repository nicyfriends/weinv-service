package com.mainthreadlab.weinv.repository;

import com.mainthreadlab.weinv.model.User;
import com.mainthreadlab.weinv.model.Wedding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Date;

@Repository
public interface WeddingRepository extends JpaRepository<Wedding, Integer>, JpaSpecificationExecutor<Wedding> {

    Wedding findByUuidAndDateGreaterThanEqual(String uuid, Date now);

    Wedding findByUuid(String uuid);

    Wedding findByResponsible(User user);
}
