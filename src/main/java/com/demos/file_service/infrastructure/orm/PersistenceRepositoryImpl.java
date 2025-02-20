package com.demos.file_service.infrastructure.orm;

import org.springframework.stereotype.Service;

import com.demos.file_service.domain.repository.PersistenceRepository;
import com.demos.file_service.infrastructure.orm.entity.AssetEntity;
import com.demos.file_service.infrastructure.orm.repositories.AssetRepository;

import lombok.AllArgsConstructor;
import reactor.core.publisher.Mono;

@AllArgsConstructor
@Service
public class PersistenceRepositoryImpl implements PersistenceRepository {

  private AssetRepository assetRepository;

  @Override
  public Mono<AssetEntity> save(AssetEntity entity) {
    return assetRepository.save(entity);
  }
}
