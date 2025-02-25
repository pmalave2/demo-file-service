package com.demos.file_service.infrastructure.storage.azure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;

@Configuration
public class AzureBlobConfig {

  @Bean
  BlobContainerClient testBlobContainerAsyncClient(BlobServiceClient blobServiceClient,
      @Value("${spring.cloud.azure.storage.blob.container-name}") String containerName) {
    var blobContainerClient = blobServiceClient.getBlobContainerClient(containerName);
    blobContainerClient.createIfNotExists();
    return blobContainerClient;
  }
}
