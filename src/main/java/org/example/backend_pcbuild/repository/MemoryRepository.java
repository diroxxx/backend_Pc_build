package org.example.backend_pcbuild.repository;

import org.example.backend_pcbuild.models.GraphicsCard;
import org.example.backend_pcbuild.models.Memory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemoryRepository extends JpaRepository<Memory, Integer> {

}
