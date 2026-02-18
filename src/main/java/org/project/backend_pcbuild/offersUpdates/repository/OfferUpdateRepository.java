package org.project.backend_pcbuild.offersUpdates.repository;

import org.project.backend_pcbuild.offersUpdates.dto.OfferUpdateStatsDTO;
import org.project.backend_pcbuild.offersUpdates.model.OfferUpdate;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OfferUpdateRepository extends JpaRepository<OfferUpdate, Long> {

    Optional<OfferUpdate> findById(Long id);

    @Query("""
    SELECT new org.project.backend_pcbuild.offersUpdates.dto.OfferUpdateStatsDTO(
        COUNT(o.id),
        u.startedAt
    )
    FROM OfferUpdate u
    JOIN u.shopOfferUpdates sou
    JOIN sou.offerShopOfferUpdates osou
    JOIN osou.offer o
    WHERE u.startedAt >= :thirtyDaysAgo and o.isVisible = true
    GROUP BY u.startedAt
    ORDER BY u.startedAt
""")
    List<OfferUpdateStatsDTO> findOfferStatsSince(@Param("thirtyDaysAgo") LocalDateTime thirtyDaysAgo);



//    @EntityGraph(attributePaths = {
//            "shopOfferUpdates",
//            "shopOfferUpdates.shop",
//            "shopOfferUpdates.offerShopOfferUpdates",
//            "shopOfferUpdates.offerShopOfferUpdates.offer",
//            "shopOfferUpdates.offerShopOfferUpdates.offer.component",
//            "shopOfferUpdates.offerShopOfferUpdates.offer.component.componentType"
//    })
//    List<OfferUpdate> findAll();


    interface OfferUpdateShopsOffersAmountStatsProjection {
        String getShopName();
        Long getOfferCount();
    }

    @Query("""
    SELECT s.name AS shopName, COUNT(o.id) AS offerCount
    FROM Offer o
    JOIN Shop s ON s.id = o.shop.id
    where o.isVisible = true
    GROUP BY s.name
    ORDER BY offerCount DESC
""")
    List<OfferUpdateShopsOffersAmountStatsProjection> findOfferStatsByShop();

    Optional<OfferUpdate> findFirstByOrderByFinishedAtDesc();

}

