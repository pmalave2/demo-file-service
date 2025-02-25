package com.demos.file_service.application.dto;

import org.springframework.http.codec.multipart.FilePart;

import jakarta.validation.constraints.NotNull;

public record AssetFileUploadFormRequest(@NotNull FilePart file) {
}
