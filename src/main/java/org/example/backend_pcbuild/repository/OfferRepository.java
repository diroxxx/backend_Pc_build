package org.example.backend_pcbuild.repository;

import org.example.backend_pcbuild.models.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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


    @Query("SELECT o FROM Offer o WHERE o.component = :component ORDER BY o.price ASC")
    List<Offer> findByComponentOrderByPriceAsc(@Param("component") Component component);

    @Query("SELECT o FROM Offer o JOIN o.component comp WHERE comp.id = :componentId and o.price <= :price ORDER BY o.price ASC")
    List<Offer> findByComponentOrderByBudgetPriceAsc(@Param("componentId") Long componentId, @Param("price") double price);



    @Query("SELECT o FROM Offer o JOIN o.component comp JOIN comp.graphicsCard g WHERE g.gpuModel = :gpuModel ORDER BY o.price ASC")
    List<Offer> findByGpuModelOrderByPriceAsc(@Param("gpuModel") GpuModel gpuModel);

    @Query("SELECT o FROM Offer o JOIN o.component comp JOIN comp.graphicsCard g WHERE g.gpuModel = :gpuModel and o.price <= :price ORDER BY o.price ASC")
    List<Offer> findByGpuModelOrderByBudgetPriceAsc(@Param("gpuModel") GpuModel gpuModel, @Param("price") double price);

    @Query("SELECT o FROM Offer o JOIN o.component comp JOIN comp.graphicsCard g WHERE g.gpuModel = :gpuModel AND o.price <= :price ORDER BY o.price ASC")
    List<Offer> findByGpuModelAndPriceLessThanEqualOrderByPriceAsc(@Param("gpuModel") GpuModel gpuModel, @Param("price") double price, Pageable pageable);


//    @Query("""
//        select o from Offer o
//        inner join Component c on o.component.id = c.id
//        inner join Brand b on b.id = c.brand.id
//        inner join Shop s on o.shop.id = s.id
//        where o.isVisible = true
//            and (:componentType is null or c.componentType = :componentType)
//            and (:brand is null or LOWER(b) = LOWER(:brand))
//            and (:minPrice is null or o.price >= :minPrice)
//            and (:maxPrice is null or o.price <= :maxPrice)
//            and (:shopName is null or LOWER(s.name) = LOWER(:shopName))
//            and (:componentCondition is null or o.condition = :componentCondition)
//
//
//
//
//""")
//    Page<Offer> findOfferByFiltersProd(
//            @Param("componentType")ComponentType componentType,
//            @Param("brand") String brand,
//            @Param("minPrice") Double minPrice,
//            @Param("maxPrice") Double MaxPrice,
//            @Param("componentCondition")ComponentCondition componentCondition,
//            @Param("shopName")String shopName,
//            @Param("querySearch")String querySearch,
//            Pageable pageable
//            );



    // OfferRepository.java (fragment)
    @Query("""
    SELECT o FROM Offer o
    JOIN Component c ON o.component.id = c.id
    LEFT JOIN Brand b ON c.brand.id = b.id
    LEFT JOIN Shop s ON o.shop.id = s.id
    WHERE o.isVisible = true 
      AND (:componentType IS NULL OR c.componentType = :componentType)
      AND (:brand IS NULL OR LOWER(b.name) = LOWER(:brand))
      AND (:minPrice IS NULL OR o.price >= :minPrice)
      AND (:maxPrice IS NULL OR o.price <= :maxPrice)
      AND (:shopName IS NULL OR LOWER(s.name) = LOWER(:shopName))
      AND (:componentCondition IS NULL OR o.condition = :componentCondition)
     AND (:querySearch IS NULL OR (
                 LOWER(o.title) LIKE CONCAT('%', LOWER(:querySearch), '%')
           ))
""")
    Page<Offer> findOfferByFiltersProd(
            @Param("componentType") ComponentType componentType,
            @Param("brand") String brand,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            @Param("componentCondition") ComponentCondition componentCondition,
            @Param("shopName") String shopName,
            @Param("querySearch") String querySearch,
            Pageable pageable
    );
//    AND (
//           :querySearch IS NULL
//                    OR FREETEXT(o.title, :querySearch)
//      )

    @Query("""
        select o from Offer o
        inner join Component c on o.component = c
        inner join Brand b on b = c.brand
        inner join Shop s on o.shop = s
        where o.isVisible = true 
            and (:componentType is null or c.componentType = :componentType) 
            and (:brand is null or LOWER(b) = LOWER(:brand))
            and (:minPrice is null or o.price >= :minPrice)
            and (:maxPrice is null or o.price <= :maxPrice)
            and (:shopName is null or LOWER(s.name) = LOWER(:shopName))
            and (:componentCondition is null or o.condition = :componentCondition)
             and (:querySearch is null or (
                         lower(o.title) like lower(concat('%', :querySearch, '%'))
                    ))
""")
    Page<Offer> findOfferByFiltersDev(
            @Param("componentType") ComponentType componentType,
            @Param("brand") String brand,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double MaxPrice,
            @Param("componentCondition") ComponentCondition componentCondition,
            @Param("shopName")String shopName,
            @Param("querySearch")String querySearch,
            Pageable pageable
    );

}
