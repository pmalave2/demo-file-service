package com.demos.file_service.application.http;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.demos.file_service.application.dto.AssetFileUploadFormRequest;
import com.demos.file_service.application.dto.AssetFileUploadRequest;
import com.demos.file_service.application.dto.AssetFileUploadResponse;
import com.demos.file_service.application.dto.AssetGetResponse;
import com.demos.file_service.domain.Asset;
import com.demos.file_service.domain.AssetFilterParams;
import com.demos.file_service.domain.mapper.AssetMapper;
import com.demos.file_service.domain.service.FileService;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@AllArgsConstructor
@RestController
@RequestMapping(AssetsController.BASE_URI)
public class AssetsController {

  public static final String BASE_URI = "/api/mgmt/1/assets";
  public static final String ASSET_UPLOAD_URI = "/actions/upload";

  private static AssetMapper mapper = AssetMapper.INSTANCE;
  private FileService fileService;

  @GetMapping
  public Flux<AssetGetResponse> get(@Valid AssetFilterParams params) {
    return fileService.getAssets(params)
        .map(mapper::toGetResponse);
  }

  @PostMapping(path = ASSET_UPLOAD_URI, consumes = MediaType.APPLICATION_JSON_VALUE)
  @ResponseStatus(HttpStatus.ACCEPTED)
  public Mono<AssetFileUploadResponse> post(@Valid @RequestBody AssetFileUploadRequest request) {
    return fileService.uploadFile(request.filename(), request.encodedFile(), request.contentType())
        .map(Asset::getId)
        .map(UUID::toString)
        .map(AssetFileUploadResponse::new);
  }

  @PostMapping(path = ASSET_UPLOAD_URI, consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseStatus(HttpStatus.ACCEPTED)
  public Mono<AssetFileUploadResponse> post(@Valid @ModelAttribute AssetFileUploadFormRequest form) {
    return fileService.uploadFile(form.file())
        .map(Asset::getId)
        .map(UUID::toString)
        .map(AssetFileUploadResponse::new);
  }
}
