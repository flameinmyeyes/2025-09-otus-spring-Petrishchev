package com.union.app.messaging.service;

import com.union.app.user.model.User;
import com.union.app.messaging.dto.EventCreateDto;
import com.union.app.messaging.dto.EventUpdateDto;
import com.union.app.messaging.model.Event;
import java.util.List;

public interface EventService {
    Event createEvent(Long unionId, EventCreateDto dto, User creator);

    Event updateEvent(Long eventId, EventUpdateDto dto, User currentUser);

    void deleteEvent(Long eventId, User currentUser);

    List<Event> getEventsForUnion(Long unionId);
}
