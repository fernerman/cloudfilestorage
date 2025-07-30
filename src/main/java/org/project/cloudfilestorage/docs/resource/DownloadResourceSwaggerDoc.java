package org.project.cloudfilestorage.docs.resource;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.MediaType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Operation(
    tags = {"Resource management"},
    summary = "Download resource from storage",
    description = "Download resource from remote storage by given path. " +
        "If the resource is a directory, it is returned as a zip archive.",
    responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Resource returned successfully. " +
                "If resource is a directory, zip archive with all contents is returned; " +
                "if resource is a file, the file content is returned.",
            content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE)
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid or missing path parameter"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized user"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Resource not found"
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error"
        )
    }
)
public @interface DownloadResourceSwaggerDoc {
}

