package org.example.backend_pcbuild.repository;

import org.example.backend_pcbuild.models.Computer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ComputerRepository extends JpaRepository<Computer, Long> {
    List<Computer> findAllByUserEmail(String userEmail);

    Optional<Computer> findByName(String name);

}
