package com.demos.file_service.application.http;

import static com.demos.file_service.TestFileServiceApplication.PROFILE;
import static com.demos.file_service.application.http.AssetsController.ASSET_UPLOAD_URI;
import static com.demos.file_service.application.http.AssetsController.BASE_URI;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;

import com.demos.file_service.application.dto.AssetFileUploadRequest;
import com.demos.file_service.domain.Asset;
import com.demos.file_service.domain.service.FileService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@WebFluxTest(AssetsController.class)
@ActiveProfiles(PROFILE)
@AutoConfigureWebTestClient
class AssetsControllerTest {

  @Autowired
  WebTestClient webTestClient;

  @MockitoBean
  FileService fileService;

  static MultiValueMap<String, String> params = new LinkedMultiValueMap<>() {
    {
      add("uploadDateStart", "2025-01-01");
      add("uploadDateEnd", "2025-12-31");
      add("filename", "test.txt");
      add("filetype", "text");
      add("sortDirection", "DESC");
    }
  };

  @BeforeEach
  void setUp() {
    var asset = createAsset();
    when(fileService.getAssets(any())).thenReturn(Flux.just(asset));
    when(fileService.uploadFile(any(), any(), any())).thenReturn(Mono.just(asset));
    when(fileService.uploadFile(any())).thenReturn(Mono.just(asset));
  }

  @Test
  void getAssetsShouldReturnListofAssets() {
    webTestClient
        .get()
        .uri(BASE_URI)
        .exchange()
        .expectStatus().isOk()
        .expectHeader().contentType(MediaType.APPLICATION_JSON)
        .expectBody()
        .consumeWith(System.out::println)
        .jsonPath("$").isArray()
        .jsonPath("$").isNotEmpty()
        .jsonPath("$.size()").isEqualTo(1);
  }

  @ParameterizedTest
  @CsvSource({
      "2025-01-01T00:00,2025-12-31T00:00,test.txt,text,DESC,OK",
      "2025-01-01T00:00,2025-12-31T00:00,test.txt,text,asc,KO",
      "2025-01-01T00:00,2025-12-31T00:00,test.txt,text,DES,KO",
      "2025-31-01T00:00,2025-12-31T00:00,test.txt,text,ASC,KO",
      "2025-01-01T00:00,2025-12-31T00:00,test.txt,text,,OK",
      "2025-01-01T00:00,2025/12/31,test.txt,text,ASC,KO",
      ",,,,,OK", })
  void getAssetsByFiltersShouldReturnBadRequestOrOK(String uploadDateStart, String uploadDateEnd,
      String filename, String filetype, String sortDirection, String expectedStatus) {
    var paramMap = new LinkedMultiValueMap<String, String>() {
      {
        add("uploadDateStart", uploadDateStart);
        add("uploadDateEnd", uploadDateEnd);
        add("filename", filename);
        add("filetype", filetype);
        add("sortDirection", sortDirection);
      }
    };

    var response = webTestClient
        .get()
        .uri(uriBuilder -> uriBuilder
            .path(BASE_URI)
            .queryParams(paramMap)
            .build())
        .exchange();
    if ("OK".equals(expectedStatus))
      response.expectStatus().isOk();
    else
      response.expectStatus().isBadRequest();
  }

  @ParameterizedTest
  @CsvSource({
      "test.txt,SG9sYSBNdW5kbwo=,text/plain,OK",
      ",SG9sYSBNdW5kbwo=,text/plain,KO",
      "test.txt,,text/plain,KO",
      "test.txt,SG9sYSBNdW5kbwo=,,KO",
      ",,,KO",
      "test.txt,SG9sYSBNdW5kbwo=,text,KO" })
  void postAssetShouldReturnBadRequestOrOK(String filename, String encodedFile, String filetype,
      String expectedStatus) {
    var request = new AssetFileUploadRequest(filename, encodedFile, filetype);

    var response = webTestClient
        .post()
        .uri(BASE_URI + ASSET_UPLOAD_URI)
        .contentType(MediaType.APPLICATION_JSON)
        .body(Mono.just(request), AssetFileUploadRequest.class)
        .exchange();
    if ("OK".equals(expectedStatus))
      response.expectStatus().isAccepted();
    else
      response.expectStatus().isBadRequest();
  }

  @ParameterizedTest
  @ValueSource(strings = { "OK", "KO" })
  void postFormAssetShouldReturnBadRequestOrOK(String expectedStatus) {
    var bodyBuilder = new MultipartBodyBuilder();
    if ("OK".equals(expectedStatus))
      bodyBuilder.part("file", new ClassPathResource("static/tux.png")).contentType(MediaType.IMAGE_PNG);

    var response = webTestClient
        .post()
        .uri(BASE_URI + ASSET_UPLOAD_URI)
        .contentType(MediaType.MULTIPART_FORM_DATA)
        .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
        .exchange();
    if ("OK".equals(expectedStatus))
      response.expectStatus().isAccepted();
    else
      response.expectStatus().isBadRequest();
  }

  private Asset createAsset() {
    return Asset.builder()
        .id(UUID.randomUUID())
        .filename("test.txt")
        .contentType(MediaType.TEXT_PLAIN)
        .size(10)
        .url("http://localhost:8080/test.txt")
        .uploadDate(LocalDateTime.of(2020, 8, 22, 12, 0, 0))
        .build();
  }
}
