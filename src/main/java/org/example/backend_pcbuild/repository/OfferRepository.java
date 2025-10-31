package org.example.backend_pcbuild.repository;

import org.example.backend_pcbuild.models.Offer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OfferRepository extends JpaRepository<Offer, Long> {
    Optional<Offer> findByWebsiteUrl(String website_url);

    List<Offer> findAllByWebsiteUrlIn(List<String> urls);

    void deleteByWebsiteUrlIn(List<String> urls);

    List<Offer> findByShop_NameIgnoreCaseAndIsVisibleTrue(String shopName);


    @Query("""
    SELECT o.item.itemType as type, COUNT(o) as count
    FROM Offer o
    WHERE o.shop.name = :shopName
      AND o.isVisible = true
      AND o.item IS NOT NULL
    GROUP BY o.item.itemType
""")
    List<OfferTypeCountProjection> countVisibleOffersByShop(@Param("shopName") String shopName);

    public interface OfferTypeCountProjection {
        String getType();
        Long getCount();
    }

    Long countOffersByIsVisibleTrue();

}
