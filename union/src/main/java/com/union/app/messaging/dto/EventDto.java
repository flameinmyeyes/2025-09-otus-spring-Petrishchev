package com.union.app.messaging.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.union.app.messaging.model.Event;
import com.union.app.user.dto.UserDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "DTO для ответа с информацией о событии")
public class EventDto {

    private Long id;

    private String title;

    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime eventDate;

    private String location;

    private UserDto createdBy;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime createdAt;

    public static EventDto fromEntity(Event event) {
        if (event == null) {
            return null;
        }

        EventDto dto = new EventDto();
        dto.setId(event.getId());
        dto.setTitle(event.getTitle());
        dto.setDescription(event.getDescription());
        dto.setLocation(event.getLocation());
        dto.setCreatedBy(UserDto.fromEntity(event.getCreatedBy()));
        dto.setCreatedAt(event.getCreatedAt());
        return dto;
    }
}