package com.demos.file_service.domain.repository;

import com.demos.file_service.domain.Asset;

import reactor.core.publisher.Mono;

public interface PersistenceRepository {

  Mono<Asset> save(Asset asset);
}
