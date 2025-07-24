package org.example.backend_pcbuild.repository;

import org.example.backend_pcbuild.models.PowerSupply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PowerSupplyRepository extends JpaRepository<PowerSupply, Integer> {

}
