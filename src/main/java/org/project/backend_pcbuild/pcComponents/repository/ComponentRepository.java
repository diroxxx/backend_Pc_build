package org.project.backend_pcbuild.pcComponents.repository;

import org.project.backend_pcbuild.offer.model.Brand;
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
public interface ComponentRepository extends JpaRepository<Component, Integer>, JpaSpecificationExecutor<Component> {


    @Query("SELECT DISTINCT i.name FROM Brand i WHERE i.name IS NOT NULL AND i.name != ''")
    List<String> findDistinctBrands();

    Optional<Component> findByBrandAndModelIgnoreCase(Brand brand, String model);

    List<Component> findAllByComponentType(ComponentType componentType);

    @Query("select c.model from Component c join c.processor p " +
            "where c.componentType = :type and p.benchmark is not null " +
            "order by p.benchmark desc")
    List<String> findProcessorModelsOrderedByBenchmarkDesc(@Param("type") ComponentType type);


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


}

