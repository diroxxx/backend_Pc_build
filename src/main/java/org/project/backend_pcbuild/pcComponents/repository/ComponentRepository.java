package org.project.backend_pcbuild.pcComponents.repository;

import org.project.backend_pcbuild.offer.model.Brand;
import org.project.backend_pcbuild.pcComponents.dto.ComponentMinMaxValueDto;
import org.project.backend_pcbuild.pcComponents.dto.ComponentsAmountPc;
import org.project.backend_pcbuild.pcComponents.model.Component;
import org.project.backend_pcbuild.pcComponents.model.ComponentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ComponentRepository extends JpaRepository<Component, Long>, JpaSpecificationExecutor<Component> {


    @Query("SELECT DISTINCT i.name FROM Brand i WHERE i.name IS NOT NULL AND i.name != ''")
    List<String> findDistinctBrands();

    Optional<Component> findByBrandAndModelIgnoreCase(Brand brand, String model);

    List<Component> findAllByComponentType(ComponentType componentType);

    @Query("select p.model from Processor p where p.benchmark is not null order by p.benchmark desc")
    List<String> findProcessorModelsOrderedByBenchmarkDesc();


    @Query(value = """
        SELECT componentType, model, amount
        FROM (
            SELECT
                c.component_type as componentType,
                c.model,
                COUNT(*) as amount,
                ROW_NUMBER() OVER (
                    PARTITION BY c. component_type 
                    ORDER BY COUNT(*) DESC
                ) as rn
            FROM component c
            JOIN offer o ON c. id = o.component_id
            JOIN computer_offer co ON o.id = co.offer
            WHERE co.created_at >= :startDate AND co.created_at < :endDate
            GROUP BY c.component_type, c.model
        ) ranked
        WHERE rn = 1
        ORDER BY amount DESC
        """, nativeQuery = true)
    List<ComponentsAmountPc> componentStatsPcBetweenH2(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );


    @Query(value = """
        SELECT componentType, model, amount
        FROM (
            SELECT
                c.component_type AS componentType,
                c.model AS model,
                COUNT(*) AS amount,
                ROW_NUMBER() OVER (
                    PARTITION BY c.component_type 
                    ORDER BY COUNT(*) DESC
                ) AS rn
            FROM component c
            INNER JOIN offer o ON c.id = o.component_id
            INNER JOIN computer_offer co ON o.id = co.offer
            WHERE co.created_at >= :startDate 
              AND co.created_at < :endDate
            GROUP BY c. component_type, c.model
        ) AS ranked
        WHERE rn = 1
        ORDER BY amount DESC
        """, nativeQuery = true)
    List<ComponentsAmountPc> componentStatsPcBetweenMSSQL(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

//stats
    @Query(
            """
            select new org.project.backend_pcbuild.pcComponents.dto.ComponentMinMaxValueDto(c.id,  min(o.price) , max(o.price))
            from Component c
            join Offer o on o.component = c
            where c.id = :id
            group by c.id
           \s"""
    )
    ComponentMinMaxValueDto findMinMaxValueDtoById(@Param("id") Long id);

    public interface ShopPriceFlat {
        String getName();
        int getMonth();
        double getAvgPrice();
    }

    @Query(value = """
        SELECT 
            s.name,
            MONTH(ou.finished_at) AS month,
            AVG(o.price) AS avgPrice
        FROM component c
        JOIN offer o ON o.component_id = c.id
        JOIN offer_shop_offer_update osou ON osou.offer_id = o.id
        JOIN shop_offer_update sou ON sou.id = osou.shop_offer_update_id
        JOIN shop s ON s.id = sou.shop_id
        JOIN offer_update ou ON ou.id = sou.offer_update_id
        WHERE c.id = :componentId 
            AND ou.finished_at IS NOT NULL
        GROUP BY s.name, YEAR(ou.finished_at), MONTH(ou.finished_at)
        ORDER BY s.name, YEAR(ou.finished_at), MONTH(ou.finished_at)
        """, nativeQuery = true)
    List<ShopPriceFlat> findMonthlyPricesByComponent(@Param("componentId") Long componentId);
}


