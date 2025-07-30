package org.project.cloudfilestorage.service;

import io.minio.BucketExistsArgs;
import io.minio.CopyObjectArgs;
import io.minio.CopySource;
import io.minio.GetObjectArgs;
import io.minio.ListObjectsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.RemoveObjectsArgs;
import io.minio.Result;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.MinioException;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.project.cloudfilestorage.exception.MinioServerException;
import org.project.cloudfilestorage.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MinioService {

  private final MinioClient minioClient;
  @Value("${minio.bucket}")
  private String bucket;

  public void initBucketIfNotExists() throws Exception {
    boolean exists = minioClient.bucketExists(
        BucketExistsArgs.builder().bucket(bucket).build());
    if (!exists) {
      minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
    }
  }

  public StatObjectResponse statObject(String path) throws Exception {
    try {
      return minioClient.statObject(StatObjectArgs.builder()
          .bucket(bucket)
          .object(path)
          .build());
    } catch (io.minio.errors.ErrorResponseException e) {
      if (e.errorResponse().code().equals("NoSuchKey")) {
        throw new ResourceNotFoundException("Object not found in path: " + path);
      } else {
        throw new MinioException("Minio Exception" + e.getMessage());
      }
    }
    catch (Exception e){
      throw new MinioException("Unexpected error while statObject: " + e.getMessage());
    }
  }

  public Iterable<Result<Item>> getObjectItems(String prefix, boolean recursive) throws Exception {
    try {
      return minioClient.listObjects(
          ListObjectsArgs.builder()
              .bucket(bucket)
              .prefix(prefix)
              .recursive(recursive)
              .build()
      );
    } catch (Exception e) {
      throw new MinioException(e.getMessage());
    }
  }

  public void removeObject(String path) throws Exception {
    try {
      minioClient.removeObject(
          RemoveObjectArgs.builder()
              .bucket(bucket)
              .object(path)
              .build());
    } catch (Exception e) {
      throw new MinioException(e.getMessage());
    }
  }

  public void removeObjects(Iterable<Result<Item>> results) throws Exception {
    try {
      List<DeleteObject> objectsToDelete = new ArrayList<>();

      for (Result<Item> result : results) {
        Item item = result.get();
        objectsToDelete.add(new DeleteObject(item.objectName()));
      }

      Iterable<Result<DeleteError>> errors = minioClient.removeObjects(
          RemoveObjectsArgs.builder()
              .bucket(bucket)
              .objects(objectsToDelete)
              .build()
      );

      for (Result<DeleteError> error : errors) {
        DeleteError err = error.get();
        throw new MinioException(
            "Failed to delete object: " + err.objectName() + ", " + err.message());
      }

    } catch (Exception e) {
      throw new MinioException("Ошибка при удалении объектов: " + e.getMessage());
    }
  }


  public void copyObject(String from, String to) throws Exception {
    try {

      minioClient.copyObject(
          CopyObjectArgs.builder()
              .bucket(bucket)
              .object(to)
              .source(CopySource.builder()
                  .bucket(bucket)
                  .object(from)
                  .build())
              .build()
      );
    } catch (Exception e) {
      throw new MinioException(e.getMessage());
    }
  }

  public InputStream getObject(String objectName) throws Exception {
    try {
      return minioClient.getObject(
          GetObjectArgs.builder()
              .bucket(bucket)
              .object(objectName)
              .build()
      );
    } catch (ErrorResponseException e) {
      if (e.errorResponse().code().equals("NoSuchKey")) {
        throw new ResourceNotFoundException("Resource not found " + objectName);
      }
       throw new RuntimeException(e);

    }
    catch (Exception e){
      throw new RuntimeException("Ошибка получения объекта: " + objectName, e);
    }
  }
  public boolean objectExists(String path) {
    try {
      minioClient.statObject(
          StatObjectArgs.builder()
              .bucket(bucket)
              .object(path)
              .build()
      );
      return true;
    } catch (ErrorResponseException e) {
      if (e.errorResponse().code().equals("NoSuchKey")) {
        return false;
      }
      throw new MinioServerException();
    } catch (Exception e) {
      throw new MinioServerException();
    }
  }

  public boolean objectExistsPrefix(String folderPath) throws MinioException {
    try {
      Iterable<Result<Item>> results = minioClient.listObjects(
          ListObjectsArgs.builder()
              .bucket(bucket)
              .prefix(folderPath)
              .maxKeys(1)
              .build()
      );
      return results.iterator().hasNext();
    } catch (Exception e) {
      throw new MinioException("Ошибка при проверке папки: " + folderPath);
    }
  }

  public void putObject(String objectName, InputStream stream, long size, String contentType)
      throws Exception {
    try {
      minioClient.putObject(
          PutObjectArgs.builder()
              .bucket(bucket)
              .object(objectName)
              .stream(stream, size, -1)
              .contentType(contentType)
              .build()
      );
    } catch (Exception e) {
      throw new MinioException(e.getMessage());
    }
  }
}
