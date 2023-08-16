package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.dto.party_request.ParticipantsNumber;
import ru.practicum.model.PartyRequest;

import java.util.List;
import java.util.Optional;

public interface PartyRequestRepository extends JpaRepository<PartyRequest, Long> {
    boolean existsByEventIdAndRequesterId(long eventId, long requesterId);

    List<PartyRequest> findAllByRequesterId(long userId);

    Optional<PartyRequest> findByIdAndRequesterId(long id, long requesterId);

    @Query("SELECT new ru.practicum.dto.party_request.ParticipantsNumber(pr.event.id, COUNT(*)) " +
            "FROM PartyRequest pr " +
            "WHERE pr.event.id == :eventId " +
            "GROUP BY pr.event.id ")
    ParticipantsNumber getParticipantsNumberByEventId(@Param("eventId") long eventId);
}
