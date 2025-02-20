package com.demos.file_service.application.dto;

import org.springframework.web.multipart.MultipartFile;

public record AssetFileUploadFormRequest(MultipartFile file) {
}
