package com.union.app.messaging.service.impl;

import com.union.app.common.util.MessageEncryptor;
import com.union.app.exception.event.AccessNotAllowedException;
import com.union.app.messaging.service.UnionService;
import com.union.app.user.model.User;
import com.union.app.messaging.dto.MessageDto;
import com.union.app.messaging.model.Message;
import com.union.app.messaging.model.Union;
import com.union.app.messaging.repository.MessageRepository;
import com.union.app.messaging.repository.UnionRepository;
import com.union.app.messaging.service.MessageService;
import com.union.app.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {
    private final MessageRepository messageRepository;

    private final UnionRepository unionRepository;

    private final UserService userService;

    private final UnionService unionService;

    private final MessageEncryptor messageEncryptor;

    @Override
    public List<Message> getMessagesForUnion(Long unionId, User currentUser) {
        if (!unionService.isMember(unionId, currentUser)) {
            throw new AccessNotAllowedException("Access to the community is not allowed");
        }
        Union union = unionRepository.findById(unionId)
                .orElseThrow(() -> new RuntimeException("Union not found"));
        List<Message> messages = messageRepository.findByUnionOrderByTimestampAsc(union);

        messages.forEach(this::decryptMessage);

        return messages;
    }

    @Override
    public List<Message> getPrivateMessages(Long userId1, Long userId2) {
        List<Message> messages = messageRepository.findPrivateMessages(userId1, userId2);

        messages.forEach(this::decryptMessage);

        return messages;
    }

    @Override
    @Transactional
    public Message saveMessage(MessageDto dto, User sender) {
        Message message = new Message();

        String encryptedContent = encryptContent(dto.getContent());
        message.setContent(encryptedContent);
        message.setSender(sender);

        if (dto.getUnionId() != null) {
            Union union = unionRepository.findById(dto.getUnionId())
                    .orElseThrow(() -> new RuntimeException("Union not found"));
            message.setUnion(union);
        } else if (dto.getReceiverId() != null) {
            User receiver = userService.getUserById(dto.getReceiverId());
            message.setReceiver(receiver);
        } else {
            throw new RuntimeException("Either unionId or receiverId must be provided");
        }

        return messageRepository.save(message);
    }

    @Override
    public Optional<Message> findById(Long messageId) {
        Optional<Message> message = messageRepository.findById(messageId);

        message.ifPresent(this::decryptMessage);

        return message;
    }

    @Override
    @Transactional
    public Message editMessage(Long messageId, String content) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));

        String encryptedContent = encryptContent(content);
        message.setContent(encryptedContent);

        return messageRepository.save(message);
    }

    @Override
    @Transactional
    public void deleteMessage(Long messageId) {
        messageRepository.deleteById(messageId);
    }

    private String encryptContent(String plainText) {
        if (plainText == null || plainText.isEmpty()) {
            return plainText;
        }

        try {
            String encrypted = messageEncryptor.encrypt(plainText);
            return encrypted;
        } catch (Exception e) {
            throw new RuntimeException("Failed to encrypt message", e);
        }
    }

    private void decryptMessage(Message message) {
        if (message == null || message.getContent() == null || message.getContent().isEmpty()) {
            return;
        }

        try {
            String decrypted = messageEncryptor.decrypt(message.getContent());
            message.setContent(decrypted);
        } catch (Exception e) {
            message.setContent("[Encrypted message - unable to decrypt]");
        }
    }
}