package org.example.dataexchangesystem.azure;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.models.BlobItem;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.example.dataexchangesystem.model.BlobDTO;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class AzureFileStorageClient implements FileStorageClient {


    private final BlobServiceClient blobServiceClient;

    public AzureFileStorageClient(BlobServiceClient blobServiceClient) {
        this.blobServiceClient = blobServiceClient;
    }


    @Override
    public BlobDTO uploadFile(String containerName, String originalFileName, InputStream inputStream, long length) throws FileUploadException {
        BlobContainerClient blobContainerClient = blobServiceClient.getBlobContainerClient(containerName);

        BlobClient blobClient = blobContainerClient.getBlobClient(originalFileName);

        try {
            blobClient.upload(inputStream, length, true);
        } catch (Exception e) {
            throw new FileUploadException("Nie udało się przesłać pliku: " + e.getMessage(), e);
        }

        return new BlobDTO(originalFileName, blobClient.getBlobUrl());
    }

    @Override
    public void deleteFile(String containerName, String orginalFileName) {
        BlobContainerClient blobContainerClient = blobServiceClient.getBlobContainerClient(containerName);
        BlobClient blobClient = blobContainerClient.getBlobClient(orginalFileName);
        if (blobClient.exists()) {
            blobClient.delete();
        }
    }

    public List<BlobDTO> getAllBlobInfo(String containerName) {
        BlobContainerClient blobContainerClient = blobServiceClient.getBlobContainerClient(containerName);

        List<BlobDTO> blobDTOList = new ArrayList<>();

        for (BlobItem blobItem : blobContainerClient.listBlobs()) {
            String blobName = blobItem.getName();
            String blobUrl = blobContainerClient.getBlobClient(blobName).getBlobUrl();

            blobDTOList.add(new BlobDTO(blobName, blobUrl));
        }

        return blobDTOList;
    }
}