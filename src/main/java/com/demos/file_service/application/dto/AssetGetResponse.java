package com.demos.file_service.application.dto;

public record AssetGetResponse(String id, String filename, String contentType, String url, long size,
    String uploadDate) {
}
