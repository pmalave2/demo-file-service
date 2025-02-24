package com.demos.file_service.domain.service;

import org.springframework.http.codec.multipart.FilePart;

import com.demos.file_service.domain.Asset;
import com.demos.file_service.domain.AssetFilterParams;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface FileService {

  Mono<Asset> uploadFile(String filename, String encodedFile, String contentType);

  Mono<Asset> uploadFile(FilePart file);

  Flux<Asset> getAssets(AssetFilterParams params);
}
