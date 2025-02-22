package com.demos.file_service.infrastructure.storage.azure;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.azure.storage.blob.BlobContainerAsyncClient;
import com.demos.file_service.domain.Asset;
import com.demos.file_service.domain.repository.StorageRepository;

import lombok.AllArgsConstructor;
import reactor.core.publisher.Mono;

@AllArgsConstructor
@Service
public class StorageRepositoryImpl implements StorageRepository {

  private BlobContainerAsyncClient blobContainerClient;

  @Override
  public Mono<Asset> saveAsset(Asset asset) {
    var blobName = UUID.randomUUID().toString();
    var blobClient = blobContainerClient.getBlobAsyncClient(blobName);
    try {
      asset.setUrl(blobClient.getBlobUrl());
      return blobClient.uploadFromFile(asset.getPath().toString()).thenReturn(asset);
    } catch (Exception ex) {
      return Mono.error(ex);
    }
  }
}
