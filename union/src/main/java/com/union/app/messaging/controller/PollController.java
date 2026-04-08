package com.union.app.messaging.controller;

import com.union.app.messaging.dto.PollCreateDto;
import com.union.app.messaging.dto.PollDeleteResponseDto;
import com.union.app.messaging.dto.PollResponseDto;
import com.union.app.messaging.dto.PollUpdateDto;
import com.union.app.messaging.model.Poll;
import com.union.app.messaging.service.PollService;
import com.union.app.user.model.User;
import com.union.app.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PutMapping;

import java.util.List;

@RestController
@RequestMapping("/api/polls")
@RequiredArgsConstructor
@Tag(name = "Polls", description = "API для создания, редактирования голосований и просмотра результатов")
@SecurityRequirement(name = "BearerAuth")
public class PollController {
    private final PollService pollService;

    private final UserService userService;

    @PostMapping("/createPoll/{unionId}")
    @Operation(
            summary = "Создать новое голосование",
            description = "Создает новое голосование в указанном объединении."
    )
    public PollResponseDto createPoll(@PathVariable Long unionId, @Valid @RequestBody PollCreateDto dto) {
        User currentUser = userService.getCurrentUser();
        Poll poll = pollService.createPoll(unionId, dto, currentUser);
        return PollResponseDto.fromEntity(poll, currentUser);
    }

    @PutMapping("/updatePoll/{pollId}")
    @Operation(
            summary = "Обновить голосование",
            description = "Позволяет автору голосования изменить вопрос и варианты ответов."
    )
    public PollResponseDto updatePoll(@PathVariable Long pollId, @Valid @RequestBody PollUpdateDto dto) {
        User currentUser = userService.getCurrentUser();
        Poll poll = pollService.updatePoll(pollId, dto, currentUser);
        return PollResponseDto.fromEntity(poll, currentUser);
    }

    @PostMapping("/vote/{pollId}/{optionId}")
    @Operation(
            summary = "Проголосовать",
            description = "Голосует за указанный вариант в голосовании. Пользователь может голосовать только один раз."
    )
    public ResponseEntity<?> vote(@PathVariable Long pollId, @PathVariable Long optionId) {
        User currentUser = userService.getCurrentUser();
        pollService.vote(pollId, optionId, currentUser);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/voteResults/{pollId}")
    @Operation(
            summary = "Получить результаты голосования",
            description = "Возвращает результаты голосования."
    )
    public ResponseEntity<PollResponseDto> getPollResults(
            @Parameter(description = "ID голосования", required = true, example = "1")
            @PathVariable Long pollId) {
        User currentUser = userService.getCurrentUser();
        PollResponseDto results = pollService.getPollResults(pollId, currentUser);
        return ResponseEntity.ok(results);
    }

    @DeleteMapping("/deletePoll/{pollId}")
    @Operation(
            summary = "Удалить голосование",
            description = "Удаляет голосование. Только создатель голосования может его удалить."
    )
    public ResponseEntity<PollDeleteResponseDto> deletePoll(
            @Parameter(description = "ID голосования", required = true, example = "1")
            @PathVariable Long pollId) {
        User currentUser = userService.getCurrentUser();
        pollService.deletePoll(pollId, currentUser);

        PollDeleteResponseDto response = new PollDeleteResponseDto(
                "Poll deleted successfully",
                pollId,
                "success"
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/listPoll/{unionId}")
    @Operation(
            summary = "Получить все голосования в объединении",
            description = "Возвращает список всех голосований в указанном объединении"
    )
    public List<PollResponseDto> getPollsByUnion(
            @Parameter(description = "ID объединения", required = true, example = "1")
            @PathVariable Long unionId) {
        User currentUser = userService.getCurrentUser();
        return pollService.getPollsByUnion(unionId, currentUser);
    }
}
