package org.project.backend_pcbuild.offersUpdates.repository;

import org.project.backend_pcbuild.offersUpdates.model.OfferUpdateType;
import org.project.backend_pcbuild.offersUpdates.model.OfferUpdateConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OfferUpdateConfigRepository extends JpaRepository<OfferUpdateConfig, Long> {

    Optional<OfferUpdateConfig> findByType(OfferUpdateType type);
}
