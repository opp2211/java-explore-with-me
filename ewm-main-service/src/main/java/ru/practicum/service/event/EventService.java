package ru.practicum.service.event;

import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.dto.event.NewEventDto;
import ru.practicum.dto.event.UpdateEventAdminRequest;
import ru.practicum.model.Event;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface EventService {
    EventFullDto addNew(NewEventDto newEventDto, long initiatorId);
    List<EventShortDto> getAllByUserId(long userId, int from, int size);
    EventFullDto getByIdAndUserId(long userId, long eventId);
    Event getById(long id);
    List<Event> getAllByIds(Collection<Long> ids);
    List<EventFullDto> getAllFiltered(List<Long> userIds, List<String> strStates, List<Long> catIds,
                                      LocalDateTime rangeStart, LocalDateTime rangeEnd, int from, int size);
    EventFullDto adminUpdate(long eventId, UpdateEventAdminRequest updateDto);
}
