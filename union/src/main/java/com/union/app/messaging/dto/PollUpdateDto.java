package com.union.app.messaging.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class PollUpdateDto {
    @NotBlank
    private String question;

    @Size(min = 2, message = "Добавьте минимум 2 варианта ответа")
    private List<String> options;
}
