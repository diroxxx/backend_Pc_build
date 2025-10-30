package org.example.backend_pcbuild.repository;

import org.example.backend_pcbuild.models.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemRepository extends JpaRepository<Item, Integer>, JpaSpecificationExecutor<Item> {

    Optional<Item> findByBrandAndModel(String brand, String model);

    @Query("SELECT DISTINCT i.brand FROM Item i WHERE i.brand IS NOT NULL AND i.brand != ''")
    List<String> findDistinctBrands();}
