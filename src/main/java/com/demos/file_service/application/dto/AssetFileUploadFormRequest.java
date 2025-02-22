package com.demos.file_service.application.dto;

import org.springframework.http.codec.multipart.FilePart;

public record AssetFileUploadFormRequest(FilePart file) {
}
