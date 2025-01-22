package org.example.dataexchangesystem.azure;

import org.example.dataexchangesystem.model.BlobDTO;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;

@Service
public interface FileStorageClient {

   BlobDTO uploadFile(String containerName, String orginalFileName, InputStream inputStream, long length) throws IOException;
   void deleteFile(String containerName, String orginalFileName);
}
