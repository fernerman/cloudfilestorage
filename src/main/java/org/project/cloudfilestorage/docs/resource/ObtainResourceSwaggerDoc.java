package org.project.cloudfilestorage.docs.resource;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.project.cloudfilestorage.dto.resource.StorageResourceResponseDto;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Operation(
        tags = {"Resource management"},
        summary = "Get resource info",
        description = "Obtain resource metadata under given path",
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Resource has been returned successfully",
                        content = @Content(schema = @Schema(implementation = StorageResourceResponseDto.class))
                )
        }
)
public @interface ObtainResourceSwaggerDoc {
}
