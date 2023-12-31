package ru.practicum.service.event;

import ru.practicum.dto.event.*;
import ru.practicum.dto.party_request.EventRequestStatusUpdateRequest;
import ru.practicum.dto.party_request.EventRequestStatusUpdateResult;
import ru.practicum.dto.party_request.PartyRequestDto;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

public interface EventService {
    EventFullDto addNew(NewEventDto newEventDto, long initiatorId);

    List<EventShortDto> getAllByUserId(long userId, int from, int size);

    EventFullDto getFullDtoByIdAndOwnerId(long userId, long eventId);

    List<EventFullDto> getAllAdminFiltered(List<Long> userIds, List<String> strStates, List<Long> catIds,
                                           LocalDateTime rangeStart, LocalDateTime rangeEnd, int from, int size);

    EventFullDto adminUpdate(long eventId, UpdateEventDto updateDto);

    List<EventShortDto> getAllPublicFiltered(String text, List<Long> catIds, Boolean paid,
                                             LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                             Boolean onlyAvailable, EventSort sort,
                                             int from, int size, HttpServletRequest request);

    EventFullDto getPublicById(long id, HttpServletRequest request);

    EventFullDto userUpdate(long userId, long eventId, UpdateEventDto updateDto);

    List<PartyRequestDto> getRequestsByEventIdAndEventOwner(long eventId, long eventOwnerId);

    EventRequestStatusUpdateResult updateRequestStatuses(
            long eventId, long userId, EventRequestStatusUpdateRequest updateDto);
}
