package com.demos.file_service.domain;

import java.time.LocalDateTime;

import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class AssetFilterParams {

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private LocalDateTime uploadDateStart;
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  private LocalDateTime uploadDateEnd;
  private String filename;
  private String filetype;
  private Sort.Direction sortDirection = Sort.Direction.DESC;
}
