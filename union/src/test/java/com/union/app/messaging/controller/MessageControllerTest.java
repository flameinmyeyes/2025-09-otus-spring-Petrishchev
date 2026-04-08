package com.union.app.messaging.controller;

import com.union.app.messaging.dto.MessageDto;
import com.union.app.messaging.model.Message;
import com.union.app.messaging.model.Union;
import com.union.app.messaging.service.MessageService;
import com.union.app.testsupport.TestFixtures;
import com.union.app.user.model.User;
import com.union.app.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MessageControllerTest {

    @Test
    void handlesRestAndWebSocketMessaging() {
        MessageService messageService = mock(MessageService.class);
        UserService userService = mock(UserService.class);
        SimpMessagingTemplate template = mock(SimpMessagingTemplate.class);
        MessageController controller = new MessageController(messageService, userService, template);
        User current = TestFixtures.user(1L, "79990001111");
        User receiver = TestFixtures.user(2L, "79990001122");
        Union union = TestFixtures.union(10L, "Union", current, current, receiver);
        Message unionMessage = TestFixtures.message(100L, "union", current, union, null, LocalDateTime.now());
        Message privateMessage = TestFixtures.message(101L, "private", current, null, receiver, LocalDateTime.now());

        MessageDto unionDto = new MessageDto();
        unionDto.setContent("union");
        unionDto.setUnionId(10L);
        MessageDto privateDto = new MessageDto();
        privateDto.setContent("private");
        privateDto.setReceiverId(2L);

        when(userService.getCurrentUser()).thenReturn(current);
        when(messageService.getMessagesForUnion(10L, current)).thenReturn(List.of(unionMessage));
        when(messageService.getPrivateMessages(1L, 2L)).thenReturn(List.of(privateMessage));
        when(messageService.saveMessage(same(unionDto), eq(current))).thenReturn(unionMessage);
        when(messageService.saveMessage(same(privateDto), eq(current))).thenReturn(privateMessage);
        when(messageService.editMessage(100L, "edited")).thenReturn(privateMessage);
        when(messageService.findById(100L)).thenReturn(Optional.of(unionMessage));
        when(messageService.findById(101L)).thenReturn(Optional.of(privateMessage));
        when(messageService.findById(999L)).thenReturn(Optional.empty());
        when(userService.getUserByPhoneNumber("79990001111")).thenReturn(current);
        when(userService.getUserById(2L)).thenReturn(receiver);

        assertEquals(1, controller.getUnionMessages(10L).size());
        assertEquals(1, controller.getPrivateMessages(2L).size());
        assertEquals(101L, controller.editMessage(100L, Map.of("content", "edited")).getId());
        assertTrue(controller.deleteMessage(100L).getStatusCode().is2xxSuccessful());
        assertTrue(controller.deleteMessage(101L).getStatusCode().is2xxSuccessful());
        assertTrue(controller.deleteMessage(999L).getStatusCode().is2xxSuccessful());

        Principal principal = () -> "79990001111";
        assertEquals(101L, controller.sendWebSocketMessage(privateDto, principal).getId());

        assertThrows(RuntimeException.class, () -> controller.sendWebSocketMessage(privateDto, null));
        assertThrows(org.springframework.web.server.ResponseStatusException.class, () -> controller.editMessage(1L, Map.of()));
    }
}
