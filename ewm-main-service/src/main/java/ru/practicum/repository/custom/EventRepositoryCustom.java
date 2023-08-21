package ru.practicum.repository.custom;

import ru.practicum.model.Event;

import java.time.LocalDateTime;
import java.util.List;

public interface EventRepositoryCustom {
    List<Event> getAllAdminFiltered(List<Long> userIds, List<String> strStates, List<Long> catIds,
                                    LocalDateTime rangeStart, LocalDateTime rangeEnd, int from, int size);

    List<Event> getAllPublicFiltered(String text, List<Long> catIds, Boolean paid,
                                     LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                     Boolean onlyAvailable, int from, int size);
}
