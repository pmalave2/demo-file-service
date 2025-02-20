package com.demos.file_service.domain.service.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.Base64;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.demos.file_service.domain.Asset;
import com.demos.file_service.domain.repository.PersistenceRepository;
import com.demos.file_service.domain.service.FileService;
import com.demos.file_service.domain.service.mapper.AssetMapper;
import com.demos.file_service.infrastructure.orm.entity.AssetEntity;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@AllArgsConstructor
@Service
public class FileServiceImpl implements FileService {

  private PersistenceRepository persistenceRepository;
  private static AssetMapper assetMapper = AssetMapper.INSTANCE;

  @Override
  public Mono<Asset> uploadFile(String filename, String encodedFile, String contentType) {
    return getEntity(filename, encodedFile, contentType)
        .flatMap(persistenceRepository::save)
        .map(assetMapper::toDomain);
  }

  @Override
  public Mono<Asset> uploadFile(MultipartFile file) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'uploadFile'");
  }

  Mono<AssetEntity> getEntity(String filename, String encodedFile, String contentType) {
    try {
      var path = Files.createTempFile(null, null);
      var decoder = Base64.getDecoder();
      var decodedFile = decoder.decode(encodedFile);
      Files.write(path, decodedFile);

      log.info("File {} has been stored in {}", filename, path);
      log.info("File content: {}", Files.readAllLines(path));

      return Mono.just(AssetEntity.builder()
          .filename(filename)
          .contentType(contentType)
          .size(path.toFile().length())
          .creationDate(LocalDateTime.now())
          .build());
    } catch (IOException ex) {
      return Mono.error(ex);
    }
  }
}
