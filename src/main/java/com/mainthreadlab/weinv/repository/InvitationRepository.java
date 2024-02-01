package com.mainthreadlab.weinv.repository;

import com.mainthreadlab.weinv.model.User;
import com.mainthreadlab.weinv.model.Event;
import com.mainthreadlab.weinv.model.Invitation;
import com.mainthreadlab.weinv.model.base.InvitationId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InvitationRepository extends JpaRepository<Invitation, InvitationId>, JpaSpecificationExecutor<Invitation> {


    List<Invitation> findByWedding(Event event);

    List<Invitation> findByWeddingOrderByGuest_FirstNameAscGuest_LastNameAsc(Event event);

    Invitation findByWeddingAndGuest(Event event, User guest);

    Invitation findByGuest(User user);

}
