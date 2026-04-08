package com.union.app.messaging.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UnionCreateDto {
    @NotBlank
    private String name;

    private String description;
}