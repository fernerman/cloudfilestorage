package org.project.cloudfilestorage.dto.resource;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;


public record MoveResourceRequestDto(String from,String to,int id) {
}



