package org.example.backend_pcbuild.Admin.repository;

import org.example.backend_pcbuild.models.OfferUpdate;
import org.example.backend_pcbuild.models.ShopOfferUpdate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface OfferUpdateRepository extends JpaRepository<OfferUpdate, Long> {

    Optional<OfferUpdate> findById(Long id);
//    @Query("""
//        SELECT ou
//        FROM OfferUpdate ou
//        JOIN ou.shopOfferUpdates sou
//        JOIN sou.shop s
//        JOIN sou.offerShopOfferUpdates osou
//        JOIN osou.offer o
//        WHERE ou.id = :id
//          AND LOWER(s.name) = LOWER(:shopName)
//          AND  o.isVisible = :isVisible
//        """)
//    Optional<OfferUpdate> findByIdAndShopName(Long id, String shopName, boolean isVisible);

//    Optional<ShopOfferUpdate> findByOfferUpdate_IdAndShop_NameAndOfferUpdate_IsVisible(Long id, String shopName, boolean visible);

}
