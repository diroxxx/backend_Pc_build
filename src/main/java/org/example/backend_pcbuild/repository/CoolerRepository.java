package org.example.backend_pcbuild.repository;

import org.example.backend_pcbuild.models.Cooler;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CoolerRepository extends JpaRepository<Cooler, Integer> {
}
