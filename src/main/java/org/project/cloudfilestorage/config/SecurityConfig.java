package org.project.cloudfilestorage.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.project.cloudfilestorage.dto.exception.UnauthorizedEntryPoint;
import org.project.cloudfilestorage.filter.UnauthorizedLogoutFilter;
import org.project.cloudfilestorage.service.SecurityContextService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  public static final String API = "/api";
  public static final String SIGNUP_ENTRY_POINT = "/auth/sign-up";
  public static final String SIGNIN_ENTRY_POINT = "/auth/sign-in";
  public static final String LOGOUT_ENTRY_POINT = "/auth/sign-out";
  public static final String ERROR_ENTRY_POINT = "/error";

  public static final String SWAGGER_UI_POINT = API + "/swagger-ui/**";
  public static final String SWAGGER_UI_HTML_POINT = API + "swagger-ui.html";

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public SecurityContextRepository securityContextRepository() {
    return new HttpSessionSecurityContextRepository();
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
      throws Exception {
    return config.getAuthenticationManager();
  }

  @Bean
  public UnauthorizedLogoutFilter unauthorizedLogoutFilter(
      SecurityContextService securityContextService,
      ObjectMapper objectMapper) {
    return new UnauthorizedLogoutFilter(securityContextService, objectMapper, "/api/auth/sign-out");
  }

  @Bean
  SecurityFilterChain filterChain(
      HttpSecurity http,
      UnauthorizedEntryPoint unauthorizedEntryPoint,
      UnauthorizedLogoutFilter unauthorizedLogoutFilter,
      SecurityContextRepository securityContextRepository) throws Exception {
    http
        .csrf(AbstractHttpConfigurer::disable)
        .httpBasic(AbstractHttpConfigurer::disable)
        .securityContext(context -> context
            .securityContextRepository(securityContextRepository)
        )
        .exceptionHandling(configurer ->
            configurer.authenticationEntryPoint(unauthorizedEntryPoint))
        .authorizeHttpRequests(authorize -> authorize
            .requestMatchers(
                API + SIGNIN_ENTRY_POINT,
                API + SIGNUP_ENTRY_POINT,
                API + LOGOUT_ENTRY_POINT,
                ERROR_ENTRY_POINT,
                SWAGGER_UI_POINT,
                SWAGGER_UI_HTML_POINT,
                "/registration",
                "/login",
                "/files/**",
                "/config.js", "/assets/**",
                "/api/swagger-ui.html",
                "/api/swagger-ui/**",
                "/api/v3/api-docs",
                "/api/v3/api-docs/**",
                "/swagger-ui/**",
                "/", "/index.html"
            ).permitAll()
            .anyRequest().authenticated()
        )
        .addFilterBefore(unauthorizedLogoutFilter, LogoutFilter.class)
        .logout(logout -> logout
            .logoutUrl("/api/auth/sign-out")
            .logoutSuccessHandler(
                new HttpStatusReturningLogoutSuccessHandler(HttpStatus.NO_CONTENT))
        );
    return http.build();
  }

  @Bean
  public SecurityContextHolderStrategy securityContextHolderStrategy() {
    return SecurityContextHolder.getContextHolderStrategy();
  }
}
