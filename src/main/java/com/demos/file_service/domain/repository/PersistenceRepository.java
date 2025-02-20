package com.demos.file_service.domain.repository;

import com.demos.file_service.infrastructure.orm.entity.AssetEntity;

import reactor.core.publisher.Mono;

public interface PersistenceRepository {

  Mono<AssetEntity> save(AssetEntity entity);
}
