package ru.practicum.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.model.Event;
import ru.practicum.model.EventState;
import ru.practicum.repository.custom.EventRepositoryCustom;

import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long>, EventRepositoryCustom {
    List<Event> findAllByInitiatorId(long initiatorId, Pageable pageable);
    Event findByIdAndInitiatorId(long eventId, long userId);
    Optional<Event> findByIdAndState(long id, EventState state);
}
