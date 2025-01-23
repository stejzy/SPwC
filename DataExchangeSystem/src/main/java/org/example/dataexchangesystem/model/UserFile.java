package org.example.dataexchangesystem.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_files")
public class UserFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private Users user;

    @Column(name = "blob_name", nullable = false)
    private String blobName;

    @Column(name = "blob_url", nullable = false)
    private String blobUrl;

    @Column(name = "last_modification", nullable = false)
    private LocalDateTime lastModification;

    @Column(name = "version", nullable = false)
    private Integer version;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;  // Dodanie rozmiaru pliku

    // Domyślny konstruktor (wymagany przez JPA)
    public UserFile() {
    }

    // Konstruktor do tworzenia obiektów
    public UserFile(Users user, String blobName, String blobUrl, Integer version, Long fileSize) {
        this.user = user;
        this.blobName = blobName;
        this.blobUrl = blobUrl;
        this.lastModification = LocalDateTime.now();
        this.version = version;
        this.fileSize = fileSize;
    }

    // Gettery i Settery

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Users getUser() {
        return user;
    }

    public void setUser(Users user) {
        this.user = user;
    }

    public String getBlobName() {
        return blobName;
    }

    public void setBlobName(String blobName) {
        this.blobName = blobName;
    }

    public String getBlobUrl() {
        return blobUrl;
    }

    public void setBlobUrl(String blobUrl) {
        this.blobUrl = blobUrl;
    }

    public LocalDateTime getLastModification() {
        return lastModification;
    }

    public void setLastModification(LocalDateTime lastModification) {
        this.lastModification = lastModification;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }
}

