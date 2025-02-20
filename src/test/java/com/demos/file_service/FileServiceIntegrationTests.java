package com.demos.file_service;

import static com.demos.file_service.application.http.AssetsController.ASSET_UPLOAD_URI;
import static com.demos.file_service.application.http.AssetsController.BASE_URI;

import java.util.Base64;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.demos.file_service.application.dto.AssetFileUploadRequest;
import com.demos.file_service.domain.service.FileService;
import com.demos.file_service.infrastructure.orm.repositories.AssetRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureWebTestClient
class FileServiceIntegrationTests {

  @Autowired
  WebTestClient webTestClient;
  @Autowired
  ObjectMapper jackson;
  @Autowired
  FileService fileService;
  @Autowired
  AssetRepository assetsRepository;

  @Test
  void givenAssets_whenUploadAsRest_thenShouldBeStored() throws Exception {
    var encoder = Base64.getEncoder();
    MockMultipartFile file = new MockMultipartFile(
        "file",
        "hello.txt",
        MediaType.TEXT_PLAIN_VALUE,
        "Hello, World!".getBytes());
    var encodedFile = encoder.encodeToString(file.getBytes());

    var request = new AssetFileUploadRequest(file.getOriginalFilename(), encodedFile, file.getContentType());

    webTestClient
        .post()
        .uri(BASE_URI + ASSET_UPLOAD_URI)
        .contentType(MediaType.APPLICATION_JSON)
        .body(Mono.just(request), AssetFileUploadRequest.class)
        .exchange()
        .expectStatus().isAccepted()
        .expectHeader().contentType(MediaType.APPLICATION_JSON)
        .expectBody()
        .jsonPath("$.id").isNotEmpty();

    assetsRepository.count().as(StepVerifier::create)
        .expectNext(1L)
        .verifyComplete();
  }

  // @Test
  void givenAssets_whenUploadAsForm_thenShouldBeStored() {
    MockMultipartFile file = new MockMultipartFile(
        "file",
        "hello.txt",
        MediaType.TEXT_PLAIN_VALUE,
        "Hello, World!".getBytes());

    webTestClient
        .post()
        .uri(BASE_URI + ASSET_UPLOAD_URI)
        .contentType(MediaType.MULTIPART_FORM_DATA)
        .bodyValue(file)
        .exchange()
        .expectStatus().isAccepted()
        .expectHeader().contentType(MediaType.APPLICATION_JSON)
        .expectBody()
        .jsonPath("$.id").isNotEmpty();

    assetsRepository.count().as(StepVerifier::create)
        .expectNext(1L)
        .verifyComplete();
  }

  // @Test
  void givenAssets_whenGetAssets_thenShouldReturnListofAssets() {
    webTestClient
        .get()
        .uri(BASE_URI)
        .exchange()
        .expectStatus().isOk()
        .expectHeader().contentType(MediaType.APPLICATION_JSON)
        .expectBody()
        .jsonPath("$").isArray()
        .jsonPath("$").isNotEmpty();
  }
}
