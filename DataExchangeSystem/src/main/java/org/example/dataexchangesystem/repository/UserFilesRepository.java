package org.example.dataexchangesystem.repository;

import org.example.dataexchangesystem.model.UserFile;
import org.example.dataexchangesystem.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserFilesRepository extends JpaRepository<UserFile, Long> {
    List<UserFile> findByUserId(Long userId);

    Optional<UserFile> findByBlobName(String fileName);

    @Query("SELECT uf FROM UserFile uf WHERE uf.user = :user AND uf.blobName LIKE CONCAT(:baseFileName, '%')")
    List<UserFile> findByUserAndBlobBaseName(@Param("user") Users user, @Param("baseFileName") String baseFileName);

    @Query("SELECT uf FROM UserFile uf WHERE uf.blobName LIKE %:baseFileName% ORDER BY uf.lastModification DESC")
    List<UserFile> findLatestFilesByBaseName(@Param("baseFileName") String baseFileName);

}
