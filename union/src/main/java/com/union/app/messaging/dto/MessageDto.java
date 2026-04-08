package com.union.app.messaging.dto;

import com.union.app.user.dto.UserDto;
import com.union.app.messaging.model.Message;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MessageDto {
    private Long id;

    private String content;

    private UserDto sender;

    private Long unionId;

    private Long receiverId;

    private LocalDateTime timestamp;

    public static MessageDto fromEntity(Message message) {
        if (message == null) {
            return null;
        }
        MessageDto dto = new MessageDto();
        dto.setId(message.getId());
        dto.setContent(message.getContent());
        dto.setSender(UserDto.fromEntity(message.getSender()));
        dto.setTimestamp(message.getTimestamp());

        if (message.getUnion() != null) {
            dto.setUnionId(message.getUnion().getId());
        }
        if (message.getReceiver() != null) {
            dto.setReceiverId(message.getReceiver().getId());
        }

        return dto;
    }
}