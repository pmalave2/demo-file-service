package com.demos.file_service.domain.service.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;

import com.demos.file_service.domain.Asset;
import com.demos.file_service.domain.AssetFilterParams;
import com.demos.file_service.domain.repository.PersistenceRepository;
import com.demos.file_service.domain.repository.StorageRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class FileServiceImplTest {

  @Mock
  private PersistenceRepository persistenceRepository;

  @Mock
  private StorageRepository storageRepository;

  @Mock
  private FilePart filePart;

  @InjectMocks
  private FileServiceImpl fileServiceImpl;

  private Asset testAsset;

  @BeforeEach
  void setup() {
    testAsset = Asset.buildAsset("sample.txt", MediaType.TEXT_PLAIN_VALUE, Path.of("sample.txt"));
  }

  @Test
  void getAssetsShouldReturnOneAsset() {
    var params = new AssetFilterParams();
    when(persistenceRepository.select(params)).thenReturn(Flux.just(testAsset));

    StepVerifier.create(fileServiceImpl.getAssets(params))
        .expectNext(testAsset)
        .verifyComplete();

    verify(persistenceRepository).select(params);
  }

  @Test
  void uploadFileShouldReturnAsset() {
    when(persistenceRepository.insert(any(Asset.class))).thenReturn(Mono.just(testAsset));
    when(storageRepository.saveAsset(any(Asset.class))).thenReturn(Mono.just(testAsset));
    when(persistenceRepository.update(any(Asset.class))).thenReturn(Mono.just(testAsset));

    var result = fileServiceImpl.uploadFile("filename.txt", "ZmlsZS1jb250ZW50", MediaType.TEXT_PLAIN_VALUE);

    StepVerifier.create(result)
        .expectNextMatches(asset -> "sample.txt".equals(asset.getFilename()))
        .verifyComplete();
  }

  @Test
  void uploadFileFormShouldReturnAsset() {
    var headers = new HttpHeaders();
    headers.setContentType(MediaType.TEXT_PLAIN);
    var bytes = "bytes".getBytes();
    DataBuffer buffer = new DefaultDataBufferFactory().allocateBuffer(bytes.length);
    buffer.write(bytes);
    when(filePart.filename()).thenReturn("another.txt");
    when(filePart.headers()).thenReturn(headers);
    when(filePart.content()).thenReturn(Flux.just(buffer));
    when(persistenceRepository.insert(any(Asset.class))).thenReturn(Mono.just(testAsset));
    when(storageRepository.saveAsset(any(Asset.class))).thenReturn(Mono.just(testAsset));
    when(persistenceRepository.update(any(Asset.class))).thenReturn(Mono.just(testAsset));

    var result = fileServiceImpl.uploadFile(filePart);

    StepVerifier.create(result)
        .expectNextMatches(asset -> "sample.txt".equals(asset.getFilename()))
        .verifyComplete();
  }
}