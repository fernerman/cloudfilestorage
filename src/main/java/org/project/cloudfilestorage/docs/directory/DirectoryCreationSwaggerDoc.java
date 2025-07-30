package org.project.cloudfilestorage.docs.directory;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.Explode;
import io.swagger.v3.oas.annotations.enums.ParameterStyle;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.project.cloudfilestorage.dto.exception.ErrorResponseDto;
import org.project.cloudfilestorage.dto.resource.StorageResourceResponseDto;

@Operation(
        tags = {"Directory management"},
        summary = "Create empty directory",
        description = "Creates an empty directory at the given path",
        parameters = {
                @Parameter(
                        name = "path",
                        description = "Path to directory",
                        required = true,
                        style = ParameterStyle.FORM,
                        explode = Explode.FALSE
                )
        },
        responses = {
                @ApiResponse(
                        responseCode = "201",
                        description = "Directory created",
                        content = @Content(schema = @Schema(implementation = StorageResourceResponseDto.class))
                ),
                @ApiResponse(
                        responseCode = "409",
                        description = "Directory already exists",
                        content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
                )
        }
)
public @interface DirectoryCreationSwaggerDoc {
}
