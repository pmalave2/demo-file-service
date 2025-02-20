package com.demos.file_service.application.dto;

public record AssetFileUploadRequest(String filename, String encodedFile, String contentType) {
}
