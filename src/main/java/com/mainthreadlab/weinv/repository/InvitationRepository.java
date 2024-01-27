package com.mainthreadlab.weinv.repository;

import com.mainthreadlab.weinv.model.User;
import com.mainthreadlab.weinv.model.Wedding;
import com.mainthreadlab.weinv.model.Invitation;
import com.mainthreadlab.weinv.model.base.InvitationId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InvitationRepository extends JpaRepository<Invitation, InvitationId>, JpaSpecificationExecutor<Invitation> {


    List<Invitation> findByWedding(Wedding wedding);

    List<Invitation> findByWeddingOrderByGuest_FirstNameAscGuest_LastNameAsc(Wedding wedding);

    Invitation findByWeddingAndGuest(Wedding wedding, User guest);

    Invitation findByGuest(User user);

    void deleteByWeddingAndGuest(Wedding wedding, User user);
}
