package org.project.cloudfilestorage.exception;

import io.minio.errors.ErrorResponseException;

public class ResourceNotFoundException extends RuntimeException {

  public ResourceNotFoundException(String message) {
    super(message);
  }

}
