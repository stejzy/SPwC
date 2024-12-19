package org.example.dataexchangesystem.azure;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Service
public class AzureFileStorageClient implements FileStorageClient {


    private final BlobServiceClient blobServiceClient;

    public AzureFileStorageClient(BlobServiceClient blobServiceClient) {
        this.blobServiceClient = blobServiceClient;
    }


    @Override
    public String uploadFile(String containerName, String orginalFileName, InputStream inputStream, long length) throws IOException {
        BlobContainerClient blobContainerClient = blobServiceClient.getBlobContainerClient(containerName);

        String newFileName = UUID.randomUUID().toString() + orginalFileName.substring(orginalFileName.lastIndexOf("."));

        BlobClient blobClient = blobContainerClient.getBlobClient(newFileName);

        blobClient.upload(inputStream, length, true);

        return blobClient.getBlobUrl();
    }
}