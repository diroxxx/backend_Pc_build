package org.example.backend_pcbuild.repository;

import org.example.backend_pcbuild.models.Storage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StorageRepository extends JpaRepository<Storage, Integer> {
}
