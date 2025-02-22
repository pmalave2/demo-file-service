package com.demos.file_service.domain.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;
import org.springframework.http.MediaType;

import com.demos.file_service.domain.Asset;
import com.demos.file_service.infrastructure.orm.entity.AssetEntity;

@Mapper
public interface AssetMapper {

  AssetMapper INSTANCE = Mappers.getMapper(AssetMapper.class);

  Asset updateDomainFromEntity(@MappingTarget Asset asset, AssetEntity entity);

  @Mapping(target = "creationDate", source = "uploadDate")
  AssetEntity toEntity(Asset domain);

  default MediaType toMediaType(String contentType) {
    return MediaType.parseMediaType(contentType);
  }

  default String fromMediaType(MediaType mediaType) {
    return mediaType.toString();
  }
}
