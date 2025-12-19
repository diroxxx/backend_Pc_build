package org.project.backend_pcbuild.offer.repository;

import org.project.backend_pcbuild.offer.model.Shop;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ShopRepository extends JpaRepository<Shop, Long> {
    Optional<Shop> findByNameIgnoreCase(String name);

}
