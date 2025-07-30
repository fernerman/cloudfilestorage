package org.project.cloudfilestorage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.project.cloudfilestorage.dto.resource.MoveResourceRequestDto;
import org.project.cloudfilestorage.dto.resource.StorageResourceResponseDto;
import org.project.cloudfilestorage.dto.resource.StorageResourceResponseDto.ResourceType;
import org.project.cloudfilestorage.dto.resource.UploadResourceRequestDto;
import org.project.cloudfilestorage.exception.ResourceNotFoundException;
import org.project.cloudfilestorage.service.DirectoryService;
import org.project.cloudfilestorage.service.ResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.org.apache.commons.io.IOUtils;

@SpringBootTest
@Testcontainers

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ResourceServiceIntegrationTest extends TestContainerConfig {

  private static final int TEST_USER_ID = 123;
  private static final String PATH_FILE_NAME = "test/test.txt";
  private static final String PATH_FOLDER = "/";
  private static final String CONTENT = "Hello World";

  @Autowired
  private DirectoryService directoryService;
  @Autowired
  private ResourceService resourceService;

  @BeforeAll
  void init() throws Exception {
    directoryService.createUserDirectory(TEST_USER_ID);
  }

  @BeforeEach
  void setUp() throws Exception {
    uploadTestFile(PATH_FILE_NAME, PATH_FOLDER, CONTENT);
  }
  @AfterEach
  void cleanup() throws Exception {
    resourceService.deleteResource(PATH_FILE_NAME, TEST_USER_ID);
  }

  @Test
  void testUploadAndGetResource() throws Exception {
    StorageResourceResponseDto fetched = resourceService.getResource(PATH_FILE_NAME, TEST_USER_ID);
    assertEquals(PATH_FILE_NAME, fetched.getPath() + fetched.getName());
    assertEquals(ResourceType.FILE, fetched.getType());
  }

  @Test
  void testDownloadFile() throws Exception {
    InputStream stream = resourceService.downloadResource(PATH_FOLDER + PATH_FILE_NAME, TEST_USER_ID);
    byte[] downloaded = IOUtils.toByteArray(stream);

    assertArrayEquals(CONTENT.getBytes(StandardCharsets.UTF_8), downloaded);
  }

  @Test
  void testMoveResource() throws Exception {
    String from = PATH_FILE_NAME;
    String to = "test/test2.txt";

    MoveResourceRequestDto moveDto = new MoveResourceRequestDto(from, to, TEST_USER_ID);
    StorageResourceResponseDto moved = resourceService.moveResource(moveDto);

    assertEquals(to, moved.getPath() + moved.getName());
    assertThrows(ResourceNotFoundException.class, () ->
        resourceService.getResource(from, TEST_USER_ID));

    uploadTestFile(PATH_FILE_NAME, PATH_FOLDER, CONTENT);
  }

  @Test
  void testSearchResource() throws Exception {
    String fileName = "test.txt";
    List<StorageResourceResponseDto> found = resourceService.searchResources(fileName, TEST_USER_ID);
    assertFalse(found.isEmpty());
    assertThat(found.stream().anyMatch(r -> r.getName().equals(fileName))).isTrue();
  }

  @Test
  void testDeleteResource() throws Exception {
    resourceService.deleteResource(PATH_FILE_NAME, TEST_USER_ID);

    assertThrows(ResourceNotFoundException.class, () ->
        resourceService.getResource(PATH_FILE_NAME, TEST_USER_ID));
    uploadTestFile(PATH_FILE_NAME, PATH_FOLDER, CONTENT);
  }

  private void uploadTestFile(String fileName, String path, String content) throws Exception {
    byte[] contentBytes = content.getBytes(StandardCharsets.UTF_8);
    MultipartFile file = new MockMultipartFile(fileName, fileName, "text/plain", contentBytes);
    UploadResourceRequestDto requestDto = new UploadResourceRequestDto(TEST_USER_ID, List.of(file),
        path);
    resourceService.uploadResource(requestDto);
  }
}
