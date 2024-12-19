package org.example.dataexchangesystem.azure;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.models.BlobItem;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
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

    public List<BlobInfo> getAllBlobInfo(String containerName) {
        BlobContainerClient blobContainerClient = blobServiceClient.getBlobContainerClient(containerName);

        List<BlobInfo> blobInfoList = new ArrayList<>();

        for (BlobItem blobItem : blobContainerClient.listBlobs()) {
            String blobName = blobItem.getName();
            String blobUrl = blobContainerClient.getBlobClient(blobName).getBlobUrl();

            blobInfoList.add(new BlobInfo(blobName, blobUrl));
        }

        return blobInfoList;
    }

    public static class BlobInfo {
        private String blobName;
        private String blobUrl;

        public BlobInfo(String blobName, String blobUrl) {
            this.blobName = blobName;
            this.blobUrl = blobUrl;
        }

        public String getBlobName() {
            return blobName;
        }

        public String getBlobUrl() {
            return blobUrl;
        }
    }
}