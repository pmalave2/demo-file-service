package com.demos.file_service.domain.service;

import org.springframework.web.multipart.MultipartFile;

import com.demos.file_service.domain.Asset;

import reactor.core.publisher.Mono;

public interface FileService {

  Mono<Asset> uploadFile(String filename, String encodedFile, String contentType);

  Mono<Asset> uploadFile(MultipartFile file);
}
