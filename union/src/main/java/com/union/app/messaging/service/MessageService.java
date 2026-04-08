package com.union.app.messaging.service;

import com.union.app.user.model.User;
import com.union.app.messaging.dto.MessageDto;
import com.union.app.messaging.model.Message;
import java.util.List;
import java.util.Optional;

public interface MessageService {
    List<Message> getMessagesForUnion(Long unionId, User currentUser);

    List<Message> getPrivateMessages(Long userId1, Long userId2);

    Message saveMessage(MessageDto dto, User sender);

    Optional<Message> findById(Long messageId);

    Message editMessage(Long messageId, String content);

    void deleteMessage(Long messageId);
}