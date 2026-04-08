package com.union.app.user.controller;

import com.union.app.user.dto.UserDto;
import com.union.app.user.model.User;
import com.union.app.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "API для управления пользователями, друзьями и профилем")
@SecurityRequirement(name = "BearerAuth")
public class UserController {
    private final UserService userService;

    @GetMapping("/userInfo")
    @Operation(summary = "Получить текущего пользователя",
            description = "Возвращает информацию о текущем аутентифицированном пользователе")
    public UserDto getCurrentUser() {
        return UserDto.fromEntity(userService.getCurrentUser());
    }

    @GetMapping("/getUser/{id}")
    @Operation(summary = "Получить пользователя по ID",
            description = "Возвращает информацию о пользователе по его ID")
    public UserDto getUserById(@PathVariable Long id) {
        return UserDto.fromEntity(userService.getUserById(id));
    }

    @PostMapping("/addFriend/{friendId}")
    @Operation(summary = "Добавить друга",
            description = "Добавляет пользователя в список друзей текущего пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Друг успешно добавлен"),
            @ApiResponse(responseCode = "400", description = "Нельзя добавить самого себя или уже друзья"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    })
    public ResponseEntity<?> addFriend(@PathVariable Long friendId) {
        userService.addFriend(friendId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/listFriends")
    @Operation(summary = "Получить список друзей",
            description = "Возвращает список всех друзей текущего пользователя")
    public List<UserDto> getFriends() {
        return userService.getCurrentUser().getFriends().stream()
                .map(UserDto::fromEntity)
                .collect(Collectors.toList());
    }

    @GetMapping("/searchUsers")
    @Operation(summary = "Поиск пользователей",
            description = "Ищет пользователей по номеру телефона или имени")
    public List<UserDto> searchUsers(@RequestParam String query) {
        return userService.searchUsers(query).stream()
                .map(UserDto::fromEntity)
                .collect(Collectors.toList());
    }

    @PutMapping("/updateUserInfo")
    @Operation(summary = "Обновить профиль",
            description = "Обновляет данные профиля текущего пользователя (имя и статус)")
    public UserDto updateProfile(@RequestBody UserDto userDto) {
        User updated = userService.updateProfile(
                userService.getCurrentUser().getId(),
                userDto.getFullName(),
                userDto.getStatus()
        );
        return UserDto.fromEntity(updated);
    }
}