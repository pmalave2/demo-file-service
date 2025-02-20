package com.demos.file_service.domain.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.springframework.http.MediaType;

import com.demos.file_service.domain.Asset;
import com.demos.file_service.infrastructure.orm.entity.AssetEntity;

@Mapper
public interface AssetMapper {

  AssetMapper INSTANCE = Mappers.getMapper(AssetMapper.class);

  @Mapping(target = "uploadDate", source = "creationDate")
  Asset toDomain(AssetEntity entity);

  default MediaType toMediaType(String contentType) {
    return MediaType.parseMediaType(contentType);
  }
}
