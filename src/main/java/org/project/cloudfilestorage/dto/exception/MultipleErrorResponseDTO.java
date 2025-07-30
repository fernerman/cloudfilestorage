package org.project.cloudfilestorage.dto.exception;

import java.util.List;

public record MultipleErrorResponseDTO(List<String> errors) {

}
