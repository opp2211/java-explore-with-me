package ru.practicum.service.party_request;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.dto.party_request.ParticipantsNumber;
import ru.practicum.dto.party_request.PartyRequestDto;
import ru.practicum.dto.party_request.PartyRequestMapper;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
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

    @Override
    public List<PartyRequestDto> getAllByEvent(long eventId) {
        return partyRequestRepository.findAllByEventId(eventId).stream()
                .map(partyRequestMapper::toPartyRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<PartyRequestDto> confirmRequests(List<Long> ids, long confirmLimit) {
        List<PartyRequest> requests = partyRequestRepository.findAllByIdIn(ids);
        if (requests.size() != ids.size()) {
            throw new NotFoundException("One or more requests from given IDs not found!");
        }
        for (PartyRequest partyRequest : requests) {
            if (partyRequest.getStatus() != PartyRequestStatus.PENDING) {
                throw new ConflictException("Request must have status PENDING");
            }
            if (confirmLimit >= 0) {
                partyRequest.setStatus(PartyRequestStatus.CONFIRMED);
                confirmLimit--;
            } else {
                partyRequest.setStatus(PartyRequestStatus.REJECTED);
            }
        }
        return partyRequestRepository.saveAll(requests).stream()
                .map(partyRequestMapper::toPartyRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<PartyRequestDto> rejectRequests(List<Long> ids) {
        List<PartyRequest> requests = partyRequestRepository.findAllByIdIn(ids);
        if (requests.size() != ids.size()) {
            throw new NotFoundException("One or more requests from given IDs not found!");
        }
        for (PartyRequest partyRequest : requests) {
            if (partyRequest.getStatus() != PartyRequestStatus.PENDING) {
                throw new ConflictException("Request must have status PENDING");
            }
            partyRequest.setStatus(PartyRequestStatus.REJECTED);
        }
        return partyRequestRepository.saveAll(requests).stream()
                .map(partyRequestMapper::toPartyRequestDto)
                .collect(Collectors.toList());
    }

    private ParticipantsNumber getParticipantsNumberByEventId(long eventId) {
        return partyRequestRepository.getParticipantsNumberByEventId(eventId);
    }
}
