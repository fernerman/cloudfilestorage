package org.project.cloudfilestorage.service.user;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.project.cloudfilestorage.dto.user.UserRequestDTO;
import org.project.cloudfilestorage.dto.user.UserResponseDTO;
import org.project.cloudfilestorage.exception.InvalidPasswordException;
import org.project.cloudfilestorage.service.SecurityContextService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserAuthService {

  private final AuthenticationManager authenticationManager;
  private final SecurityContextService securityContextService;

  @Transactional(readOnly = true)
  public Authentication  authenticate(UserRequestDTO userRequestDTO,
      HttpServletRequest request, HttpServletResponse response) {
    try {
      Authentication auth = authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(userRequestDTO.getUsername(),
              userRequestDTO.getPassword())
      );

      securityContextService.applyAuthenticationContext(auth, request, response);
      return auth;
    } catch (BadCredentialsException e) {
      throw new InvalidPasswordException("Invalid username or password");
    }
  }

  @Transactional
  public void logout(HttpServletRequest request, HttpServletResponse response) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth != null) {
      new SecurityContextLogoutHandler().logout(request, response, auth);
    }
  }
}
