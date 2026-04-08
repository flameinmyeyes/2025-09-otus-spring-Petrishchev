package com.union.app.messaging.controller;

import com.union.app.messaging.dto.EventCreateDto;
import com.union.app.messaging.dto.EventUpdateDto;
import com.union.app.messaging.model.Event;
import com.union.app.messaging.model.Union;
import com.union.app.messaging.service.EventService;
import com.union.app.testsupport.TestFixtures;
import com.union.app.user.model.User;
import com.union.app.user.service.UserService;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EventControllerTest {

    @Test
    void handlesEventEndpoints() {
        EventService eventService = mock(EventService.class);
        UserService userService = mock(UserService.class);
        EventController controller = new EventController(eventService, userService);
        User current = TestFixtures.user(1L, "79990001111");
        Union union = TestFixtures.union(10L, "Union", current, current);
        Event event = TestFixtures.event(100L, "Meetup", "desc", LocalDateTime.now().plusDays(1), "online", union, current, LocalDateTime.now());

        when(userService.getCurrentUser()).thenReturn(current);
        when(eventService.createEvent(eq(10L), any(), eq(current))).thenReturn(event);
        when(eventService.updateEvent(eq(100L), any(), eq(current))).thenReturn(event);
        when(eventService.getEventsForUnion(10L)).thenReturn(List.of(event));

        EventCreateDto createDto = new EventCreateDto();
        createDto.setTitle("Meetup");
        createDto.setDescription("desc");
        createDto.setLocation("online");
        EventUpdateDto updateDto = new EventUpdateDto();
        updateDto.setTitle("Meetup");
        updateDto.setDescription("desc");
        updateDto.setLocation("online");

        assertEquals(100L, controller.createEvent(10L, createDto).getId());
        assertEquals(100L, controller.updateEvent(100L, updateDto).getId());
        assertTrue(controller.deleteEvent(100L).getStatusCode().is2xxSuccessful());
        assertEquals(1, controller.getEvents(10L).size());
    }
}
