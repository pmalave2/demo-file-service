package com.demos.file_service.application.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;

public record AssetFileUploadRequest(
    @NotEmpty String filename,
    @NotEmpty String encodedFile,
    @NotEmpty @Pattern(regexp = "\\w+/[-+.\\w]+", message = "Invalid content type") String contentType) {
}
