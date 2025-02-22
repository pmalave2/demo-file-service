package com.demos.file_service.domain.service;

import org.springframework.http.codec.multipart.FilePart;

import com.demos.file_service.domain.Asset;

import reactor.core.publisher.Mono;

public interface FileService {

  Mono<Asset> uploadFile(String filename, String encodedFile, String contentType);

  Mono<Asset> uploadFile(FilePart file);
}
