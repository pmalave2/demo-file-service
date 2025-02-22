package com.demos.file_service;

import static com.demos.file_service.TestFileServiceApplication.PROFILE;
import static com.demos.file_service.application.http.AssetsController.ASSET_UPLOAD_URI;
import static com.demos.file_service.application.http.AssetsController.BASE_URI;

import java.util.Base64;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

import com.demos.file_service.application.dto.AssetFileUploadRequest;
import com.demos.file_service.domain.service.FileService;
import com.demos.file_service.infrastructure.orm.entity.AssetEntity;
import com.demos.file_service.infrastructure.orm.repositories.AssetRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@Import({ TestcontainersConfiguration.class, TestConfig.class })
@SpringBootTest
@AutoConfigureWebTestClient(timeout = "360000")
@ActiveProfiles(PROFILE)
class FileServiceIntegrationTests {

  @Autowired
  WebTestClient webTestClient;
  @Autowired
  ObjectMapper jackson;
  @Autowired
  FileService fileService;
  @Autowired
  AssetRepository assetsRepository;

  static GenericContainer<?> azurite = new GenericContainer<>(
      DockerImageName.parse("mcr.microsoft.com/azure-storage/azurite:latest"))
      .withExposedPorts(10000, 10001, 10002);

  static {
    azurite.start();
  }

  @DynamicPropertySource
  static void registerAzuriteProperties(DynamicPropertyRegistry registry) {
    var string = String.format("http://%s:%s/devstoreaccount1", azurite.getHost(),
        azurite.getMappedPort(10000));
    registry.add("spring.cloud.azure.storage.blob.endpoint",
        () -> string);
  }

  @Test
  void givenAssets_whenUploadAsRest_thenShouldBeStored() {
    var encoder = Base64.getEncoder();
    var encodedFile = encoder.encodeToString("Hola Mundo\n".getBytes());
    var request = new AssetFileUploadRequest("test.txt", encodedFile, MediaType.TEXT_PLAIN_VALUE);

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

    var caseInsensitiveExampleMatcher = ExampleMatcher.matchingAny().withIgnoreCase().withMatcher("filename",
        ExampleMatcher.GenericPropertyMatcher::exact)
        .withMatcher("url",
            ExampleMatcher.GenericPropertyMatcher::contains);
    var example = Example.of(AssetEntity.builder().filename("test.txt").url("http").build(),
        caseInsensitiveExampleMatcher);
    assetsRepository.exists(example).as(StepVerifier::create)
        .expectNext(Boolean.TRUE)
        .verifyComplete();
  }

  @Test
  void givenAssets_whenUploadAsForm_thenShouldBeStored() {
    var bodyBuilder = new MultipartBodyBuilder();
    bodyBuilder.part("file", new ClassPathResource("static/tux.png")).contentType(MediaType.IMAGE_PNG);

    webTestClient
        .post()
        .uri(BASE_URI + ASSET_UPLOAD_URI)
        .contentType(MediaType.MULTIPART_FORM_DATA)
        .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
        .exchange()
        .expectStatus().isAccepted()
        .expectHeader().contentType(MediaType.APPLICATION_JSON)
        .expectBody()
        .jsonPath("$.id").isNotEmpty();

    assetsRepository.count().as(StepVerifier::create)
        .expectNext(1L)
        .verifyComplete();
  }

  @Test
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
