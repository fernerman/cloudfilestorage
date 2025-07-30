package org.project.cloudfilestorage;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.project.cloudfilestorage.dto.user.UserRequestDTO;
import org.project.cloudfilestorage.dto.user.UserResponseDTO;
import org.project.cloudfilestorage.entity.User;
import org.project.cloudfilestorage.exception.UserAlreadyExistsException;
import org.project.cloudfilestorage.repository.UserRepository;
import org.project.cloudfilestorage.service.user.UserRegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.testcontainers.junit.jupiter.Testcontainers;


@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class UserServiceIntegrationTest extends TestContainerConfig {

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private UserRegistrationService userRegistrationService;

  private MockHttpServletRequest request;
  private MockHttpServletResponse response;

  @BeforeEach
  void setUp() {
    request = new MockHttpServletRequest();
    response = new MockHttpServletResponse();
    userRepository.deleteAll();
  }

  @Test
  void whenCreateUser_thenItIsPersisted() throws Exception {
    UserRequestDTO requestDTO = new UserRequestDTO("test123", "test123");
    UserResponseDTO responseDTO = userRegistrationService.registerUser(requestDTO, request,
        response);

    Optional<User> userOpt = userRepository.findByUsername("test123");
    assertTrue(userOpt.isPresent());
    assertEquals("test123", userOpt.get().getUsername());
  }

  @Test
  void whenCreateUserWithDuplicateUsername_thenThrowsException() throws Exception {
    UserRequestDTO dto1 = new UserRequestDTO("test123", "test123");
    UserRequestDTO dto2 = new UserRequestDTO(dto1.getUsername(), dto1.getPassword());

    UserResponseDTO responseDTO = userRegistrationService.registerUser(dto1, request, response);
    assertThrows(UserAlreadyExistsException.class, () -> {
      userRegistrationService.registerUser(dto2, request, response);
    });
  }
}

