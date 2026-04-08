package com.union.app.messaging.service.impl;

import com.union.app.exception.UnionNotFoundException;
import com.union.app.exception.event.AccessNotAllowedException;
import com.union.app.messaging.dto.EventCreateDto;
import com.union.app.messaging.dto.EventUpdateDto;
import com.union.app.messaging.model.Event;
import com.union.app.messaging.model.Union;
import com.union.app.messaging.repository.EventRepository;
import com.union.app.messaging.repository.UnionRepository;
import com.union.app.messaging.service.EventService;
import com.union.app.messaging.service.UnionService;
import com.union.app.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;

    private final UnionRepository unionRepository;

    private final UnionService unionService;

    @Override
    @Transactional
    public Event createEvent(Long unionId, EventCreateDto dto, User creator) {
        if (!unionService.isMember(unionId, creator)) {
            throw new AccessNotAllowedException("Access to the community is not allowed");
        }
        Union union = unionRepository.findById(unionId)
                .orElseThrow(() -> new UnionNotFoundException("Union not found"));
        Event event = new Event();
        event.setTitle(dto.getTitle().trim());
        event.setDescription(dto.getDescription());
        event.setLocation(dto.getLocation());
        event.setUnion(union);
        event.setCreatedBy(creator);
        return eventRepository.save(event);
    }

    @Override
    @Transactional
    public Event updateEvent(Long eventId, EventUpdateDto dto, User currentUser) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        if (!event.getCreatedBy().getId().equals(currentUser.getId())) {
            throw new SecurityException("Only the creator can edit this event");
        }

        if (dto.getTitle() != null && !dto.getTitle().trim().isEmpty()) {
            event.setTitle(dto.getTitle().trim());
        }
        if (dto.getDescription() != null) {
            event.setDescription(dto.getDescription());
        }
        if (dto.getLocation() != null) {
            event.setLocation(dto.getLocation());
        }

        return eventRepository.save(event);
    }

    @Override
    @Transactional
    public void deleteEvent(Long eventId, User currentUser) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        if (!event.getCreatedBy().getId().equals(currentUser.getId())) {
            throw new SecurityException("Only the creator can delete this event");
        }

        eventRepository.delete(event);
    }

    @Override
    public List<Event> getEventsForUnion(Long unionId) {
        Union union = unionRepository.findById(unionId)
                .orElseThrow(() -> new RuntimeException("Union not found"));
        return eventRepository.findByUnion(union);
    }
}
