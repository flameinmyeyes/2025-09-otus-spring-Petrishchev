package com.union.app.messaging.service.impl;

import com.union.app.exception.event.AccessNotAllowedException;
import com.union.app.messaging.dto.EventCreateDto;
import com.union.app.messaging.dto.EventUpdateDto;
import com.union.app.messaging.model.Event;
import com.union.app.messaging.model.Union;
import com.union.app.messaging.repository.EventRepository;
import com.union.app.messaging.repository.UnionRepository;
import com.union.app.messaging.service.UnionService;
import com.union.app.testsupport.TestFixtures;
import com.union.app.user.model.User;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EventServiceImplTest {

    @Test
    void createsUpdatesDeletesAndLoadsEvents() {
        EventRepository eventRepository = mock(EventRepository.class);
        UnionRepository unionRepository = mock(UnionRepository.class);
        UnionService unionService = mock(UnionService.class);
        User creator = TestFixtures.user(1L, "79990001111");
        Union union = TestFixtures.union(10L, "Union", creator, creator);
        when(unionService.isMember(10L, creator)).thenReturn(true);
        when(unionRepository.findById(10L)).thenReturn(Optional.of(union));

        EventCreateDto createDto = new EventCreateDto();
        createDto.setTitle("  Meetup  ");
        createDto.setDescription("desc");
        createDto.setLocation("online");
        Event created = TestFixtures.event(1L, "Meetup", "desc", LocalDateTime.now().plusDays(1), "online", union, creator, LocalDateTime.now());
        when(eventRepository.save(any())).thenReturn(created);

        EventServiceImpl service = new EventServiceImpl(eventRepository, unionRepository, unionService);
        Event result = service.createEvent(10L, createDto, creator);
        assertEquals("Meetup", result.getTitle());

        EventUpdateDto updateDto = new EventUpdateDto();
        updateDto.setTitle("  Updated  ");
        updateDto.setDescription("new desc");
        updateDto.setLocation("office");
        Event event = TestFixtures.event(2L, "Old", "old", LocalDateTime.now().plusDays(1), "old place", union, creator, LocalDateTime.now());
        when(eventRepository.findById(2L)).thenReturn(Optional.of(event));
        when(eventRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Event updated = service.updateEvent(2L, updateDto, creator);
        assertEquals("Updated", updated.getTitle());
        assertEquals("new desc", updated.getDescription());
        assertEquals("office", updated.getLocation());

        when(eventRepository.findById(3L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.updateEvent(3L, updateDto, creator));

        User other = TestFixtures.user(2L, "79990001122");
        when(eventRepository.findById(4L)).thenReturn(Optional.of(TestFixtures.event(4L, "x", "y", LocalDateTime.now().plusDays(1), "z", union, creator, LocalDateTime.now())));
        assertThrows(SecurityException.class, () -> service.updateEvent(4L, updateDto, other));

        when(eventRepository.findById(5L)).thenReturn(Optional.of(event));
        service.deleteEvent(5L, creator);
        verify(eventRepository).delete(event);

        when(unionRepository.findById(10L)).thenReturn(Optional.of(union));
        when(eventRepository.findByUnion(union)).thenReturn(List.of(event));
        assertEquals(List.of(event), service.getEventsForUnion(10L));

        when(unionRepository.findById(11L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.getEventsForUnion(11L));
    }

    @Test
    void rejectsNonMembersAndWrongCreators() {
        EventRepository eventRepository = mock(EventRepository.class);
        UnionRepository unionRepository = mock(UnionRepository.class);
        UnionService unionService = mock(UnionService.class);
        User creator = TestFixtures.user(1L, "79990001111");
        when(unionService.isMember(10L, creator)).thenReturn(false);

        EventServiceImpl service = new EventServiceImpl(eventRepository, unionRepository, unionService);
        EventCreateDto createDto = new EventCreateDto();
        createDto.setTitle("Meeting");
        createDto.setDescription("d");
        createDto.setLocation("l");
        assertThrows(AccessNotAllowedException.class, () -> service.createEvent(10L, createDto, creator));

        when(unionService.isMember(10L, creator)).thenReturn(true);
        when(unionRepository.findById(10L)).thenReturn(Optional.of(TestFixtures.union(10L, "Union", creator, creator)));
        Event event = TestFixtures.event(4L, "x", "y", LocalDateTime.now().plusDays(1), "z", TestFixtures.union(10L, "Union", creator, creator), creator, LocalDateTime.now());
        when(eventRepository.findById(4L)).thenReturn(Optional.of(event));
        assertThrows(SecurityException.class, () -> service.deleteEvent(4L, TestFixtures.user(2L, "79990001122")));
        when(eventRepository.findById(6L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.deleteEvent(6L, creator));
    }
}
