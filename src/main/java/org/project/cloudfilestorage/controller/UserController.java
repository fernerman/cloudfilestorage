package org.project.cloudfilestorage.controller;

import static org.project.cloudfilestorage.config.SecurityConfig.API;
import static org.project.cloudfilestorage.config.SecurityConfig.LOGOUT_ENTRY_POINT;
import static org.project.cloudfilestorage.config.SecurityConfig.SIGNIN_ENTRY_POINT;
import static org.project.cloudfilestorage.config.SecurityConfig.SIGNUP_ENTRY_POINT;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.project.cloudfilestorage.docs.auth.AuthenticationSwaggerDoc;
import org.project.cloudfilestorage.docs.auth.RegistrationSwaggerDoc;
import org.project.cloudfilestorage.dto.user.UserRequestDTO;
import org.project.cloudfilestorage.dto.user.UserResponseDTO;
import org.project.cloudfilestorage.entity.User;
import org.project.cloudfilestorage.service.user.UserAuthService;
import org.project.cloudfilestorage.service.user.UserRegistrationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping(API)
@RestController
@RequiredArgsConstructor
public class UserController {

  private final UserRegistrationService userRegistrationService;
  private final UserAuthService userAuthService;

  @RegistrationSwaggerDoc
  @PostMapping(SIGNUP_ENTRY_POINT)
  public ResponseEntity<?> signUp(@Valid @RequestBody UserRequestDTO userRequestDTO,
      HttpServletRequest httpRequest,
      HttpServletResponse httpResponse
      )
      throws Exception {
    UserResponseDTO user = userRegistrationService.registerUser(userRequestDTO,httpRequest,httpResponse);
    return new ResponseEntity<>(user, HttpStatus.CREATED);
  }

  @AuthenticationSwaggerDoc
  @PostMapping(SIGNIN_ENTRY_POINT)
  public ResponseEntity<?> signIn(@Valid @RequestBody UserRequestDTO userRequestDTO,
      HttpServletRequest httpRequest,
      HttpServletResponse httpResponse) {
    Authentication auth = userAuthService.authenticate(userRequestDTO, httpRequest, httpResponse);
    UserResponseDTO responseDTO = new UserResponseDTO(auth.getName());
    return new ResponseEntity<>(responseDTO, HttpStatus.OK);
  }

  @PostMapping(LOGOUT_ENTRY_POINT)
  public ResponseEntity<Void> signOut(HttpServletRequest request, HttpServletResponse response) {
    userAuthService.logout(request, response);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/user/me")
  public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal User user) {
    return new ResponseEntity<>(new UserResponseDTO(user.getUsername()), HttpStatus.OK);
  }
}
