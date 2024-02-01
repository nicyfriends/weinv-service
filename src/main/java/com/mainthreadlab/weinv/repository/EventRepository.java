package com.mainthreadlab.weinv.repository;

import com.mainthreadlab.weinv.model.User;
import com.mainthreadlab.weinv.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Date;

@Repository
public interface EventRepository extends JpaRepository<Event, Integer>, JpaSpecificationExecutor<Event> {

    Event findByUuidAndDateGreaterThanEqual(String uuid, Date now);

    Event findByUuid(String uuid);

    Event findByResponsible(User user);
}
