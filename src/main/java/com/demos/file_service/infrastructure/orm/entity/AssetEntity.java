package com.demos.file_service.infrastructure.orm.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;

@AllArgsConstructor
@Getter
@Setter
@Builder
@FieldNameConstants
@Table(name = "assets")
public class AssetEntity {

  @Id
  @Column
  private String id;
  @Column
  private String filename;
  @Column("contentType")
  private String contentType;
  @Column
  private long size;
  @Column
  private String url;
  @Column("creationDate")
  @CreatedDate
  private LocalDateTime creationDate;
}
