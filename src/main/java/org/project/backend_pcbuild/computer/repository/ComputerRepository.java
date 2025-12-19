package org.project.backend_pcbuild.computer.repository;

import org.project.backend_pcbuild.computer.model.Computer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ComputerRepository extends JpaRepository<Computer, Long> {
    List<Computer> findAllByUserEmail(String userEmail);
    List<Computer> findAllByUserEmailAndName(String userEmail, String name);
    Optional<Computer> findByName(String name);

}
