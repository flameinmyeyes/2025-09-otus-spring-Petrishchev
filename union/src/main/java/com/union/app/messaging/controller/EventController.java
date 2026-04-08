package com.union.app.messaging.controller;

import com.union.app.messaging.dto.EventCreateDto;
import com.union.app.messaging.dto.EventDto;
import com.union.app.messaging.dto.EventUpdateDto;
import com.union.app.messaging.model.Event;
import com.union.app.messaging.service.EventService;
import com.union.app.user.model.User;
import com.union.app.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PutMapping;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
@Tag(name = "Events", description = "API для управления событиями")
@SecurityRequirement(name = "BearerAuth")
public class EventController {
    private final EventService eventService;

    private final UserService userService;

    @PostMapping("/createEvent/{unionId}")
    @Operation(summary = "Создать событие")
    public EventDto createEvent(@PathVariable Long unionId,
                                @Valid @RequestBody EventCreateDto dto) {
        User currentUser = userService.getCurrentUser();
        Event event = eventService.createEvent(unionId, dto, currentUser);
        return EventDto.fromEntity(event);
    }

    @PutMapping("/updateEvent/{eventId}")
    @Operation(summary = "Обновить событие")
    public EventDto updateEvent(@PathVariable Long eventId,
                                @RequestBody EventUpdateDto dto) {
        User currentUser = userService.getCurrentUser();
        Event event = eventService.updateEvent(eventId, dto, currentUser);
        return EventDto.fromEntity(event);
    }

    @DeleteMapping("/deleteEvent/{eventId}")
    @Operation(summary = "Удалить событие")
    public ResponseEntity<?> deleteEvent(@PathVariable Long eventId) {
        User currentUser = userService.getCurrentUser();
        eventService.deleteEvent(eventId, currentUser);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/listEvents/{unionId}")
    @Operation(summary = "Получить все события объединения")
    public List<EventDto> getEvents(@PathVariable Long unionId) {
        List<Event> events = eventService.getEventsForUnion(unionId);
        return events.stream()
                .map(EventDto::fromEntity)
                .collect(Collectors.toList());
    }
}
