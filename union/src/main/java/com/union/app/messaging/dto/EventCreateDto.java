package com.union.app.messaging.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "DTO для создания события")
public class EventCreateDto {

    @NotBlank
    @Schema(description = "Название события", example = "Встреча Java разработчиков")
    private String title;

    @Schema(description = "Описание события", example = "Обсуждение новых технологий")
    private String description;

    @Schema(description = "Место проведения", example = "Конференц-зал, г. Москва")
    private String location;
}
