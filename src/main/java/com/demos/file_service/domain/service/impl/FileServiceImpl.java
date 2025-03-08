package com.demos.file_service.domain.service.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;
import java.util.Objects;

import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;

import com.demos.file_service.domain.Asset;
import com.demos.file_service.domain.AssetFilterParams;
import com.demos.file_service.domain.repository.PersistenceRepository;
import com.demos.file_service.domain.repository.StorageRepository;
import com.demos.file_service.domain.service.FileService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
@AllArgsConstructor
@Service
public class FileServiceImpl implements FileService {

  private PersistenceRepository persistenceRepository;
  private StorageRepository storageRepository;

  @Override
  public Mono<Asset> uploadFile(String filename, String encodedFile, String contentType) {
    return getAsset(filename, encodedFile, contentType)
        .flatMap(this::processAssetUpload);
  }

  @Override
  public Mono<Asset> uploadFile(FilePart file) {
    return getAsset(file)
        .flatMap(this::processAssetUpload);
  }

  private Mono<Asset> processAssetUpload(Asset asset) {
    return persistenceRepository.insert(asset)
        .doOnNext(elem -> storageRepository.saveAsset(elem).flatMap(persistenceRepository::update).subscribe());
  }

  private Mono<Asset> getAsset(String filename, String encodedFile, String contentType) {
    return Mono.defer(() -> {
      try {
        var path = Files.createTempFile(null, "");
        var decoder = Base64.getDecoder();
        var decodedFile = decoder.decode(encodedFile);
        Files.write(path, decodedFile);

        log.trace("File '{}'' has been stored in '{}'", filename, path);
        log.trace("File length: '{}'", path.toFile().length());

        return Mono.just(Asset.buildAsset(filename, contentType, path));
      } catch (IOException ex) {
        return Mono.error(ex);
      }
    })
        .subscribeOn(Schedulers.boundedElastic());
  }

  private Mono<Asset> getAsset(FilePart file) {
    return Mono.defer(() -> {
      try {
        var path = Files.createTempFile(null, "");
        DataBufferUtils.write(file.content(), path).block();
        if (Files.size(path) == 0) {
          Files.delete(path);
          return Mono.error(new IOException("File is empty"));
        }

        log.trace("File '{}'' has been stored in '{}'", file.filename(), path);
        log.trace("File length: '{}'", path.toFile().length());

        var contentType = Objects.requireNonNullElse(file.headers().getContentType(),
            MediaTypeFactory.getMediaType(file.filename()).orElse(MediaType.APPLICATION_OCTET_STREAM)).toString();

        return Mono.just(Asset.buildAsset(file.filename(), contentType, path));
      } catch (IOException ex) {
        return Mono.error(ex);
      }
    })
        .subscribeOn(Schedulers.boundedElastic());
  }

  @Override
  public Flux<Asset> getAssets(AssetFilterParams params) {
    return persistenceRepository.select(params);
  }
}
