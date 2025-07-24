package org.example.backend_pcbuild.repository;

import org.example.backend_pcbuild.models.Computer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ComputerRepository extends JpaRepository<Computer, Integer> {
}
