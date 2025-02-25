package com.demos.file_service.domain.repository;

import com.demos.file_service.domain.Asset;
import com.demos.file_service.domain.AssetFilterParams;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PersistenceRepository {

  Mono<Asset> insert(Asset asset);

  Mono<Asset> update(Asset asset);

  Flux<Asset> select(AssetFilterParams params);
}
