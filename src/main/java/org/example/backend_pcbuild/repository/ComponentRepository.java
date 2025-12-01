package org.example.backend_pcbuild.repository;

import org.example.backend_pcbuild.models.Brand;
import org.example.backend_pcbuild.models.Component;
import org.example.backend_pcbuild.models.ComponentType;
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

}

