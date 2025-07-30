package org.project.cloudfilestorage.docs.resource;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.project.cloudfilestorage.dto.exception.ErrorResponseDto;
import org.project.cloudfilestorage.dto.resource.StorageResourceResponseDto;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Operation(
        tags = {"Resource management"},
        summary = "Move or rename resource",
        description = "Move or rename resource, given source and target resource path",
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "target file under new path or name",
                        content = @Content(schema = @Schema(implementation = StorageResourceResponseDto.class))
                ),
                @ApiResponse(
                        responseCode = "409",
                        description = "Resource under target path already exists",
                        content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
                )
        }
)
public @interface ResourceManipulationSwaggerDoc {
}
