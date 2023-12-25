package com.mainthreadlab.weinv.repository;

import com.mainthreadlab.weinv.model.User;
import com.mainthreadlab.weinv.model.Wedding;
import com.mainthreadlab.weinv.model.WeddingGuest;
import com.mainthreadlab.weinv.model.base.WeddingGuestId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WeddingGuestRepository extends JpaRepository<WeddingGuest, WeddingGuestId>, JpaSpecificationExecutor<WeddingGuest> {


    List<WeddingGuest> findByWedding(Wedding wedding);

    List<WeddingGuest> findByWeddingOrderByGuest_FirstNameAscGuest_LastNameAsc(Wedding wedding);

    WeddingGuest findByWeddingAndGuest(Wedding wedding, User guest);

    WeddingGuest findByGuest(User user);

    void deleteByWeddingAndGuest(Wedding wedding, User user);
}
