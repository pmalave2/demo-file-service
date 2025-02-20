package com.demos.file_service.infrastructure.orm.repositories;

import java.util.UUID;

import org.springframework.data.r2dbc.repository.R2dbcRepository;

import com.demos.file_service.infrastructure.orm.entity.AssetEntity;

public interface AssetRepository extends R2dbcRepository<AssetEntity, UUID> {
}
