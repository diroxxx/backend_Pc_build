package org.example.backend_pcbuild.repository;

import org.example.backend_pcbuild.models.Shop;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ShopRepository extends JpaRepository<Shop, Long> {
    Optional<Shop> findByNameIgnoreCase(String name);

}
