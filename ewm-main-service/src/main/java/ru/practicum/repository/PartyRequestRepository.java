package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.dto.party_request.ParticipantsNumber;
import ru.practicum.model.PartyRequest;
import ru.practicum.model.PartyRequestStatus;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PartyRequestRepository extends JpaRepository<PartyRequest, Long> {
    List<PartyRequest> findAllByRequesterId(long userId);
    List<PartyRequest> findAllByEventId(long eventId);

    Optional<PartyRequest> findByIdAndRequesterId(long id, long requesterId);

    @Query("SELECT new ru.practicum.dto.party_request.ParticipantsNumber(pr.event.id, COUNT(*)) " +
            "FROM PartyRequest pr " +
            "WHERE pr.status = :status " +
            "AND pr.event.id IN :eventIds " +
            "GROUP BY pr.event.id ")
    List<ParticipantsNumber> getAllParticipantsNumberByStatusAndEventIdIn(@Param("status") PartyRequestStatus status,
                                                                          @Param("eventIds") Collection<Long> eventIds);

    List<PartyRequest> findAllByIdIn(List<Long> ids);

    long countByEventIdAndStatus(long eventId, PartyRequestStatus status);
}
