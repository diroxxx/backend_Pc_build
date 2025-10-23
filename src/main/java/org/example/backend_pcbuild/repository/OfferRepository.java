package org.example.backend_pcbuild.repository;

import org.example.backend_pcbuild.models.Offer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OfferRepository extends JpaRepository<Offer, Long> {
    Optional<Offer> findByWebsiteUrl(String website_url);

    List<Offer> findAllByWebsiteUrlIn(List<String> urls);

    void deleteByWebsiteUrlIn(List<String> urls);
}
