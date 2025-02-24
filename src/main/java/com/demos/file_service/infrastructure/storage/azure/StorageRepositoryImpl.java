package com.demos.file_service.infrastructure.storage.azure;

import org.springframework.stereotype.Service;

import com.azure.storage.blob.BlobContainerAsyncClient;
import com.demos.file_service.domain.Asset;
import com.demos.file_service.domain.repository.StorageRepository;

import io.github.resilience4j.retry.annotation.Retry;
import lombok.AllArgsConstructor;
import reactor.core.publisher.Mono;

@AllArgsConstructor
@Service
public class StorageRepositoryImpl implements StorageRepository {

  public static final String SERVICE_NAME = "storageAzure";

  private BlobContainerAsyncClient blobContainerClient;

  @Retry(name = SERVICE_NAME)
  @Override
  public Mono<Asset> saveAsset(Asset asset) {
    var blobName = String.format("%s-%s", asset.getId().toString(), asset.getFilename());
    var blobClient = blobContainerClient.getBlobAsyncClient(blobName);
    asset.setUrl(blobClient.getBlobUrl());

    return blobClient.uploadFromFile(asset.getPath().toString()).thenReturn(asset);
  }
}
