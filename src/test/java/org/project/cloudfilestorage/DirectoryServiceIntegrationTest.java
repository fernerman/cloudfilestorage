package org.project.cloudfilestorage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.project.cloudfilestorage.dto.resource.StorageResourceResponseDto;
import org.project.cloudfilestorage.dto.resource.StorageResourceResponseDto.ResourceType;
import org.project.cloudfilestorage.exception.FolderAlreadyExistsException;
import org.project.cloudfilestorage.exception.FolderNotFoundException;
import org.project.cloudfilestorage.service.DirectoryService;
import org.project.cloudfilestorage.util.PathUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DirectoryServiceIntegrationTest extends TestContainerConfig {

  private static final int TEST_USER_ID = 123;
  @Autowired
  private DirectoryService directoryService;

  @BeforeAll
  void init() throws Exception {
    directoryService.createUserDirectory(TEST_USER_ID);
  }

  @Test
  public void testCreateEmptyDirectory_success() throws Exception {
    String path = "docs/";
    StorageResourceResponseDto dto = directoryService.createEmptyDirectory(path, TEST_USER_ID);

    assertNotNull(dto);
    assertEquals(path, dto.getName());
  }
  @Test
  void createEmptyDirectory_shouldFailWhenParentNotFound() {
    String path = "noSuchParent/childDir/";

    FolderNotFoundException exception = assertThrows(
        FolderNotFoundException.class,
        () -> directoryService.createEmptyDirectory(path, TEST_USER_ID)
    );
    assertTrue(exception.getMessage().contains("Not found"));
  }
  @Test
  void createEmptyDirectory_shouldFailWhenFolderAlreadyExists() throws Exception {
    String path = "existingDir/";

    directoryService.createEmptyDirectory(path, TEST_USER_ID);

    assertThrows(
        FolderAlreadyExistsException.class,
        () -> directoryService.createEmptyDirectory(path, TEST_USER_ID)
    );
  }
  @Test
  void getContentDirectory_returnsExpectedItems() throws Exception {
    String path = "folder1/folder2/";
    var absolutePath=PathUtil.getAbsolutePath(TEST_USER_ID, path);
    directoryService.createRecursiveVirtualFolders(absolutePath);

    directoryService.createEmptyDirectory(path + "sub1/", TEST_USER_ID);
    directoryService.createEmptyDirectory(path + "sub2/", TEST_USER_ID);

    List<StorageResourceResponseDto> contents = directoryService.getContentDirectory(path, TEST_USER_ID);

    assertEquals(2, contents.size());
    assertTrue(contents.stream().anyMatch(item -> item.getName().equals("sub1/") && item.getType()==ResourceType.DIRECTORY));
    assertTrue(contents.stream().anyMatch(item -> item.getName().equals("sub2/") && item.getType()==ResourceType.DIRECTORY));
  }
}
