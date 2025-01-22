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
    private Users user; // Powiązanie z użytkownikiem, zakładając, że masz klasę `Users` jako encję.

    @Column(name = "blob_name", nullable = false)
    private String blobName;

    @Column(name = "blob_url", nullable = false)
    private String blobUrl;

    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime uploadedAt;

    // Domyślny konstruktor (wymagany przez JPA)
    public UserFile() {
    }

    // Konstruktor do tworzenia obiektów
    public UserFile(Users user, String blobName, String blobUrl) {
        this.user = user;
        this.blobName = blobName;
        this.blobUrl = blobUrl;
        this.uploadedAt = LocalDateTime.now(); // Ustawienie daty dodania pliku
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

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }
}
