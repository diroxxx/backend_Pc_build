package org.example.backend_pcbuild.Admin.repository;

import org.example.backend_pcbuild.models.ShopOfferUpdate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ShopOfferUpdateRepository extends JpaRepository<ShopOfferUpdate, Long> {

    Optional<ShopOfferUpdate> findByOfferUpdateIdAndShopName(Long offerUpdateId, String shopName);

}
