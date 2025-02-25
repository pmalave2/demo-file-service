package com.demos.file_service.infrastructure.storage.azure;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;

import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.BlobContainerAsyncClient;
import com.demos.file_service.domain.Asset;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class StorageRepositoryImplTest {

  @Mock
  private BlobContainerAsyncClient blobContainerClient;

  @Mock
  private BlobAsyncClient blobClient;

  @InjectMocks
  private StorageRepositoryImpl storageRepository;

  private Asset asset;

  @BeforeEach
  void setUp() {
    asset = Asset.builder()
        .id(UUID.randomUUID())
        .filename("test.txt")
        .contentType(MediaType.TEXT_PLAIN)
        .size(10)
        .path(Paths.get("/path/to/testfile.txt"))
        .uploadDate(LocalDateTime.of(2020, 8, 22, 12, 0, 0))
        .build();
  }

  @Test
  void saveAssetShouldSaveAsset() {
    when(blobContainerClient.getBlobAsyncClient(anyString())).thenReturn(blobClient);
    when(blobClient.getBlobUrl()).thenReturn("http://blob.url/testfile.txt");
    when(blobClient.uploadFromFile(anyString())).thenReturn(Mono.empty());

    var result = storageRepository.saveAsset(asset);

    StepVerifier.create(result)
        .expectNextMatches(savedAsset -> savedAsset.getUrl().equals("http://blob.url/testfile.txt"))
        .verifyComplete();

    verify(blobContainerClient).getBlobAsyncClient(anyString());
    verify(blobClient).uploadFromFile(anyString());
  }
}
