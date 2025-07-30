package org.project.cloudfilestorage.docs.auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.project.cloudfilestorage.dto.user.UserResponseDTO;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Operation(
        tags = {"Authorization"},
        summary = "Register new user",
        description = "Registers a new user and returns basic user information",
        responses = {
                @ApiResponse(
                        responseCode = "201",
                        description = "User created",
                        content = @Content(schema = @Schema(implementation = UserResponseDTO.class))
                ),
                @ApiResponse(
                        responseCode = "400",
                        description = "Validation error",
                        content = @Content(schema = @Schema(implementation = UserResponseDTO.class))
                ),
                @ApiResponse(
                        responseCode = "409",
                        description = "Username already exists",
                        content = @Content(schema = @Schema(implementation = UserResponseDTO.class))
                ),
                @ApiResponse(
                        responseCode = "500",
                        description = "Internal server error",
                        content = @Content(schema = @Schema(implementation = UserResponseDTO.class))
                )
        }
)
public @interface RegistrationSwaggerDoc {
}