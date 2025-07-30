package org.project.cloudfilestorage.mapper.helper;

import java.util.UUID;
import org.project.cloudfilestorage.dto.resource.StorageResourceResponseDto;
import org.project.cloudfilestorage.util.PathUtil;

public class ResourceDtoHelper {

  public static StorageResourceResponseDto toResourceDtoCommon(String absolutePath, boolean isDir,
      Long size, int userId) {
    String relativePath = PathUtil.getRelativePath(absolutePath, userId);
    String folderPath = PathUtil.getFolderPath(relativePath);
    String name = PathUtil.getResourceNameFromPath(relativePath);
    if (isDir == true) {
      name += "/";
    }
    StorageResourceResponseDto.ResourceType type =
        isDir ? StorageResourceResponseDto.ResourceType.DIRECTORY
            : StorageResourceResponseDto.ResourceType.FILE;

    return new StorageResourceResponseDto(folderPath, name, size, type);
  }
}
