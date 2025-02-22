package com.demos.file_service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;

@TestConfiguration
public class TestConfig {

  @Bean
  public BlobContainerClient testBlobContainerAsyncClient(BlobServiceClient blobServiceClient,
      @Value("${spring.cloud.azure.storage.blob.container-name}") String containerName) {
    var blobContainerClient = blobServiceClient.getBlobContainerClient(containerName);
    blobContainerClient.create();
    return blobContainerClient;
  }
}
