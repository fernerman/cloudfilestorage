package org.project.cloudfilestorage.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Component
public class SecurityContextService {

  private final SecurityContextRepository securityContextRepository;
  private final SecurityContextHolderStrategy securityContextHolderStrategy;

  public void applyAuthenticationContext(Authentication authentication,
      HttpServletRequest request, HttpServletResponse response) {

    SecurityContext context = securityContextHolderStrategy.createEmptyContext();
    context.setAuthentication(authentication);
    securityContextHolderStrategy.setContext(context);
    securityContextRepository.saveContext(context, request, response);
  }

  public boolean isAuthenticated() {
    Authentication authentication = this.securityContextHolderStrategy.getContext()
        .getAuthentication();
    return authentication != null && authentication.isAuthenticated()
        && !(authentication instanceof AnonymousAuthenticationToken);
  }
}