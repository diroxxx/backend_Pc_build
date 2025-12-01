package org.example.backend_pcbuild.repository;

import org.example.backend_pcbuild.models.Component;
import org.example.backend_pcbuild.models.GpuModel;
import org.example.backend_pcbuild.models.Offer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OfferRepository extends JpaRepository<Offer, Long> {

    @Query("SELECT o FROM Offer o WHERE TRIM(LOWER(o.websiteUrl)) = TRIM(LOWER(:url))")
    Optional<Offer> findByWebsiteUrl(@Param("url") String url);

    @Query("SELECT COUNT(o) FROM Offer o JOIN o.offerShopOfferUpdates osou WHERE osou.shopOfferUpdate.id = :shopOfferUpdateId")
    int countByShopOfferUpdates_ShopOfferUpdate_Id(@Param("shopOfferUpdateId") Long shopOfferUpdateId);


    @Query("""
        SELECT o FROM Offer o
        WHERE LOWER(TRIM(o.shop.name)) = LOWER(TRIM(:shopName))
          AND LOWER(TRIM(o.websiteUrl)) = LOWER(TRIM(:url))
    """)
    Optional<Offer> findByShopNameAndWebsiteUrlIgnoreCaseTrim(
            @Param("shopName") String shopName,
            @Param("url") String url
    );

    List<Offer> findAllByWebsiteUrlIn(List<String> urls);

    void deleteByWebsiteUrlIn(List<String> urls);

    @Query("""
    SELECT o.component.componentType as type, COUNT(o) as count
    FROM Offer o
    WHERE o.shop.name = :shopName
      AND o.isVisible = true
      AND o.component IS NOT NULL
    GROUP BY o.component.componentType
""")
    List<OfferTypeCountProjection> countVisibleOffersByShop(@Param("shopName") String shopName);

    interface OfferTypeCountProjection {
        String getType();
        Long getCount();
    }

    Long countOffersByIsVisibleTrue();

    @Query("SELECT DISTINCT o.shop.name FROM Offer o")
    List<String> findDistinctShopNames();

    @Query("SELECT i.componentType AS componentType, s.name AS shopName, COUNT(o) AS count " +
            "FROM Offer o join Component i on o.component= i join Shop s on o.shop = s GROUP BY i.componentType, s.name")
    List<ComponentShopCountProjection> getOfferStatsByComponentAndShop();

    @Query("SELECT i.componentType AS componentType, COUNT(o) AS total " +
            "FROM Offer o join Component i on o.component = i GROUP BY i.componentType")
    List<ComponentTotalProjection> getOfferStatsTotal();


    public interface ComponentShopCountProjection {
        String getComponentType();
        String getShopName();
        Long getCount();
    }

    public interface ComponentTotalProjection {
        String getComponentType();
        Long getTotal();
    }

    @Query("SELECT o FROM Offer o " +
            "JOIN o.component comp " +
            "JOIN comp.graphicsCard g " +
            "WHERE g.gpuModel = :gpuModel " +
            "ORDER BY o.price ASC")
    List<Offer> findTopByGpuModelOrderByPriceAsc(@Param("gpuModel") GpuModel gpuModel);


    Optional<Offer> findFirstByComponentOrderByPriceAsc(Component component);

    @Query(value = "SELECT * FROM offer o WHERE o.component_id = :componentId ORDER BY o.price ASC LIMIT 1", nativeQuery = true)
    Optional<Offer> findCheapestNative(@Param("componentId") Long componentId);



}
