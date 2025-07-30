package org.project.cloudfilestorage.controller;

import jakarta.validation.constraints.NotBlank;
import java.util.List;
import static org.project.cloudfilestorage.config.SecurityConfig.API;
import lombok.RequiredArgsConstructor;
import org.project.cloudfilestorage.docs.directory.DirectoryCreationSwaggerDoc;
import org.project.cloudfilestorage.docs.directory.GetDirectoryContentSwaggerDoc;
import org.project.cloudfilestorage.dto.resource.StorageResourceResponseDto;
import org.project.cloudfilestorage.entity.User;
import org.project.cloudfilestorage.service.DirectoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping(API+"/directory")
@RestController
@RequiredArgsConstructor
@Validated
public class DirectoryController {

    private final DirectoryService directoryService;

    @GetMapping
    @GetDirectoryContentSwaggerDoc
    public ResponseEntity<?> getContentDirectory(
            @RequestParam("path")  String path,
            @AuthenticationPrincipal User user) throws Exception {
        List<StorageResourceResponseDto> storageResourceResponseDtoList = directoryService.getContentDirectory(
                path,
                user.getId());
        return ResponseEntity.ok(storageResourceResponseDtoList);
    }

    @DirectoryCreationSwaggerDoc
    @PostMapping()
    public ResponseEntity<?> createDirectory(
            @RequestParam("path")   String path,
            @AuthenticationPrincipal User user) throws Exception {
        StorageResourceResponseDto dto = directoryService.createEmptyDirectory(path,user.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }
}
