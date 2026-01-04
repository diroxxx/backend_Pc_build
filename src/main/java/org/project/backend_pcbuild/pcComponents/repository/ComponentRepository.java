package org.project.backend_pcbuild.pcComponents.repository;

import org.project.backend_pcbuild.offer.model.Brand;
import org.project.backend_pcbuild.pcComponents.dto.ComponentsAmountPc;
import org.project.backend_pcbuild.pcComponents.model.Component;
import org.project.backend_pcbuild.pcComponents.model.ComponentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ComponentRepository extends JpaRepository<Component, Integer>, JpaSpecificationExecutor<Component> {


    @Query("SELECT DISTINCT i.name FROM Brand i WHERE i.name IS NOT NULL AND i.name != ''")
    List<String> findDistinctBrands();

    @Query("SELECT DISTINCT i.model FROM Component i WHERE i.brand.name = :brandName")
    List<String> findDistinctModelsByBrandName(@Param("brandName") String brandName);

    Optional<Component> findByBrandAndModelIgnoreCase(Brand brand, String model);

    List<Component> findAllByComponentType(ComponentType componentType);


//    @Query("SELECT c FROM Component c WHERE c.componentType = :componentType AND c.model LIKE %:model%")
//    List<Component> findAllByComponentTypeAndModel(@Param("model") String model, @Param("componentType") ComponentType componentType);

    @Query("SELECT c FROM Component c " +
            "WHERE c.componentType = :componentType " +
            "AND LOWER(c.model) LIKE LOWER(CONCAT('%', :model, '%'))")
    List<Component> findAllByComponentTypeAndModel(@Param("model") String model,
                                                   @Param("componentType") ComponentType componentType);


    @Query("""
    select c from Component c 
    left join c.brand b
    left join Processor p on p.component = c and :type = org.project.backend_pcbuild.pcComponents.model.ComponentType.PROCESSOR
    left join GraphicsCard gc on gc.component = c and :type = org.project.backend_pcbuild.pcComponents.model.ComponentType.GRAPHICS_CARD
    left join Motherboard m on m.component = c and :type = org.project.backend_pcbuild.pcComponents.model.ComponentType.MOTHERBOARD
    left join PowerSupply ps on ps.component = c and :type = org.project.backend_pcbuild.pcComponents.model.ComponentType.POWER_SUPPLY
    left join Memory my on my.component = c and :type = org.project.backend_pcbuild.pcComponents.model.ComponentType.MEMORY
    left join Storage s on s.component = c and :type = org.project.backend_pcbuild.pcComponents.model.ComponentType.STORAGE
    left join Cooler cr on cr.component = c and :type = org.project.backend_pcbuild.pcComponents.model.ComponentType.CPU_COOLER
    left join Case ce on ce.component = c and :type = org.project.backend_pcbuild.pcComponents.model.ComponentType.CASE_PC
    where (:brand is null or LOWER(b.name) = Lower(:brand))
    and (:type is null or c.componentType = :type)
    and (:searchTerm is null or lower(c.model) like lower(concat('%', :searchTerm, '%')))
    
    
    



""")
    Page<Component> findAllByFilters(@Param("brand") String brand, @Param("type") ComponentType type, @Param("searchTerm") String searchTerm, Pageable pageable);


    @Query(value ="""

        SELECT componentType, model, amount
        FROM (
            SELECT
                c. component_type as componentType,
                c.model,
                COUNT(*) as amount,
                ROW_NUMBER() OVER (PARTITION BY c.component_type ORDER BY COUNT(*) DESC) as rn
            FROM component c
            JOIN offer o ON c.id = o.component_id
            JOIN computer_offer co ON o.id = co.offer
            GROUP BY c.component_type, c. model
        ) ranked
        WHERE rn = 1
        ORDER BY amount DESC;
""", nativeQuery = true)
    List<ComponentsAmountPc> componentStatsPc();

}

