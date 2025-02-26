package com.demos.file_service.infrastructure.orm;

import static org.springframework.data.relational.core.query.Criteria.from;
import static org.springframework.data.relational.core.query.Criteria.where;
import static org.springframework.data.relational.core.query.Query.query;

import java.util.ArrayList;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Sort;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.stereotype.Service;

import com.demos.file_service.domain.Asset;
import com.demos.file_service.domain.AssetFilterParams;
import com.demos.file_service.domain.mapper.AssetMapper;
import com.demos.file_service.domain.repository.PersistenceRepository;
import com.demos.file_service.infrastructure.orm.entity.AssetEntity;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@AllArgsConstructor
@Service
public class PersistenceRepositoryImpl implements PersistenceRepository {

  private static AssetMapper assetMapper = AssetMapper.INSTANCE;

  private R2dbcEntityTemplate template;

  @Override
  public Mono<Asset> insert(Asset asset) {
    return template.insert(assetMapper.toEntity(asset))
        .map(elem -> assetMapper.updateDomainFromEntity(asset, elem))
        .doOnSubscribe(sub -> log.trace("Saving asset {}", asset));
  }

  @Override
  public Mono<Asset> update(Asset asset) {
    return template.update(assetMapper.toEntity(asset))
        .map(elem -> assetMapper.updateDomainFromEntity(asset, elem))
        .doOnSubscribe(sub -> log.trace("Updating asset {}", asset));
  }

  @Override
  public Flux<Asset> select(AssetFilterParams params) {
    return template.select(AssetEntity.class)
        .matching(query(filter(params)).sort(sort(params.getSortDirection())))
        .all()
        .map(assetMapper::toDomain);
  }

  private Criteria filter(AssetFilterParams params) {
    var criterias = new ArrayList<Criteria>();

    if (Objects.nonNull(params.getUploadDateStart()))
      criterias.add(where(AssetEntity.Fields.creationDate).greaterThanOrEquals(params.getUploadDateStart()));

    if (Objects.nonNull(params.getUploadDateEnd()))
      criterias.add(where(AssetEntity.Fields.creationDate).lessThanOrEquals(params.getUploadDateEnd()));

    if (StringUtils.isNotBlank(params.getFilename()))
      criterias.add(where(AssetEntity.Fields.filename).like("%" + params.getFilename() + "%"));

    if (StringUtils.isNotBlank(params.getFiletype()))
      criterias.add(where(AssetEntity.Fields.contentType).like("%" + params.getFiletype() + "%"));

    return from(criterias);
  }

  private Sort sort(Sort.Direction direction) {
    return Sort.by(direction, AssetEntity.Fields.creationDate)
        .and(Sort.by(Sort.Direction.ASC, AssetEntity.Fields.filename));
  }
}
