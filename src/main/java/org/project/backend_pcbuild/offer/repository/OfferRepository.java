package org.project.backend_pcbuild.offer.repository;

import org.project.backend_pcbuild.offer.model.Offer;
import org.project.backend_pcbuild.pcComponents.model.Component;
import org.project.backend_pcbuild.pcComponents.model.ComponentCondition;
import org.project.backend_pcbuild.pcComponents.model.ComponentType;
import org.project.backend_pcbuild.pcComponents.model.GpuModel;
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

    Long countOffersByIsVisibleTrue();

    @Query("SELECT DISTINCT o.shop.name FROM Offer o")
    List<String> findDistinctShopNames();

    @Query("""
        SELECT i.componentType AS componentType, s.name AS shopName, COUNT(o) AS count 
            FROM Offer o join Component i on o.component= i join Shop s on o.shop = s 
                    where o.isVisible = true 
                            GROUP BY i.componentType, s.name
                    
                            """)
    List<ComponentShopCountProjection> getOfferStatsByComponentAndShop();

    @Query("""
            SELECT i.componentType AS componentType, COUNT(o) AS total 
            FROM Offer o join Component i on o.component = i 
                        where o.isVisible = true 
                                    GROUP BY i.componentType 
                        
                        
                                    """)
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
    List<Offer> findByComponentOrderByBudgetPriceAsc(@Param("componentId") Long componentId, @Param("price") Double price);



    @Query("SELECT o FROM Offer o JOIN o.component comp JOIN comp.graphicsCard g WHERE g.gpuModel = :gpuModel ORDER BY o.price ASC")
    List<Offer> findByGpuModelOrderByPriceAsc(@Param("gpuModel") GpuModel gpuModel);

    @Query("SELECT o FROM Offer o JOIN o.component comp JOIN comp.graphicsCard g WHERE g.gpuModel = :gpuModel and o.price <= :price ORDER BY o.price ASC")
    List<Offer> findByGpuModelOrderByBudgetPriceAsc(@Param("gpuModel") GpuModel gpuModel, @Param("price") Double price);

    @Query("SELECT o FROM Offer o JOIN o.component comp JOIN comp.graphicsCard g WHERE g.gpuModel = :gpuModel AND o.price <= :price ORDER BY o.price ASC")
    List<Offer> findByGpuModelAndPriceLessThanEqualOrderByPriceAsc(@Param("gpuModel") GpuModel gpuModel, @Param("price") Double price, Pageable pageable);


    @Query("""
    SELECT o FROM Offer o
    JOIN o.component c
    LEFT JOIN c.brand b
    LEFT JOIN o.shop s
    WHERE o. isVisible = true
      AND (:componentType IS NULL OR c.componentType = :componentType)
      AND (:brand IS NULL OR LOWER(b.name) = LOWER(:brand))
      AND (:minPrice IS NULL OR o. price >= :minPrice)
      AND (:maxPrice IS NULL OR o.price <= :maxPrice)
      AND (:shopName IS NULL OR LOWER(s.name) = LOWER(:shopName))
      AND (:componentCondition IS NULL OR o.condition = :componentCondition)
      AND (:querySearch IS NULL OR LOWER(o.title) LIKE LOWER(CONCAT('%', :querySearch, '%')))
    """)
    Page<Offer> findOfferByFiltersProd(
            @Param("componentType") ComponentType componentType,
            @Param("brand") String brand,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            @Param("componentCondition") ComponentCondition componentCondition,
            @Param("shopName") String shopName,
            @Param("querySearch") String querySearch,
            Pageable pageable);

//    AND (
//           :querySearch IS NULL
//                    OR FREETEXT(o.title, :querySearch)
//      )

    @Query("""
        SELECT o FROM Offer o
        JOIN o.component c
        LEFT JOIN c.brand b
        LEFT JOIN o.shop s
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
    Page<Offer> findOfferByFiltersDev(
            @Param("componentType") ComponentType componentType,
            @Param("brand") String brand,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            @Param("componentCondition") ComponentCondition componentCondition,
            @Param("shopName") String shopName,
            @Param("querySearch") String querySearch,
            Pageable pageable
    );

    @Query("""
    SELECT o FROM Offer o 
    JOIN o.component comp 
    JOIN comp.processor p 
    WHERE p.benchmark BETWEEN :minBenchmark AND :maxBenchmark 
    AND o.isVisible = true
    ORDER BY o.price ASC
""")
List<Offer> findByCpuBenchmarkRangeOrderByPriceAsc(
    @Param("minBenchmark") double minBenchmark, 
    @Param("maxBenchmark") double maxBenchmark
);

@Query("""
    SELECT o FROM Offer o 
    JOIN o.component comp 
    JOIN comp.processor p 
    WHERE p.benchmark BETWEEN :minBenchmark AND :maxBenchmark 
    AND o.price <= :budget 
    AND o.isVisible = true
    ORDER BY o.price ASC
""")
List<Offer> findByCpuBenchmarkRangeAndBudgetOrderByPriceAsc(
    @Param("minBenchmark") double minBenchmark, 
    @Param("maxBenchmark") double maxBenchmark,
    @Param("budget") Double budget
);

// GPU benchmark queries
@Query("""
    SELECT o FROM Offer o 
    JOIN o.component comp 
    JOIN comp.graphicsCard g 
    WHERE g.benchmark BETWEEN :minBenchmark AND :maxBenchmark 
    AND o.isVisible = true
    ORDER BY o.price ASC
""")
List<Offer> findByGpuBenchmarkRangeOrderByPriceAsc(
    @Param("minBenchmark") double minBenchmark, 
    @Param("maxBenchmark") double maxBenchmark
);

@Query("""
    SELECT o FROM Offer o 
    JOIN o.component comp 
    JOIN comp.graphicsCard g 
    WHERE g.benchmark BETWEEN :minBenchmark AND :maxBenchmark 
    AND o.price <= :budget 
    AND o.isVisible = true
    ORDER BY o.price ASC
""")
List<Offer> findByGpuBenchmarkRangeAndBudgetOrderByPriceAsc(
    @Param("minBenchmark") double minBenchmark, 
    @Param("maxBenchmark") double maxBenchmark,
    @Param("budget") Double budget
);



}
