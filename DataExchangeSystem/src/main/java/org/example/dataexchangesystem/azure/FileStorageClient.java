package org.example.dataexchangesystem.azure;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;

@Service
public interface FileStorageClient {

   String uploadFile(String containerName, String orginalFileName, InputStream inputStream, long length) throws IOException;

}
