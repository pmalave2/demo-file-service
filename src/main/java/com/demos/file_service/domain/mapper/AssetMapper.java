package com.demos.file_service.domain.mapper;

import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;
import org.springframework.http.MediaType;

import com.demos.file_service.application.dto.AssetGetResponse;
import com.demos.file_service.domain.Asset;
import com.demos.file_service.infrastructure.orm.entity.AssetEntity;

@Mapper
public interface AssetMapper {

  AssetMapper INSTANCE = Mappers.getMapper(AssetMapper.class);

  @Mapping(target = "asset.uploadDate", ignore = true)
  @Mapping(target = "asset.path", ignore = true)
  Asset updateDomainFromEntity(@MappingTarget Asset asset, AssetEntity entity);

  @Mapping(target = "creationDate", source = "uploadDate")
  AssetEntity toEntity(Asset domain);

  AssetGetResponse toGetResponse(Asset domain);

  @Mapping(target = "uploadDate", source = "creationDate")
  @Mapping(target = "path", ignore = true)
  Asset toDomain(AssetEntity entity);

  default MediaType toMediaType(String contentType) {
    return StringUtils.isNotBlank(contentType) ? MediaType.parseMediaType(contentType)
        : MediaType.APPLICATION_OCTET_STREAM;
  }

  default String fromMediaType(MediaType mediaType) {
    return Objects.nonNull(mediaType) ? mediaType.toString() : MediaType.APPLICATION_OCTET_STREAM_VALUE;
  }
}
