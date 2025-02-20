package com.demos.file_service.domain;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.http.MediaType;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class Asset {

  private UUID id;
  private String filename;
  private MediaType contentType;
  private String url;
  private long size;
  private LocalDateTime uploadDate;
}
