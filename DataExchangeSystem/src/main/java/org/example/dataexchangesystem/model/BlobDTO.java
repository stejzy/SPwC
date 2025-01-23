package org.example.dataexchangesystem.model;

import java.time.LocalDateTime;

public class BlobDTO {
    private String blobUrl;
    private String blobName;
    private long fileSize;
    private int version;
    private LocalDateTime lastModification;  // Nowe pole

    // Konstruktor bez 'lastModification'
    public BlobDTO(String blobName, String blobUrl) {
        this.blobName = blobName;
        this.blobUrl = blobUrl;
    }

    // Konstruktor z 'lastModification'
    public BlobDTO(String blobName, String blobUrl, long fileSize, LocalDateTime lastModification, int version) {
        this.blobUrl = blobUrl;
        this.blobName = blobName;
        this.fileSize = fileSize;
        this.version = version;
        this.lastModification = lastModification;
    }

    // Gettery i settery
    public String getBlobUrl() {
        return blobUrl;
    }

    public String getBlobName() {
        return blobName;
    }

    public long getFileSize() {
        return fileSize;
    }

    public int getVersion() {
        return version;
    }

    public LocalDateTime getLastModification() {
        return lastModification;
    }

    public void setBlobUrl(String blobUrl) {
        this.blobUrl = blobUrl;
    }

    public void setBlobName(String blobName) {
        this.blobName = blobName;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public void setLastModification(LocalDateTime lastModification) {
        this.lastModification = lastModification;
    }
}
