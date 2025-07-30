package org.project.cloudfilestorage.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.project.cloudfilestorage.dto.exception.ErrorResponseDto;
import org.project.cloudfilestorage.service.SecurityContextService;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

public class UnauthorizedLogoutFilter extends OncePerRequestFilter {

  private final SecurityContextService securityContextService;
  private final ObjectMapper objectMapper;
  private final String logoutPath;

  public UnauthorizedLogoutFilter(SecurityContextService securityContextService,
      ObjectMapper objectMapper,
      String logoutPath) {
    this.securityContextService = securityContextService;
    this.objectMapper = objectMapper;
    this.logoutPath = logoutPath;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain)
      throws ServletException, IOException {

    boolean isLogoutRequest = request.getMethod().equalsIgnoreCase("POST")
        && request.getRequestURI().equals(logoutPath);

    if (isLogoutRequest && !securityContextService.isAuthenticated()) {
      response.setStatus(HttpStatus.UNAUTHORIZED.value());
      response.setContentType("application/json;charset=UTF-8");
      ErrorResponseDto errorResponse = new ErrorResponseDto(
          "Not authenticated users are not allowed to perform logout.");
      String json = objectMapper.writeValueAsString(errorResponse);
      response.getWriter().write(json);
      return;
    }

    filterChain.doFilter(request, response);
  }
}