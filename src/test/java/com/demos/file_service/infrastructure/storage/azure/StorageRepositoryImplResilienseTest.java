package com.demos.file_service.infrastructure.storage.azure;

import static com.demos.file_service.TestFileServiceApplication.PROFILE;
import static com.demos.file_service.application.http.AssetsController.ASSET_UPLOAD_URI;
import static com.demos.file_service.application.http.AssetsController.BASE_URI;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.data.relational.core.query.Criteria.from;
import static org.springframework.data.relational.core.query.Criteria.where;
import static org.springframework.data.relational.core.query.Query.query;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.context.annotation.Import;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.BlobContainerAsyncClient;
import com.demos.file_service.TestcontainersConfiguration;
import com.demos.file_service.application.dto.AssetFileUploadRequest;
import com.demos.file_service.infrastructure.orm.entity.AssetEntity;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@Import({ TestcontainersConfiguration.class })
@SpringBootTest
@AutoConfigureWebTestClient(timeout = "360000")
@ActiveProfiles(PROFILE)
@ExtendWith({ MockitoExtension.class, OutputCaptureExtension.class })
class StorageRepositoryImplResilienseTest {

  @Autowired
  WebTestClient webTestClient;

  @MockitoBean
  private BlobContainerAsyncClient blobContainerClient;

  @Mock
  BlobAsyncClient blobClient;

  @Autowired
  R2dbcEntityTemplate template;

  @Test
  void saveAssetShouldSaveAsset(final CapturedOutput output) {
    when(blobContainerClient.getBlobAsyncClient(anyString())).thenReturn(blobClient);
    when(blobClient.getBlobUrl()).thenReturn("http://blob.url/testfile.txt");
    when(blobClient.uploadFromFile(anyString())).thenReturn(Mono.error(new UncheckedIOException(new IOException())));

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
        .delayElement(Duration.ofSeconds(15))
        .as(StepVerifier::create)
        .expectNext(Boolean.FALSE)
        .verifyComplete();

    var pattern = Pattern.compile("Error saving asset to Storage");
    var matcher = pattern.matcher(output.getOut());
    var count = 0;
    while (matcher.find()) {
      count++;
    }
    assertThat(count).isEqualTo(3);
  }
}
