package org.example.dataexchangesystem.model;

public class BlobDTO {
    private String blobUrl;
    private String blobName;

    // Konstruktor
    public BlobDTO(String blobName, String blobUrl) {
        this.blobUrl = blobUrl;
        this.blobName = blobName;
    }

    // Gettery
    public String getBlobUrl() {
        return blobUrl;
    }

    public String getBlobName() {
        return blobName;
    }

    // Settery (opcjonalnie, jeśli są potrzebne)
    public void setBlobUrl(String blobUrl) {
        this.blobUrl = blobUrl;
    }

    public void setBlobName(String blobName) {
        this.blobName = blobName;
    }
}
