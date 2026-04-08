package com.union.app.messaging.controller;

import com.union.app.user.model.User;
import com.union.app.messaging.dto.UnionCreateDto;
import com.union.app.messaging.dto.UnionDto;
import com.union.app.messaging.model.Union;
import com.union.app.messaging.service.UnionService;
import com.union.app.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/unions")
@RequiredArgsConstructor
@Tag(name = "Unions", description = "API для работы с объединениями")
public class UnionController {
    private final UnionService unionService;

    private final UserService userService;

    @PostMapping("/createUnion")
    @Operation(summary = "Создать новое объединение")
    public UnionDto createUnion(@RequestBody UnionCreateDto dto) {
        User currentUser = userService.getCurrentUser();
        Union union = unionService.createUnion(dto, currentUser);
        return UnionDto.fromEntity(union);
    }

    @GetMapping("/myUnions")
    @Operation(summary = "Получить мои объединения",
            description = "Возвращает список объединений, в которых состоит текущий пользователь")
    public List<UnionDto> getUserUnions() {
        User currentUser = userService.getCurrentUser();
        return unionService.getUserUnions(currentUser).stream()
                .map(UnionDto::fromEntity)
                .collect(Collectors.toList());
    }

    @GetMapping("/getUnionInfo/{id}")
    @Operation(summary = "Получить объединение по ID")
    public UnionDto getUnionById(@PathVariable Long id) {
        Union union = unionService.getUnionById(id);
        return UnionDto.fromEntity(union);
    }

    @PostMapping("/{unionId}/addUserToUnion/{userId}")
    @Operation(summary = "Добавить участника в объединение")
    public ResponseEntity<?> addMember(@PathVariable Long unionId, @PathVariable Long userId) {
        unionService.addMember(unionId, userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{unionId}/removeUserFromUnion/{userId}")
    @Operation(summary = "Удалить участника из объединения")
    public ResponseEntity<?> removeMember(@PathVariable Long unionId, @PathVariable Long userId) {
        unionService.removeMember(unionId, userId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/updateUnionInfo/{id}")
    @Operation(summary = "Обновление информации об объединении")
    public UnionDto updateUnion(@PathVariable Long id, @RequestBody UnionCreateDto dto) {
        Union union = unionService.updateUnion(id, dto);
        return UnionDto.fromEntity(union);
    }

    @DeleteMapping("/deleteUnion/{id}")
    @Operation(summary = "Удаление объединения")
    public ResponseEntity<?> deleteUnion(@PathVariable Long id) {
        unionService.deleteUnion(id);
        return ResponseEntity.ok().build();
    }
}