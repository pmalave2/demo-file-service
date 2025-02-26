package com.demos.file_service.infrastructure.orm;

import static com.demos.file_service.TestFileServiceApplication.PROFILE;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.Resource;
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator;
import org.springframework.test.context.ActiveProfiles;

import com.demos.file_service.TestcontainersConfiguration;
import com.demos.file_service.domain.Asset;
import com.demos.file_service.domain.AssetFilterParams;
import com.demos.file_service.domain.repository.PersistenceRepository;

import io.r2dbc.spi.ConnectionFactory;
import reactor.test.StepVerifier;

@Import({ TestcontainersConfiguration.class })
@SpringBootTest
@ActiveProfiles(PROFILE)
class PersistenceRepositoryImplTest {

  @Autowired
  PersistenceRepository persistenceRepository;

  @BeforeEach
  void setUp(@Value("classpath:static/test-data-2.sql") Resource testDataSql,
      @Autowired ConnectionFactory connectionFactory) {
    var resourceDatabasePopulator = new ResourceDatabasePopulator();
    resourceDatabasePopulator.addScript(testDataSql);
    resourceDatabasePopulator.populate(connectionFactory).block();
  }

  @Test
  void insertShouldSaveAsset() {
    var asset = Asset.builder().filename("test-insert-file").build();

    var result = persistenceRepository.insert(asset);

    StepVerifier.create(result)
        .assertNext(saved -> assertThat(saved.getFilename()).isEqualTo("test-insert-file"))
        .verifyComplete();
  }

  @Test
  void updateShouldModifyAsset() {
    var asset = Asset.builder().filename("old-name").build();

    persistenceRepository.insert(asset).block();
    asset.setFilename("new-name");

    var result = persistenceRepository.update(asset);

    StepVerifier.create(result)
        .assertNext(updated -> assertThat(updated.getFilename()).isEqualTo("new-name"))
        .verifyComplete();
  }

  @Test
  void selectShouldRetrieveAssets() {
    var asset1 = Asset.builder().filename("select-file1").build();
    var asset2 = Asset.builder().filename("select-file2").build();

    persistenceRepository.insert(asset1).block();
    persistenceRepository.insert(asset2).block();

    StepVerifier.create(persistenceRepository.select(new AssetFilterParams()))
        .expectNextCount(2)
        .verifyComplete();
  }
}
