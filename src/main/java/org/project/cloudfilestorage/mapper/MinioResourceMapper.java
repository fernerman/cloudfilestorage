package org.project.cloudfilestorage.mapper;

import io.minio.StatObjectResponse;
import io.minio.messages.Item;
import java.util.UUID;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import org.project.cloudfilestorage.dto.resource.StorageResourceResponseDto;
import org.project.cloudfilestorage.mapper.helper.ResourceDtoHelper;

@Mapper
public interface MinioResourceMapper {

  MinioResourceMapper INSTANCE = Mappers.getMapper(MinioResourceMapper.class);

  default StorageResourceResponseDto toResourceDto(Item item, int userId) {
    if (item == null) {
      throw new IllegalArgumentException("Resource cannot be null.");
    }
    return ResourceDtoHelper.toResourceDtoCommon(item.objectName(), item.isDir(), item.size(), userId);
  }

  default StorageResourceResponseDto toResourceDto(StatObjectResponse response, int userId) {
    if (response == null) {
      throw new IllegalArgumentException("Response cannot be null.");
    }
    boolean isDir = response.object().endsWith("/");
    Long size = isDir ? null : response.size();
    return ResourceDtoHelper.toResourceDtoCommon(response.object(), isDir, size, userId);
  }
}
