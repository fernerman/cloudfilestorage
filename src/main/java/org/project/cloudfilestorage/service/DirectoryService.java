package org.project.cloudfilestorage.service;

import io.minio.Result;
import io.minio.StatObjectResponse;
import io.minio.messages.Item;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.project.cloudfilestorage.dto.resource.StorageResourceResponseDto;
import org.project.cloudfilestorage.exception.FolderAlreadyExistsException;
import org.project.cloudfilestorage.exception.FolderNotFoundException;
import org.project.cloudfilestorage.mapper.MinioResourceMapper;
import org.project.cloudfilestorage.util.PathUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DirectoryService {

  private final MinioService minioService;

  @Transactional
  public void createUserDirectory(int userId) throws Exception {
    String userRootDirectory = PathUtil.getUserRootDirectory(userId);
    createFolderIfNotExists(userRootDirectory + "/");
  }

  @Transactional
  public StorageResourceResponseDto createEmptyDirectory(String path, int userId)
      throws Exception {
    String absolutePath = PathUtil.getAbsolutePath(userId, path);
    String parentPath = PathUtil.getParentDirectory(userId, path);

    if (!minioService.objectExistsPrefix(parentPath)) {
      throw new FolderNotFoundException("Not found " + parentPath + " parent folder");
    }

    if (minioService.objectExistsPrefix(absolutePath)) {
      throw new FolderAlreadyExistsException("Folder " + parentPath + " already exists");
    }
    minioService.putObject(
        absolutePath,
        new ByteArrayInputStream(new byte[0]),
        0,
        "application/octet-stream"
    );

    StatObjectResponse statObject = minioService.statObject(absolutePath);
    return MinioResourceMapper.INSTANCE.toResourceDto(statObject, userId);
  }

  @Transactional(readOnly = true)
  public List<StorageResourceResponseDto> getContentDirectory(String path, int userId)
      throws Exception {
    List<StorageResourceResponseDto> storageResourceResponseDtoList = new ArrayList<>();
    String absolutePath = PathUtil.getAbsolutePath(userId, path);
    Iterable<Result<Item>> results = minioService.getObjectItems(absolutePath, false);

    for (Result<Item> res : results) {
      Item item = res.get();
      if (!item.objectName().equals(absolutePath)) {
        storageResourceResponseDtoList.add(
            MinioResourceMapper.INSTANCE.toResourceDto(item, userId)
        );
      }
    }
    return storageResourceResponseDtoList;
  }

  @Transactional
  public void createRecursiveVirtualFolders(String path) {
    List<String> virtualRecursivePath = PathUtil.getVirtualRecursivePath(path);
    for (String folderPathInMinio : virtualRecursivePath) {
      createFolderIfNotExists(folderPathInMinio);
    }
  }

  private void createFolderIfNotExists(String folderPath) {
    try (InputStream empty = new ByteArrayInputStream(new byte[0])) {
      if (!minioService.objectExists(folderPath)) {
        minioService.putObject(folderPath, empty, 0, "application/octet-stream");
      }
    } catch (Exception e) {
      throw new RuntimeException("Ошибка при создании папки " + folderPath, e);
    }
  }
}
