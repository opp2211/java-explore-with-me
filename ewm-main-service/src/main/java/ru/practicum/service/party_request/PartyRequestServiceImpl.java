package ru.practicum.service.party_request;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.dto.party_request.ParticipantsNumber;
import ru.practicum.dto.party_request.PartyRequestDto;
import ru.practicum.dto.party_request.PartyRequestMapper;
import ru.practicum.model.*;
import ru.practicum.repository.PartyRequestRepository;
import ru.practicum.service.event.EventService;
import ru.practicum.service.user.UserService;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PartyRequestServiceImpl implements PartyRequestService {
    private final PartyRequestMapper partyRequestMapper;
    private final PartyRequestRepository partyRequestRepository;
    private final UserService userService;
    private final EventService eventService;

    @Override
    public PartyRequestDto addNew(long userId, long eventId) {
        boolean hasDuplicate = partyRequestRepository.existsByEventIdAndRequesterId(eventId, userId);
        if (hasDuplicate) {
            throw new RuntimeException(); //todo: exceptions
        }
        User user = userService.getById(userId);
        Event event = eventService.getById(eventId);
        if (event.getInitiator().getId() == userId) {
            throw new RuntimeException();
        }
        if (event.getState() != EventState.PUBLISHED) {
            throw new RuntimeException();
        }
        if (event.getParticipantLimit() != 0 ||
                getParticipantsNumberByEventId(eventId).getNumber() >= event.getParticipantLimit()) {
            throw new RuntimeException();
        }

        PartyRequest partyRequest = PartyRequest.builder()
                .event(event)
                .requester(user)
                .created(LocalDateTime.now())
                .status(event.getRequestModeration() ? PartyRequestStatus.PENDING : PartyRequestStatus.CONFIRMED)
                .build();
        PartyRequest savedRequest = partyRequestRepository.save(partyRequest);
        return partyRequestMapper.toPartyRequestDto(savedRequest);
    }

    @Override
    public List<PartyRequestDto> getAllByUserId(long userId) {
        return partyRequestRepository.findAllByRequesterId(userId).stream()
                .map(partyRequestMapper::toPartyRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    public void cancelOwn(long userId, long requestId) {
        PartyRequest partyRequest = partyRequestRepository.findByIdAndRequesterId(requestId, userId).orElseThrow(() ->
                new EntityNotFoundException(String.format("ParticipationRequest ID = %d not found!", requestId)));
        partyRequest.setStatus(PartyRequestStatus.REJECTED); //todo: canceled?
        partyRequestRepository.save(partyRequest);
    }

    private ParticipantsNumber getParticipantsNumberByEventId(long eventId) {
        return partyRequestRepository.getParticipantsNumberByEventId(eventId);
    }
}
