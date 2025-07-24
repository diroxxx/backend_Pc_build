package org.example.backend_pcbuild.repository;

import org.example.backend_pcbuild.models.Motherboard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MotherboardRepository extends JpaRepository<Motherboard, Integer> {
}
