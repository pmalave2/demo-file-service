package com.demos.file_service;

import static com.demos.file_service.TestFileServiceApplication.PROFILE;
import static com.demos.file_service.application.http.AssetsController.ASSET_UPLOAD_URI;
import static com.demos.file_service.application.http.AssetsController.BASE_URI;
import static org.springframework.data.relational.core.query.Criteria.from;
import static org.springframework.data.relational.core.query.Criteria.where;
import static org.springframework.data.relational.core.query.Query.query;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;

import com.demos.file_service.application.dto.AssetFileUploadRequest;
import com.demos.file_service.domain.service.FileService;
import com.demos.file_service.infrastructure.orm.entity.AssetEntity;

import io.r2dbc.spi.ConnectionFactory;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@Import({ TestcontainersConfiguration.class })
@SpringBootTest
@AutoConfigureWebTestClient(timeout = "360000")
@ActiveProfiles(PROFILE)
class FileServiceIntegrationTests {

  @Autowired
  WebTestClient webTestClient;
  @Autowired
  FileService fileService;
  @Autowired
  R2dbcEntityTemplate template;

  @BeforeEach
  void setUp(@Value("classpath:static/test-data.sql") Resource testDataSql,
      @Autowired ConnectionFactory connectionFactory) {
    var resourceDatabasePopulator = new ResourceDatabasePopulator();
    resourceDatabasePopulator.addScript(testDataSql);
    resourceDatabasePopulator.populate(connectionFactory).block();
  }

  @Test
  void uploadAsRestShouldBeStored() {
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
        .consumeWith(System.out::println)
        .jsonPath("$.id").isNotEmpty();

    var criterias = new ArrayList<Criteria>();
    criterias.add(where(AssetEntity.Fields.filename).is("test.txt"));
    criterias.add(where(AssetEntity.Fields.url).like("http%test.txt"));
    Mono.delay(Duration.ofSeconds(5))
        .then(
            template.select(AssetEntity.class)
                .matching(query(from(criterias)))
                .exists())
        .delayElement(Duration.ofSeconds(5))
        .as(StepVerifier::create)
        .expectNext(Boolean.TRUE)
        .verifyComplete();
  }

  @Test
  void uploadAsFormShouldBeStored() {
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
        .consumeWith(System.out::println)
        .jsonPath("$.id").isNotEmpty();

    var criterias = new ArrayList<Criteria>();
    criterias.add(where(AssetEntity.Fields.filename).is("tux.png"));
    criterias.add(where(AssetEntity.Fields.url).like("http%tux.png"));
    Mono.delay(Duration.ofSeconds(5))
        .then(
            template.select(AssetEntity.class)
                .matching(query(from(criterias)))
                .exists())
        .as(StepVerifier::create)
        .expectNext(Boolean.TRUE)
        .verifyComplete();
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
        .jsonPath("$.size()").isEqualTo(4);
  }

  @Test
  void getAssetsByUploadDateStartShouldReturnListofAssets() {
    MultiValueMap<String, String> params = new LinkedMultiValueMap<>() {
      {
        add("uploadDateStart", "2025-01-01T00:00");
      }
    };

    webTestClient
        .get()
        .uri(uriBuilder -> uriBuilder
            .path(BASE_URI)
            .queryParams(params)
            .build())
        .exchange()
        .expectStatus().isOk()
        .expectHeader().contentType(MediaType.APPLICATION_JSON)
        .expectBody()
        .consumeWith(System.out::println)
        .jsonPath("$").isArray()
        .jsonPath("$").isEmpty();
  }

  @Test
  void getAssetsByUploadDateEndAndSortedAscShouldReturnListofAssets() {
    MultiValueMap<String, String> params = new LinkedMultiValueMap<>() {
      {
        add("uploadDateEnd", "2025-01-01T00:00");
        add("sortDirection", "ASC");
      }
    };

    webTestClient
        .get()
        .uri(uriBuilder -> uriBuilder
            .path(BASE_URI)
            .queryParams(params)
            .build())
        .exchange()
        .expectStatus().isOk()
        .expectHeader().contentType(MediaType.APPLICATION_JSON)
        .expectBody()
        .consumeWith(System.out::println)
        .jsonPath("$").isNotEmpty()
        .jsonPath("$.size()").isEqualTo(4)
        .jsonPath("$[0].uploadDate").isEqualTo("2020-06-14T00:00:00")
        .jsonPath("$[3].uploadDate").isEqualTo("2023-06-15T16:00:00");
  }
}
