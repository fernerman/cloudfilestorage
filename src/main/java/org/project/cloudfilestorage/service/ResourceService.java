package org.project.cloudfilestorage.service;

import io.minio.Result;
import io.minio.StatObjectResponse;
import io.minio.errors.MinioException;
import io.minio.messages.Item;
import jakarta.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import lombok.RequiredArgsConstructor;
import org.project.cloudfilestorage.dto.resource.MoveResourceRequestDto;
import org.project.cloudfilestorage.dto.resource.StorageResourceResponseDto;
import org.project.cloudfilestorage.dto.resource.StorageResourceResponseDto.ResourceType;
import org.project.cloudfilestorage.dto.resource.UploadResourceRequestDto;
import org.project.cloudfilestorage.exception.FileSizeLimitExceededException;
import org.project.cloudfilestorage.exception.MinioServerException;
import org.project.cloudfilestorage.exception.ResourceAlreadyExistsException;
import org.project.cloudfilestorage.exception.ResourceNotFoundException;
import org.project.cloudfilestorage.mapper.MinioResourceMapper;
import org.project.cloudfilestorage.util.PathUtil;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ResourceService {

  private final MinioService minioService;
  private final DirectoryService directoryService;

  @PostConstruct
  public void init() throws Exception {
    minioService.initBucketIfNotExists();
  }

  public StorageResourceResponseDto getResource(String path, int userId) throws Exception {
    String absolutePath = PathUtil.getAbsolutePath(userId, path);

    if (minioService.objectExists(absolutePath)) {
      StatObjectResponse stat = minioService.statObject(absolutePath);
      return MinioResourceMapper.INSTANCE.toResourceDto(stat, userId);
    }
    throw new ResourceNotFoundException("Ресурс не найден: " + path);
  }

  public void deleteResource(String path, int userId) throws Exception {
    String absolutePath = PathUtil.getAbsolutePath(userId, path);
    StorageResourceResponseDto resourceDto = getResource(path, userId);

    if (resourceDto.getType() == ResourceType.DIRECTORY) {
      deleteDirectory(absolutePath);
    }
    minioService.removeObject(absolutePath);
  }

  public InputStream downloadResource(String path, int userId) throws Exception {
    StorageResourceResponseDto resource = getResource(path, userId);
    String absolutePath = PathUtil.getAbsolutePath(userId, path);

    return switch (resource.getType()) {
      case DIRECTORY -> downloadDirectoryAsZip(absolutePath);
      case FILE -> downloadFile(absolutePath);
    };
  }

  public InputStream downloadFile(String objectName) {
    try {
      return minioService.getObject(objectName);
    } catch (Exception e) {
      throw new MinioServerException();
    }
  }

  public StorageResourceResponseDto moveResource(MoveResourceRequestDto moveResourceRequestDto)
      throws Exception {
    String fromPath = moveResourceRequestDto.from();
    String toPath = moveResourceRequestDto.to();
    int userId = moveResourceRequestDto.id();

    String absolutePathFrom = PathUtil.getAbsolutePath(userId, fromPath);
    String absolutePathTo = PathUtil.getAbsolutePath(userId, toPath);

    if (minioService.objectExists(absolutePathTo)) {
      throw new ResourceAlreadyExistsException(
          "Ресурс уже существует по пути: " + absolutePathTo);
    }
    StorageResourceResponseDto resourceDto = getResource(fromPath, userId);
    if (resourceDto.getType() == ResourceType.DIRECTORY) {
      moveDirectory(absolutePathFrom, absolutePathTo);
    } else if (resourceDto.getType() == ResourceType.FILE) {
      moveFile(absolutePathFrom, absolutePathTo);
    }
    return getResource(toPath, userId);
  }

  public List<StorageResourceResponseDto> searchResources(String query, int userId)
      throws Exception {
    ArrayList<StorageResourceResponseDto> list = new ArrayList<StorageResourceResponseDto>();

    PathUtil.validateName(query);
    String userPath = PathUtil.getUserRootDirectory(userId);

    Iterable<Result<Item>> items = minioService.getObjectItems(userPath, true);
    for (Result<Item> result : items) {
      String objectName = result.get().objectName();
      String relativePath = PathUtil.getRelativePath(objectName, userId);
      String resourceName = PathUtil.getLastSegmentWithoutExtension(objectName);

      if (PathUtil.matchesQuery(resourceName, query)) {
        list.add(getResource(relativePath, userId));
      }
    }
    return list;
  }

  public List<StorageResourceResponseDto> uploadResource(
      UploadResourceRequestDto uploadResourceRequestDto) throws Exception {
    List<StorageResourceResponseDto> storageResourceResponseDtoList = new ArrayList<StorageResourceResponseDto>();
    List<MultipartFile> files = uploadResourceRequestDto.files();
    int id = uploadResourceRequestDto.userId();
    String pathToFolderResource = uploadResourceRequestDto.path();

    String absolutePath = PathUtil.getAbsolutePath(id, pathToFolderResource);

    for (MultipartFile file : files) {
      if (file == null || file.isEmpty()) {
        continue;
      }
      String relativePath = file.getOriginalFilename();
      String filename = PathUtil.getResourceNameFromPath(relativePath);
      PathUtil.validateName(filename);

      String absolutePathForUpload = absolutePath + relativePath;
      String folderPath = PathUtil.getFolderPath(absolutePathForUpload);
      directoryService.createRecursiveVirtualFolders(folderPath);
      uploadSingleFile(absolutePathForUpload, file, uploadResourceRequestDto.userId());

      var dto = getResource(pathToFolderResource + relativePath, id);
      storageResourceResponseDtoList.add(dto);
    }
    return storageResourceResponseDtoList;
  }

  private InputStream downloadDirectoryAsZip(String folderPath) throws MinioException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try (ZipOutputStream zos = new ZipOutputStream(baos)) {
      Iterable<Result<Item>> items = minioService.getObjectItems(folderPath, true);

      for (Result<Item> resultItem : items) {
        Item item = resultItem.get();
        if (!item.isDir()) {
          addFileToZip(item, folderPath, zos);
        }
      }
    } catch (Exception e) {
      throw new MinioException("Failed to create ZIP archive" + e.getMessage());
    }
    return new ByteArrayInputStream(baos.toByteArray());
  }

  private void addFileToZip(Item item, String folderPath, ZipOutputStream zipOutputStream)
      throws Exception {
    String relativePath = item.objectName().substring(folderPath.length());
    zipOutputStream.putNextEntry(new ZipEntry(relativePath));
    try (InputStream objectStream = minioService.getObject(item.objectName())) {
      objectStream.transferTo(zipOutputStream);
    }
    zipOutputStream.closeEntry();
  }


  private void moveFile(String from, String to) throws Exception {
    minioService.copyObject(from, to);
    minioService.removeObject(from);
  }

  private void moveDirectory(String from, String to) throws Exception {
    Iterable<Result<Item>> objectItems = minioService.getObjectItems(
        from, true);
    for (Result<Item> result : objectItems) {
      String objectName = result.get().objectName();
      String relativePath = PathUtil.subtractBasePath(objectName, from);
      String fullPathTo = to + relativePath;
      moveFile(objectName, fullPathTo);
    }
  }

  private void deleteDirectory(String absolutePath) throws Exception {
    minioService.removeObjects(minioService.getObjectItems(absolutePath, true));
  }

  private void uploadSingleFile(String path, MultipartFile file, int id) throws Exception {
    try {
      boolean fileExists = minioService.objectExists(path);
      boolean folderExists = minioService.objectExistsPrefix(path);

      if (fileExists || folderExists) {
        throw new ResourceAlreadyExistsException("Ресурс уже существует: " + path);
      }
      minioService.putObject(path, file.getInputStream(), file.getSize(), file.getContentType());
    } catch (MaxUploadSizeExceededException e) {
      throw new FileSizeLimitExceededException("Maximum upload size exceeded 50 MB.");
    }
  }
}