package com.union.app.messaging.service.impl;

import com.union.app.common.util.MessageEncryptor;
import com.union.app.messaging.dto.MessageDto;
import com.union.app.messaging.model.Message;
import com.union.app.messaging.model.Union;
import com.union.app.messaging.repository.MessageRepository;
import com.union.app.messaging.repository.UnionRepository;
import com.union.app.messaging.service.UnionService;
import com.union.app.testsupport.TestFixtures;
import com.union.app.user.model.User;
import com.union.app.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MessageServiceImplTest {

    private MessageEncryptor messageEncryptor;

    @BeforeEach
    void setUp() {
        messageEncryptor = new MessageEncryptor();
        org.springframework.test.util.ReflectionTestUtils.setField(messageEncryptor, "password", "mySecretPassword123");
        org.springframework.test.util.ReflectionTestUtils.setField(messageEncryptor, "salt", "a1b2c3d4e5f67890");
        messageEncryptor.init();
    }

    @Test
    void loadsUnionMessagesAndPrivateMessages() {
        User user = TestFixtures.user(1L, "79990001111");

        MessageRepository messageRepository = mock(MessageRepository.class);
        UnionRepository unionRepository = mock(UnionRepository.class);
        UserService userService = mock(UserService.class);
        UnionService unionService = mock(UnionService.class);

        Union union = TestFixtures.union(10L, "Union", user, user);

        String encryptedContent = messageEncryptor.encrypt("Test message");
        Message testMessage = TestFixtures.message(
                1L, encryptedContent, user, union, null, LocalDateTime.now()
        );

        when(unionService.isMember(10L, user)).thenReturn(true);
        when(unionRepository.findById(10L)).thenReturn(Optional.of(union));
        when(messageRepository.findByUnionOrderByTimestampAsc(union))
                .thenReturn(List.of(testMessage));

        when(messageRepository.findPrivateMessages(1L, 2L))
                .thenReturn(List.of());

        MessageServiceImpl service = new MessageServiceImpl(
                messageRepository, unionRepository, userService, unionService, messageEncryptor
        );

        List<Message> messages = service.getMessagesForUnion(10L, user);

        assertEquals(1, messages.size());
        assertEquals("Test message", messages.get(0).getContent()); // расшифрован

        assertTrue(service.getPrivateMessages(1L, 2L).isEmpty());

        assertThrows(RuntimeException.class,
                () -> service.getMessagesForUnion(11L, user));
    }

    @Test
    void savesMessageInEncryptedForm() {
        MessageRepository messageRepository = mock(MessageRepository.class);
        UnionRepository unionRepository = mock(UnionRepository.class);
        UserService userService = mock(UserService.class);
        UnionService unionService = mock(UnionService.class);

        User sender = TestFixtures.user(1L, "79990001111");
        Union union = TestFixtures.union(10L, "Union", sender, sender);

        when(unionRepository.findById(10L)).thenReturn(Optional.of(union));

        when(messageRepository.save(any(Message.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        MessageServiceImpl service = new MessageServiceImpl(
                messageRepository, unionRepository, userService, unionService, messageEncryptor
        );

        MessageDto dto = new MessageDto();
        dto.setContent("Hello");
        dto.setUnionId(10L);

        Message result = service.saveMessage(dto, sender);

        assertNotEquals("Hello", result.getContent());

        String decrypted = messageEncryptor.decrypt(result.getContent());
        assertEquals("Hello", decrypted);
    }

    @Test
    void editsMessageInEncryptedForm() {
        MessageRepository messageRepository = mock(MessageRepository.class);
        UnionRepository unionRepository = mock(UnionRepository.class);
        UserService userService = mock(UserService.class);
        UnionService unionService = mock(UnionService.class);

        User user = TestFixtures.user(1L, "79990001111");

        Message existing = TestFixtures.message(
                1L,
                messageEncryptor.encrypt("Old"),
                user,
                null,
                null,
                LocalDateTime.now()
        );

        when(messageRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(messageRepository.save(any(Message.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        MessageServiceImpl service = new MessageServiceImpl(
                messageRepository, unionRepository, userService, unionService, messageEncryptor
        );

        Message updated = service.editMessage(1L, "New content");

        assertNotEquals("New content", updated.getContent());

        String decrypted = messageEncryptor.decrypt(updated.getContent());
        assertEquals("New content", decrypted);
    }

    @Test
    void handlesEncryptionErrors() {
        MessageRepository messageRepository = mock(MessageRepository.class);
        UnionRepository unionRepository = mock(UnionRepository.class);
        UserService userService = mock(UserService.class);
        UnionService unionService = mock(UnionService.class);

        MessageEncryptor brokenEncryptor = mock(MessageEncryptor.class);
        when(brokenEncryptor.encrypt(anyString()))
                .thenThrow(new RuntimeException("Encryption failed"));

        User sender = TestFixtures.user(1L, "79990001111");

        MessageDto dto = new MessageDto();
        dto.setContent("test");
        dto.setUnionId(10L);

        Union union = TestFixtures.union(10L, "Union", sender, sender);
        when(unionRepository.findById(10L)).thenReturn(Optional.of(union));

        MessageServiceImpl service = new MessageServiceImpl(
                messageRepository, unionRepository, userService, unionService, brokenEncryptor
        );

        assertThrows(RuntimeException.class,
                () -> service.saveMessage(dto, sender));

        verify(messageRepository, never()).save(any());
    }
}