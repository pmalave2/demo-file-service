package com.demos.file_service.infrastructure.exception;

public class SaveAssetException extends RuntimeException {

  public static final String MESSAGE = "Error saving asset to Storage";

  public SaveAssetException(Throwable cause) {
    super(MESSAGE, cause);
  }
}
