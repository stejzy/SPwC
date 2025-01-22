package org.example.dataexchangesystem.repository;

import org.example.dataexchangesystem.model.UserFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserFilesRepository extends JpaRepository<UserFile, Long> {
    List<UserFile> findByUserId(Long userId);

    Optional<UserFile> findByBlobName(String fileName);
}
