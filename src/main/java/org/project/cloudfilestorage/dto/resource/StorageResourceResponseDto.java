package org.project.cloudfilestorage.dto.resource;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StorageResourceResponseDto {

  private String path;
  private String name;
  private Long size;
  private ResourceType type;

  public enum ResourceType {
    FILE,
    DIRECTORY
  }
}