package com.demos.file_service.domain;

import java.time.LocalDate;

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
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  private LocalDate uploadDateStart;
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  private LocalDate uploadDateEnd;
  private String filename;
  private String filetype;
  private Sort.Direction sortDirection = Sort.Direction.DESC;
}
