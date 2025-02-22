package com.demos.file_service.infrastructure.orm;

import org.springframework.stereotype.Service;

import com.demos.file_service.domain.Asset;
import com.demos.file_service.domain.repository.PersistenceRepository;
import com.demos.file_service.domain.service.mapper.AssetMapper;
import com.demos.file_service.infrastructure.orm.repositories.AssetRepository;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@AllArgsConstructor
@Service
public class PersistenceRepositoryImpl implements PersistenceRepository {

  private static AssetMapper assetMapper = AssetMapper.INSTANCE;
  private AssetRepository assetRepository;

  @Override
  public Mono<Asset> save(Asset asset) {
    log.info("Saving asset {}", asset);
    return assetRepository.save(assetMapper.toEntity(asset))
        .map(elem -> assetMapper.updateDomainFromEntity(asset, elem));
  }
}
