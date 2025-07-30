package org.project.cloudfilestorage.service.user;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.project.cloudfilestorage.dto.user.UserRequestDTO;
import org.project.cloudfilestorage.dto.user.UserResponseDTO;
import org.project.cloudfilestorage.entity.User;
import org.project.cloudfilestorage.exception.UserAlreadyExistsException;
import org.project.cloudfilestorage.repository.UserRepository;
import org.project.cloudfilestorage.service.DirectoryService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserRegistrationService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final DirectoryService directoryService;
  private final UserAuthService userAuthService;

  @Transactional
  public UserResponseDTO registerUser(UserRequestDTO userRequestDTO, HttpServletRequest httpRequest,
      HttpServletResponse httpResponse) throws Exception {
    Optional<User> userInfo = userRepository.findByUsername(userRequestDTO.getUsername());
    if (userInfo.isPresent()) {
      throw new UserAlreadyExistsException("User already exists");
    }
    User savedUser = userRepository.save(
        new User(
            userRequestDTO.getUsername(),
            passwordEncoder.encode(userRequestDTO.getPassword())));
    directoryService.createUserDirectory(savedUser.getId());
    userAuthService.authenticate(userRequestDTO, httpRequest, httpResponse);
    return new UserResponseDTO(savedUser.getUsername());
  }
}
