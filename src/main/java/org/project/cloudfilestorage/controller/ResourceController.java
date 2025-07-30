package org.project.cloudfilestorage.controller;

import static org.project.cloudfilestorage.config.SecurityConfig.API;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.project.cloudfilestorage.docs.resource.DeleteResourceSwaggerDoc;
import org.project.cloudfilestorage.docs.resource.DownloadResourceSwaggerDoc;
import org.project.cloudfilestorage.docs.resource.ObtainResourceSwaggerDoc;
import org.project.cloudfilestorage.docs.resource.ResourceManipulationSwaggerDoc;
import org.project.cloudfilestorage.docs.resource.SearchFunctionalitySwaggerDoc;
import org.project.cloudfilestorage.docs.resource.UploadResourceSwaggerDoc;
import org.project.cloudfilestorage.dto.resource.MoveResourceRequestDto;
import org.project.cloudfilestorage.dto.resource.StorageResourceResponseDto;
import org.project.cloudfilestorage.dto.resource.UploadResourceRequestDto;
import org.project.cloudfilestorage.entity.User;
import org.project.cloudfilestorage.service.ResourceService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;


@RequestMapping(API + "/resource")
@RestController
@RequiredArgsConstructor
@Validated
public class ResourceController {

  private final ResourceService resourceService;

  @ObtainResourceSwaggerDoc
  @GetMapping()
  public ResponseEntity<?> getResource(
      @RequestParam("path") String path,
      @AuthenticationPrincipal User user)
      throws Exception {
    StorageResourceResponseDto responseStorageResourceDTO = resourceService
        .getResource(path, user.getId());
    return ResponseEntity.ok(responseStorageResourceDTO);
  }

  @UploadResourceSwaggerDoc
  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<?> uploadResource(
      @RequestParam("path") String path,
      @RequestParam("object") List<MultipartFile> file,
      @AuthenticationPrincipal User user) throws Exception {
    UploadResourceRequestDto uploadResourceRequestDto = new UploadResourceRequestDto(user.getId(),
        file, path);
    List<StorageResourceResponseDto> uploadedResources = resourceService.uploadResource(
        uploadResourceRequestDto);
    return ResponseEntity.status(HttpStatus.CREATED).body(uploadedResources);
  }

  @DeleteResourceSwaggerDoc
  @DeleteMapping()
  public ResponseEntity<Void> deleteResource(@RequestParam("path") String path,
      @AuthenticationPrincipal User user)
      throws Exception {
    resourceService.deleteResource(path, user.getId());
    return ResponseEntity.noContent().build();
  }

  @DownloadResourceSwaggerDoc
  @GetMapping("/download")
  public ResponseEntity<?> downloadResource(
      @RequestParam("path") String path,
      @AuthenticationPrincipal User user) throws Exception {
    InputStream zipStream = resourceService.downloadResource(path, user.getId());
    InputStreamResource resource = new InputStreamResource(zipStream);
    HttpHeaders httpHeaders = createBinaryEntryFileHeaders(path);
    return new ResponseEntity<>(resource, httpHeaders, HttpStatus.OK);
  }

  @ResourceManipulationSwaggerDoc
  @GetMapping("/move")
  public ResponseEntity<?> moveResource(
      @RequestParam("from") String from,
      @RequestParam("to") String to,
      @AuthenticationPrincipal User user) throws Exception {
    MoveResourceRequestDto moveResourceRequestDto = new MoveResourceRequestDto(from, to,
        user.getId());
    StorageResourceResponseDto dto = resourceService.moveResource(moveResourceRequestDto);
    return ResponseEntity.ok(dto);
  }

  @SearchFunctionalitySwaggerDoc
  @GetMapping("/search")
  public ResponseEntity<?> searchResource(
      @RequestParam("query") String query,
      @AuthenticationPrincipal User user) throws Exception {
    List<StorageResourceResponseDto> storageResourceResponseDtoList = resourceService.searchResources(
        query, user.getId());
    return ResponseEntity.ok(storageResourceResponseDtoList);
  }

  private HttpHeaders createBinaryEntryFileHeaders(String path) {
    HttpHeaders headers = new HttpHeaders();

    String fileName = Paths.get(path).getFileName().toString();

    if (path.endsWith("/")) {
      fileName += ".zip";
    }

    headers.setContentDisposition(ContentDisposition.builder("attachment")
        .filename(fileName, StandardCharsets.UTF_8)
        .build());

    headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
    return headers;
  }
}
