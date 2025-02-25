package com.demos.file_service.domain;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.http.MediaType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class Asset {

  private UUID id;
  private String filename;
  private MediaType contentType;
  private String url;
  private long size;
  private LocalDateTime uploadDate;
  private Path path;

  public static Asset buildAsset(String filename, String contentType, Path path) {
    return Asset.builder()
        .filename(filename)
        .contentType(MediaType.valueOf(contentType))
        .size(path.toFile().length())
        .path(path)
        .build();
  }
}
