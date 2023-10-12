package ru.practicum.repository.custom;

import ru.practicum.model.Event;
import ru.practicum.model.EventState;

import java.time.LocalDateTime;
import java.util.List;

public interface EventRepositoryCustom {
    List<Event> getAllAdminFiltered(List<Long> userIds, List<EventState> states, List<Long> catIds,
                                    LocalDateTime rangeStart, LocalDateTime rangeEnd, int from, int size);

    List<Event> getAllPublicFiltered(String text, List<Long> catIds, Boolean paid,
                                     LocalDateTime rangeStart, LocalDateTime rangeEnd, int from, int size);
}
