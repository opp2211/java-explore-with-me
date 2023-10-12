package ru.practicum.service.party_request;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.dto.party_request.PartyRequestDto;
import ru.practicum.dto.party_request.PartyRequestMapper;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.model.*;
import ru.practicum.repository.EventRepository;
import ru.practicum.repository.PartyRequestRepository;
import ru.practicum.repository.UserRepository;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PartyRequestServiceImpl implements PartyRequestService {
    private final PartyRequestMapper partyRequestMapper;
    private final PartyRequestRepository partyRequestRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    @Override
    public PartyRequestDto addNew(long userId, long eventId) {
        User user = getUserById(userId);
        Event event = getEventById(eventId);
        if (event.getInitiator().getId() == userId) {
            throw new ConflictException("Can't participate own event!");
        }
        if (event.getState() != EventState.PUBLISHED) {
            throw new ConflictException("Can't participate unpublished event!");
        }
        if (event.getParticipantLimit() != 0 &&
                getNumberConfirmedRequestsByEventId(eventId) >= event.getParticipantLimit()) {
            throw new ConflictException("Event participant limit has been reached");
        }

        PartyRequest partyRequest = PartyRequest.builder()
                .event(event)
                .requester(user)
                .created(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS))
                .status(!event.getRequestModeration() || event.getParticipantLimit() == 0 ?
                        PartyRequestStatus.CONFIRMED : PartyRequestStatus.PENDING)
                .build();
        PartyRequest savedRequest = partyRequestRepository.save(partyRequest);
        return partyRequestMapper.toPartyRequestDto(savedRequest);
    }

    @Override
    public List<PartyRequestDto> getAllByUserId(long userId) {
        getUserById(userId);
        return partyRequestRepository.findAllByRequesterId(userId).stream()
                .map(partyRequestMapper::toPartyRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    public PartyRequestDto cancelOwn(long userId, long requestId) {
        PartyRequest partyRequest = partyRequestRepository.findByIdAndRequesterId(requestId, userId).orElseThrow(() ->
                new EntityNotFoundException(String.format("ParticipationRequest ID = %d not found!", requestId)));
        partyRequest.setStatus(PartyRequestStatus.CANCELED);
        partyRequestRepository.save(partyRequest);
        return partyRequestMapper.toPartyRequestDto(partyRequest);
    }

    private Long getNumberConfirmedRequestsByEventId(Long eventId) {
        return partyRequestRepository.countByEventIdAndStatus(eventId, PartyRequestStatus.CONFIRMED);
    }

    private User getUserById(Long userId) {
        return userRepository.findById(userId).orElseThrow(() ->
                new EntityNotFoundException(String.format("User ID = %d not found!", userId)));
    }

    private Event getEventById(Long eventId) {
        return eventRepository.findById(eventId).orElseThrow(() ->
                new NotFoundException(String.format("Event ID = %d not found!", eventId)));
    }
}
