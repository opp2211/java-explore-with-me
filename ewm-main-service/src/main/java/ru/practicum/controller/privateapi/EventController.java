package ru.practicum.controller.privateapi;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.dto.event.NewEventDto;
import ru.practicum.dto.event.UpdateEventDto;
import ru.practicum.dto.party_request.EventRequestStatusUpdateRequest;
import ru.practicum.dto.party_request.EventRequestStatusUpdateResult;
import ru.practicum.dto.party_request.PartyRequestDto;
import ru.practicum.service.event.EventService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@RequestMapping("/users/{userId}/events")
@RequiredArgsConstructor
@Validated
public class EventController {
    private final EventService eventService;

    @PostMapping
    public EventFullDto addNew(@Positive @PathVariable long userId,
                               @Valid @RequestBody NewEventDto newEventDto) {
        return eventService.addNew(newEventDto, userId);
    }

    @GetMapping
    public List<EventShortDto> getAllByUserId(@Positive @PathVariable long userId,
                                       @PositiveOrZero @RequestParam(defaultValue = "0") int from,
                                       @Positive @RequestParam(defaultValue = "10") int size) {
        return eventService.getAllByUserId(userId, from, size);
    }

    @GetMapping("/{eventId}")
    public EventFullDto getByIdAndUserId(@Positive @PathVariable long userId,
                                         @Positive @PathVariable long eventId) {
        return eventService.getByIdAndUserId(userId, eventId);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto update(@Positive @PathVariable long userId,
                               @Positive @PathVariable long eventId,
                               @Valid @RequestBody UpdateEventDto updateDto) {
        return eventService.userUpdate(userId, eventId, updateDto);
    }

    @GetMapping("/{eventId}/requests")
    public List<PartyRequestDto> getPartyRequestsToEvent(@PathVariable @Positive long userId,
                                                        @PathVariable @Positive long eventId) {
        return eventService.getAllEventRequests(eventId, userId);
    }

    @PatchMapping("/{eventId}/requests")
    public EventRequestStatusUpdateResult updateRequestStatuses(@Positive @PathVariable long userId,
                                                                @Positive @PathVariable long eventId,
                                                                @RequestBody EventRequestStatusUpdateRequest updateDto) {
        return eventService.updateRequestStatuses(eventId, userId, updateDto);
    }

}
