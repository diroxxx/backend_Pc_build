package org.example.backend_pcbuild.Admin.repository;

import org.example.backend_pcbuild.Admin.dto.OfferUpdateType;
import org.example.backend_pcbuild.models.OfferUpdateConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OfferUpdateConfigRepository extends JpaRepository<OfferUpdateConfig, Long> {

    Optional<OfferUpdateConfig> findByType(OfferUpdateType type);
}
