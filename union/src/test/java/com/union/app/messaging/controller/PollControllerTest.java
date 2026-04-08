package com.union.app.messaging.controller;

import com.union.app.messaging.dto.PollCreateDto;
import com.union.app.messaging.dto.PollUpdateDto;
import com.union.app.messaging.model.Poll;
import com.union.app.messaging.model.Union;
import com.union.app.messaging.service.PollService;
import com.union.app.testsupport.TestFixtures;
import com.union.app.user.model.User;
import com.union.app.user.service.UserService;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PollControllerTest {

    @Test
    void handlesPollEndpoints() {
        PollService pollService = mock(PollService.class);
        UserService userService = mock(UserService.class);
        PollController controller = new PollController(pollService, userService);
        User current = TestFixtures.user(1L, "79990001111");
        Union union = TestFixtures.union(10L, "Union", current, current);
        Poll poll = TestFixtures.poll(100L, "Question", union, current, LocalDateTime.now(), LocalDateTime.now().plusDays(1),
                TestFixtures.option(1L, "A"), TestFixtures.option(2L, "B"));

        when(userService.getCurrentUser()).thenReturn(current);
        when(pollService.createPoll(eq(10L), any(PollCreateDto.class), eq(current))).thenReturn(poll);
        when(pollService.updatePoll(eq(100L), any(PollUpdateDto.class), eq(current))).thenReturn(poll);
        when(pollService.getPollResults(100L, current)).thenReturn(com.union.app.messaging.dto.PollResponseDto.fromEntity(poll, current));
        when(pollService.getPollsByUnion(10L, current)).thenReturn(List.of(com.union.app.messaging.dto.PollResponseDto.fromEntity(poll, current)));

        PollCreateDto createDto = new PollCreateDto();
        createDto.setQuestion("Question");
        createDto.setOptions(List.of("A", "B"));
        PollUpdateDto updateDto = new PollUpdateDto();
        updateDto.setQuestion("Question");
        updateDto.setOptions(List.of("A", "B"));

        assertEquals(100L, controller.createPoll(10L, createDto).getId());
        assertEquals(100L, controller.updatePoll(100L, updateDto).getId());
        assertTrue(controller.vote(100L, 1L).getStatusCode().is2xxSuccessful());
        assertEquals(100L, controller.getPollResults(100L).getBody().getId());
        assertEquals("success", controller.deletePoll(100L).getBody().getStatus());
        assertEquals(1, controller.getPollsByUnion(10L).size());
    }
}
