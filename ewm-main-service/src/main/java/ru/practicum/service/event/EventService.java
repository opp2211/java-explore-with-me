package ru.practicum.service.event;

import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.dto.event.NewEventDto;
import ru.practicum.model.Event;

import java.util.Collection;
import java.util.List;

public interface EventService {
    EventFullDto addNew(NewEventDto newEventDto, long initiatorId);
    List<EventShortDto> getAllByUserId(long userId, int from, int size);
    EventFullDto getByIdAndUserId(long userId, long eventId);
    Event getById(long id);
    List<Event> getAllByIds(Collection<Long> ids);
}
