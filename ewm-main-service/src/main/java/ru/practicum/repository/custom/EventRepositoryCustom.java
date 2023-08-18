package ru.practicum.repository.custom;

import ru.practicum.model.Event;

import java.time.LocalDateTime;
import java.util.List;

public interface EventRepositoryCustom {
    List<Event> getAllFiltered(List<Long> userIds, List<String> strStates, List<Long> catIds,
                               LocalDateTime rangeStart, LocalDateTime rangeEnd, int from, int size);
}
