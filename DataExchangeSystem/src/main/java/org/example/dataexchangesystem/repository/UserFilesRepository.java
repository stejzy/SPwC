package org.example.dataexchangesystem.repository;

import org.example.dataexchangesystem.model.UserFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserFilesRepository extends JpaRepository<UserFile, Long> {
    List<UserFile> findByUserId(Long userId);

}
