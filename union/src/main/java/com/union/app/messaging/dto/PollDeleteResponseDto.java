package com.union.app.messaging.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Ответ при удалении голосования")
public class PollDeleteResponseDto {
    @Schema(description = "Сообщение", example = "Poll deleted successfully")
    private String message;

    @Schema(description = "ID удаленного голосования", example = "1")
    private Long pollId;

    @Schema(description = "Статус", example = "success")
    private String status;
}