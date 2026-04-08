package com.union.app.messaging.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "DTO для обновления события")
public class EventUpdateDto {

    @Schema(description = "Название события", example = "Встреча Java разработчиков")
    private String title;

    @Schema(description = "Описание события", example = "Обсуждение новых технологий")
    private String description;

    @Schema(description = "Место проведения", example = "Конференц-зал, г. Москва")
    private String location;
}
