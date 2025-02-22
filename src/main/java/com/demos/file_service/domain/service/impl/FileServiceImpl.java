package com.demos.file_service.domain.service.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Objects;

import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;

import com.demos.file_service.domain.Asset;
import com.demos.file_service.domain.repository.PersistenceRepository;
import com.demos.file_service.domain.repository.StorageRepository;
import com.demos.file_service.domain.service.FileService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@AllArgsConstructor
@Service
public class FileServiceImpl implements FileService {

  private PersistenceRepository persistenceRepository;
  private StorageRepository storageRepository;

  @Override
  public Mono<Asset> uploadFile(String filename, String encodedFile, String contentType) {
    return getAsset(filename, encodedFile, contentType)
        .flatMap(persistenceRepository::save)
        .doOnNext(elem -> storageRepository.saveAsset(elem).flatMap(persistenceRepository::save).subscribe());
  }

  @Override
  public Mono<Asset> uploadFile(FilePart file) {
    return getAsset(file)
        .flatMap(persistenceRepository::save)
        .doOnNext(elem -> storageRepository.saveAsset(elem).flatMap(persistenceRepository::save).subscribe());
  }

  private Mono<Asset> getAsset(String filename, String encodedFile, String contentType) {
    try {
      var path = Files.createTempFile(null, "");
      var decoder = Base64.getDecoder();
      var decodedFile = decoder.decode(encodedFile);
      Files.write(path, decodedFile);

      log.info("File {} has been stored in {}", filename, path);
      log.info("File content: {}", path.toFile().length());

      return Mono.just(buildAsset(filename, contentType, path));
    } catch (IOException ex) {
      return Mono.error(ex);
    }
  }

  private Mono<Asset> getAsset(FilePart file) {
    try {
      var path = Files.createTempFile(null, "");

      DataBufferUtils.write(file.content(), path).block();
      log.info("File {} has been stored in {}", file.filename(), path);
      log.info("File content: {}", path.toFile().length());

      String contentType = Objects.requireNonNullElse(file.headers().getContentType(),
          MediaTypeFactory.getMediaType(file.filename()).orElse(MediaType.APPLICATION_OCTET_STREAM)).toString();

      return Mono.just(buildAsset(file.filename(), contentType, path));
    } catch (IOException ex) {
      return Mono.error(ex);
    }
  }

  private Asset buildAsset(String filename, String contentType, Path path) {
    return Asset.builder()
        .filename(filename)
        .contentType(MediaType.valueOf(contentType))
        .size(path.toFile().length())
        .path(path)
        .uploadDate(LocalDateTime.now())
        .build();
  }
}
