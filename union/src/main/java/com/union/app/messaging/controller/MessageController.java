package com.union.app.messaging.controller;

import com.union.app.user.model.User;
import com.union.app.messaging.dto.MessageDto;
import com.union.app.messaging.model.Message;
import com.union.app.messaging.service.MessageService;
import com.union.app.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PutMapping;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
@Tag(name = "Messages", description = "API для работы с сообщениями")
@SecurityRequirement(name = "BearerAuth")
public class MessageController {
    private final MessageService messageService;

    private final UserService userService;

    private final SimpMessagingTemplate messagingTemplate;

    @GetMapping("/listMessageByUnion/{unionId}")
    @Operation(summary = "Получить все сообщения в объединении",
            description = "Возвращает список всех сообщений в указанном объединении (групповой чат)")
    public List<MessageDto> getUnionMessages(@PathVariable Long unionId) {
        User currentUser = userService.getCurrentUser();
        List<Message> messages = messageService.getMessagesForUnion(unionId, currentUser);
        return messages.stream()
                .map(MessageDto::fromEntity)
                .collect(Collectors.toList());
    }

    @GetMapping("/private/list/{userId}")
    @Operation(summary = "Получить приватные сообщения с пользователем",
            description = "Возвращает все сообщения между текущим пользователем и указанным")
    public List<MessageDto> getPrivateMessages(@PathVariable Long userId) {
        User currentUser = userService.getCurrentUser();
        List<Message> messages = messageService.getPrivateMessages(currentUser.getId(), userId);
        return messages.stream()
                .map(MessageDto::fromEntity)
                .collect(Collectors.toList());
    }

    @PutMapping("/edit/{messageId}")
    @Operation(summary = "Редактировать сообщение",
            description = "Изменяет содержимое существующего сообщения")
    public MessageDto editMessage(@PathVariable Long messageId, @RequestBody Map<String, String> payload) {
        String content = payload.get("content");
        if (content == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "content is required");
        }

        Message message = messageService.editMessage(messageId, content);
        MessageDto response = MessageDto.fromEntity(message);
        broadcastMessage(response);
        return response;
    }

    @DeleteMapping("/delete/{messageId}")
    @Operation(summary = "Удалить сообщение",
            description = "Удаляет сообщение по ID")
    public ResponseEntity<?> deleteMessage(@PathVariable Long messageId) {
        MessageDto messageDto = messageService.findById(messageId)
                .map(MessageDto::fromEntity)
                .orElse(null);

        messageService.deleteMessage(messageId);

        if (messageDto != null) {
            broadcastDeletion(messageDto);
        }

        return ResponseEntity.ok().build();
    }

    @MessageMapping("/chat.sendMessage")
    @Operation(summary = "Отправить сообщение через WebSocket",
            description = "WebSocket endpoint для отправки сообщений в реальном времени")
    public MessageDto sendWebSocketMessage(@Payload MessageDto messageDto,
                                           Principal principal) {
        if (principal == null) {
            throw new RuntimeException("User not authenticated");
        }
        User sender = userService.getUserByPhoneNumber(principal.getName());

        Message message = messageService.saveMessage(messageDto, sender);
        MessageDto response = MessageDto.fromEntity(message);
        broadcastMessage(response);
        return response;
    }

    private void broadcastMessage(MessageDto message) {
        if (message.getUnionId() != null) {
            messagingTemplate.convertAndSend("/topic/union/" + message.getUnionId(), message);
            return;
        }

        if (message.getSender() != null) {
            messagingTemplate.convertAndSend("/topic/private/" + message.getSender().getId(), message);
        }
        if (message.getReceiverId() != null) {
            messagingTemplate.convertAndSend("/topic/private/" + message.getReceiverId(), message);
        }
    }

    private void broadcastDeletion(MessageDto message) {
        if (message.getUnionId() != null) {
            messagingTemplate.convertAndSend("/topic/union/" + message.getUnionId() + "/deleted",
                    message.getId());
            return;
        }

        if (message.getSender() != null) {
            messagingTemplate.convertAndSend("/topic/private/" + message.getSender().getId() + "/deleted",
                    message.getId());
        }
        if (message.getReceiverId() != null) {
            messagingTemplate.convertAndSend("/topic/private/" + message.getReceiverId() + "/deleted",
                    message.getId());
        }
    }
}
