package ru.practicum.service.event;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStats;
import ru.practicum.dto.event.*;
import ru.practicum.dto.party_request.*;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.ForbiddenException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.model.Event;
import ru.practicum.model.EventState;
import ru.practicum.model.PartyRequest;
import ru.practicum.model.PartyRequestStatus;
import ru.practicum.repository.CategoryRepository;
import ru.practicum.repository.EventRepository;
import ru.practicum.repository.PartyRequestRepository;
import ru.practicum.repository.UserRepository;
import ru.practicum.statsclient.StatsClient;
import ru.practicum.validator.StaticValidator;

import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
    private final EventMapper eventMapper;
    private final EventRepository eventRepository;
    private final StatsClient statsClient;
    private final PartyRequestRepository partyRequestRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final PartyRequestMapper partyRequestMapper;

    @Override
    public EventFullDto addNew(NewEventDto newEventDto, long initiatorId) {
        if (LocalDateTime.now().plusHours(2).isAfter(newEventDto.getEventDate())) {
            throw new ValidationException(
                    String.format("Field: eventDate. Error: должно содержать дату, которая еще не наступила. " +
                            "Value: %s", newEventDto.getEventDate().toString()));
        }
        Event newEvent = eventMapper.toEvent(
                newEventDto,
                categoryRepository.findById(newEventDto.getCategory()).orElseThrow(() ->
                        new NotFoundException(String.format("Category ID = %d not found!", newEventDto.getCategory()))),
                userRepository.findById(initiatorId).orElseThrow(() ->
                        new EntityNotFoundException(String.format("User ID = %d not found!", initiatorId))));

        Event savedEvent = eventRepository.save(newEvent);
        return eventMapper.toEventFullDto(savedEvent, 0L, 0L);
    }

    @Override
    public List<EventShortDto> getAllByUserId(long userId, int from, int size) {
        StaticValidator.validateFromSize(from, size);
        List<Event> events = eventRepository.findAllByInitiatorId(userId, PageRequest.of(from/size, size));
        List<Long> eventIds = events.stream().map(Event::getId).collect(Collectors.toList());
        return eventMapper.toEventShortDtoList(events, getEventsViewsMap(eventIds), getEventsConfReqsMap(eventIds));
    }

    @Override
    public EventFullDto getFullDtoByIdAndOwnerId(long userId, long eventId) {
        return eventMapper.toEventFullDto(
                getByIdAndOwnerId(eventId, userId),
                getEventViews(eventId),
                getEventConfReqs(eventId));
    }

    @Override
    public List<EventFullDto> getAllAdminFiltered(List<Long> userIds, List<String> strStates, List<Long> catIds,
                                                  LocalDateTime rangeStart, LocalDateTime rangeEnd, int from, int size) {
        StaticValidator.validateFromSize(from, size);
        List<EventState> states = strStates == null ?
                null : strStates.stream().map(EventState::valueOf).collect(Collectors.toList());
        List<Event> events =
                eventRepository.getAllAdminFiltered(userIds, states, catIds, rangeStart, rangeEnd, from, size);
        List<Long> eventIds = events.stream().map(Event::getId).collect(Collectors.toList());
        return eventMapper.toEventFullDtoList(events, getEventsViewsMap(eventIds), getEventsConfReqsMap(eventIds));
    }

    @Override
    public EventFullDto adminUpdate(long eventId, UpdateEventDto updateDto) {
        if (updateDto.getEventDate() != null && LocalDateTime.now().plusHours(1).isAfter(updateDto.getEventDate())) {
            throw new ValidationException(
                    String.format("Field: eventDate. Error: должно содержать дату, которая еще не наступила. " +
                            "Value: %s", updateDto.getEventDate().toString()));
        }
        Event event = getById(eventId);
        if (updateDto.getStateAction() != null) {
            if (event.getState() != EventState.PENDING) {
                throw new ConflictException(
                        String.format("Cannot publish the event because it's not in the right state: %s",
                                event.getState()));
            }
            switch (updateDto.getStateAction()){
                case PUBLISH_EVENT:
                    event.setState(EventState.PUBLISHED);
                    event.setPublishedOn(LocalDateTime.now());
                    break;
                case REJECT_EVENT:
                    event.setState(EventState.CANCELED);
                    break;
            }
        }
        applyPatchChanges(event, updateDto);
        eventRepository.save(event);
        return eventMapper.toEventFullDto(event, getEventViews(eventId), getEventConfReqs(eventId));
    }

    @Override
    public List<EventShortDto> getAllPublicFiltered(String text, List<Long> catIds, Boolean paid,
                                                    LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                                    Boolean onlyAvailable, EventSort sort, int from, int size,
                                                    HttpServletRequest request) {
        StaticValidator.validateFromSize(from, size);
        if (rangeStart != null && rangeEnd != null && rangeEnd.isBefore(rangeStart)) {
            throw new ValidationException("End datetime cannot be before Start");
        }
        if (rangeStart == null && rangeEnd == null) {
            rangeStart = LocalDateTime.now();
        }
        List<Event> events = eventRepository.getAllPublicFiltered(
                text, catIds, paid, rangeStart, rangeEnd, from, size);

        List<Long> eventIds = events.stream().map(Event::getId).collect(Collectors.toList());
        Map<Long, Long> eventsViewsMap = getEventsViewsMap(eventIds);

        if (onlyAvailable) {
            events = events.stream()
                    .filter(event ->
                            event.getParticipantLimit() == 0 ||
                                    event.getParticipantLimit() - eventsViewsMap.get(event.getId()) > 0)
                    .collect(Collectors.toList());
        }

        List<EventShortDto> shortEvents = eventMapper.toEventShortDtoList(
                events, eventsViewsMap, getEventsConfReqsMap(eventIds));

        if (sort == EventSort.VIEWS) {
            shortEvents.sort(Comparator.comparing(EventShortDto::getViews));
        }

        statsClient.saveStatHit(new EndpointHitDto("ewm-main",
                request.getRequestURI(),
                request.getRemoteAddr(),
                LocalDateTime.now()));

        return shortEvents;
    }

    @Override
    public EventFullDto getPublicById(long id, HttpServletRequest request) {
        Event event = eventRepository.findByIdAndState(id, EventState.PUBLISHED).orElseThrow(() ->
                new NotFoundException(String.format("Event ID = %d not found or unavailable!", id)));

        statsClient.saveStatHit(new EndpointHitDto("ewm-main",
                request.getRequestURI(),
                request.getRemoteAddr(),
                LocalDateTime.now()));

        return eventMapper.toEventFullDto(event, getEventViews(id), getEventConfReqs(id));
    }

    @Override
    public EventFullDto userUpdate(long userId, long eventId, UpdateEventDto updateDto) {
        if (updateDto.getEventDate() != null && LocalDateTime.now().plusHours(2).isAfter(updateDto.getEventDate())) {
            throw new ValidationException(
                    String.format("Field: eventDate. Error: должно содержать дату, которая еще не наступила. " +
                            "Value: %s", updateDto.getEventDate().toString()));
        }
        userRepository.findById(userId).orElseThrow(() ->
                new EntityNotFoundException(String.format("User ID = %d not found!", userId)));
        Event event = getById(eventId);
        if (event.getState() == EventState.PUBLISHED) {
            throw new ConflictException("Only pending or canceled events can be changed");
        }
        if (userId != event.getInitiator().getId()) {
            throw new ForbiddenException("Only event owner can update event");
        }
        if (updateDto.getStateAction() != null) {
            switch (updateDto.getStateAction()) {
                case SEND_TO_REVIEW:
                    event.setState(EventState.PENDING);
                    break;
                case CANCEL_REVIEW:
                    event.setState(EventState.CANCELED);
                    break;
                default:
                    throw new ValidationException("Unsupported action state");
            }
        }
        applyPatchChanges(event, updateDto);
        eventRepository.save(event);
        return eventMapper.toEventFullDto(event, getEventViews(eventId), getEventConfReqs(eventId));
    }

    @Override
    public List<PartyRequestDto> getRequestsByEventIdAndEventOwner(long eventId, long eventOwnerId) {
        getByIdAndOwnerId(eventId, eventOwnerId);
        return partyRequestRepository.findAllByEventId(eventId).stream()
                .map(partyRequestMapper::toPartyRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    public EventRequestStatusUpdateResult updateRequestStatuses(long eventId, long userId,
                                                                EventRequestStatusUpdateRequest updateDto) {
        Event event = getByIdAndOwnerId(eventId, userId);
        long requestsLimit = event.getParticipantLimit();
        long numberConfRequests = partyRequestRepository.countByEventIdAndStatus(eventId, PartyRequestStatus.CONFIRMED);
        if (updateDto.getStatus() == PartyRequestStatus.CONFIRMED &&
                requestsLimit != 0 && requestsLimit <= numberConfRequests) {
            throw new ConflictException("The participant limit has been reached");
        }

        EventRequestStatusUpdateResult result = new EventRequestStatusUpdateResult();
        List<PartyRequest> requests = partyRequestRepository.findAllByIdIn(updateDto.getRequestIds());
        if (requests.size() != updateDto.getRequestIds().size()) {
            throw new NotFoundException("One or more requests from given IDs not found!");
        }
        for (PartyRequest partyRequest : requests) {
            if (partyRequest.getStatus() != PartyRequestStatus.PENDING) {
                throw new ConflictException("Request must have status PENDING");
            }

            if (updateDto.getStatus() == PartyRequestStatus.CONFIRMED &&
                    (requestsLimit == 0 || requestsLimit - numberConfRequests > 0)) {
                partyRequest.setStatus(PartyRequestStatus.CONFIRMED);
                result.getConfirmedRequests().add(partyRequestMapper.toPartyRequestDto(partyRequest));
                numberConfRequests++;
            } else {
                partyRequest.setStatus(PartyRequestStatus.REJECTED);
                result.getRejectedRequests().add(partyRequestMapper.toPartyRequestDto(partyRequest));
            }
        }
        partyRequestRepository.saveAll(requests);
        return result;
    }

    private Event getById(long id) {
        return eventRepository.findById(id).orElseThrow(() ->
                new NotFoundException(String.format("Event ID = %d not found!", id)));
    }

    private Map<Long, Long> getEventsViewsMap(Collection<Long> eventIds) {
        Set<String> urisForStatsMap = eventIds.stream()
                .map(id -> "/events/" + id)
                .collect(Collectors.toSet());
        List<ViewStats> stats = statsClient.getStats(LocalDateTime.ofInstant(Instant.EPOCH, ZoneId.systemDefault()),
                LocalDateTime.now(), true, urisForStatsMap);
        return stats
                .stream()
                .collect(Collectors.toMap(
                        viewStats -> Long.parseLong(viewStats.getUri().substring("/events/".length())),
                        ViewStats::getHits));
    }
    private Long getEventViews(Long eventId) {
        Map<Long, Long> viewsMap = getEventsViewsMap(List.of(eventId));
        return viewsMap.getOrDefault(eventId, 0L);
    }

    private Map<Long, Long> getEventsConfReqsMap(Collection<Long> eventIds) {
        return partyRequestRepository
                .getAllParticipantsNumberByStatusAndEventIdIn(PartyRequestStatus.CONFIRMED, eventIds)
                .stream()
                .collect(Collectors.toMap(
                        ParticipantsNumber::getEventId,
                        ParticipantsNumber::getNumber));
    }
    private Long getEventConfReqs(Long eventId) {
        return partyRequestRepository.countByEventIdAndStatus(eventId, PartyRequestStatus.CONFIRMED);
    }

    private Event getByIdAndOwnerId(long eventId, long ownerId) {
        return eventRepository.findByIdAndInitiatorId(eventId, ownerId).orElseThrow(() ->
                new NotFoundException(
                        String.format("Event ID=%d belongs to user ID=%d was not found", eventId, ownerId)));
    }

    private void applyPatchChanges(Event event, UpdateEventDto updateDto) {
        String newTitle = updateDto.getTitle();
        if (newTitle != null) {
            event.setTitle(newTitle);
        }
        String newAnnotation = updateDto.getAnnotation();
        if (newAnnotation != null) {
            event.setAnnotation(newAnnotation);
        }
        String newDescription = updateDto.getDescription();
        if (newDescription != null) {
            event.setDescription(newDescription);
        }
        LocalDateTime newEventDate = updateDto.getEventDate();
        if (newEventDate != null) {
            event.setEventDate(newEventDate);
        }
        Long newCategoryId = updateDto.getCategory();
        if (newCategoryId != null) {
            event.setCategory(categoryRepository.findById(newCategoryId).orElseThrow(() ->
                    new NotFoundException(String.format("Category ID = %d not found!", newCategoryId))));
        }
        Boolean newPaid = updateDto.getPaid();
        if (newPaid != null) {
            event.setPaid(newPaid);
        }
        Long newPartyLimit = updateDto.getParticipantLimit();
        if (newPartyLimit != null) {
            event.setParticipantLimit(newPartyLimit);
        }
        Boolean newRequestModeration = updateDto.getRequestModeration();
        if (newRequestModeration != null) {
            event.setRequestModeration(newRequestModeration);
        }
        Location newLocation = updateDto.getLocation();
        if (newLocation != null) {
            if (newLocation.getLat() != null) {
                event.setLocationLat(updateDto.getLocation().getLat());
            }
            if (newLocation.getLon() != null) {
                event.setLocationLon(updateDto.getLocation().getLon());
            }
        }
    }
}
