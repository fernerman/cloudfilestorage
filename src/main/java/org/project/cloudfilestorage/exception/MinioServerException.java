package org.project.cloudfilestorage.exception;

public class MinioServerException extends RuntimeException {

  private static final String message = "Minio Server Exception";

  public MinioServerException() {
    super(message);
  }
}
