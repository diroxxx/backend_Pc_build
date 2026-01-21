package org.project.backend_pcbuild.offersUpdates.repository;

import org.project.backend_pcbuild.offersUpdates.model.ShopOfferUpdate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ShopOfferUpdateRepository extends JpaRepository<ShopOfferUpdate, Long> {

    @Query("SELECT s FROM ShopOfferUpdate s WHERE s.offerUpdate.id = :offerUpdateId AND LOWER(s.shop.name) = LOWER(:shopName)")
    Optional<ShopOfferUpdate> findFirstByOfferUpdate_IdAndShop_NameIgnoreCase(@Param("offerUpdateId") Long offerUpdateId,
                                                                              @Param("shopName") String shopName);
}


