package org.example.backend_pcbuild.repository;

import org.example.backend_pcbuild.models.GpuModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GpuModelRepository extends JpaRepository<GpuModel, Long> {
    Optional<GpuModel> findByChipsetIgnoreCase(String chipset);
    List<GpuModel> findByChipsetContainingIgnoreCase(String part);
    List<GpuModel> findAll();
}
