package org.project.cloudfilestorage.dto.resource;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public record UploadResourceRequestDto(int userId, List<MultipartFile>  files, String path) {

}
