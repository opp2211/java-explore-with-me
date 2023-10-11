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
public class EventControllerPrivate {
    private final EventService eventService;

    @PostMapping
    public EventFullDto addNew(@PathVariable @Positive long userId,
                               @RequestBody @Valid NewEventDto newEventDto) {
        return eventService.addNew(newEventDto, userId);
    }

    @GetMapping
    public List<EventShortDto> getAllByOwnerId(@PathVariable @Positive long userId,
                                              @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                              @RequestParam(defaultValue = "10") @Positive int size) {
        return eventService.getAllByUserId(userId, from, size);
    }

    @GetMapping("/{eventId}")
    public EventFullDto getByIdAndOwnerId(@PathVariable @Positive long userId,
                                          @PathVariable @Positive long eventId) {
        return eventService.getFullDtoByIdAndOwnerId(userId, eventId);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto update(@PathVariable @Positive long userId,
                               @PathVariable @Positive long eventId,
                               @RequestBody @Valid UpdateEventDto updateDto) {
        return eventService.userUpdate(userId, eventId, updateDto);
    }

    @GetMapping("/{eventId}/requests")
    public List<PartyRequestDto> getPartyRequestsToEvent(@PathVariable @Positive long userId,
                                                         @PathVariable @Positive long eventId) {
        return eventService.getRequestsByEventIdAndEventOwner(eventId, userId);
    }

    @PatchMapping("/{eventId}/requests")
    public EventRequestStatusUpdateResult updateRequestStatuses(@PathVariable @Positive long userId,
                                                                @PathVariable @Positive long eventId,
                                                                @RequestBody EventRequestStatusUpdateRequest updateDto) {
        return eventService.updateRequestStatuses(eventId, userId, updateDto);
    }

}
