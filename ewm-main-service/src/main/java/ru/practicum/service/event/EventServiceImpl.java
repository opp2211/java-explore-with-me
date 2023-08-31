package ru.practicum.service.event;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.dto.ViewStats;
import ru.practicum.dto.event.*;
import ru.practicum.dto.party_request.EventRequestStatusUpdateRequest;
import ru.practicum.dto.party_request.EventRequestStatusUpdateResult;
import ru.practicum.dto.party_request.PartyRequestDto;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.model.Event;
import ru.practicum.model.EventState;
import ru.practicum.model.PartyRequestStatus;
import ru.practicum.repository.EventRepository;
import ru.practicum.service.category.CategoryService;
import ru.practicum.service.party_request.PartyRequestService;
import ru.practicum.service.user.UserService;
import ru.practicum.statsclient.StatsClient;
import ru.practicum.validator.StaticValidator;

import javax.persistence.EntityNotFoundException;
import java.security.InvalidParameterException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
    private final EventMapper eventMapper;
    private final EventRepository eventRepository;
    private final CategoryService categoryService;
    private final UserService userService;
    private final PartyRequestService partyRequestService;
    private final StatsClient statsClient;

    @Override
    public EventFullDto addNew(NewEventDto newEventDto, long initiatorId) {
        Event newEvent = eventMapper.toEvent(newEventDto);
        newEvent.setCategory(categoryService.getById(newEventDto.getCategory()));
        newEvent.setInitiator(userService.getById(initiatorId));
        newEvent.setCreatedOn(LocalDateTime.now());
        newEvent.setState(EventState.PENDING);

        Event savedEvent = eventRepository.save(newEvent);
        return eventMapper.toEventFullDto(savedEvent);
    }

    @Override
    public List<EventShortDto> getAllByUserId(long userId, int from, int size) {
        StaticValidator.validateFromSize(from, size);
        List<Event> events = eventRepository.findAllByInitiatorId(userId, PageRequest.of(from/size, size));

        return getFilledShortDtos(events);
    }

    @Override
    public EventFullDto getFullDtoByIdAndOwnerId(long userId, long eventId) {
        EventFullDto eventFullDto = eventMapper.toEventFullDto(getByIdAndOwnerId(eventId, userId));
        eventFullDto.setViews(getMapIdViews(List.of(eventFullDto.getId())).get(eventFullDto.getId()));
        eventFullDto.setConfirmedRequests(partyRequestService.getNumberConfirmedRequestsByEventId(eventId));
        return eventFullDto;
    }

    @Override
    public Event getById(long id) {
        return eventRepository.findById(id).orElseThrow(() ->
                new NotFoundException(String.format("Event ID = %d not found!", id)));
    }

    @Override
    public List<Event> getAllByIds(Collection<Long> ids) {
        Set<Long> setIds = new HashSet<>(ids);
        List<Event> foundEvents = eventRepository.findAllById(setIds);
        Set<Long> foundIds = foundEvents.stream()
                .map(Event::getId)
                .collect(Collectors.toSet());
        setIds.removeAll(foundIds);
        if (!setIds.isEmpty()) {
            throw new NotFoundException(String.format("Event IDs = %s not found!", setIds));
        }
        return foundEvents;
    }

    @Override
    public List<EventFullDto> getAllAdminFiltered(List<Long> userIds, List<String> strStates, List<Long> catIds,
                                                  LocalDateTime rangeStart, LocalDateTime rangeEnd, int from, int size) {
        if (from % size != 0) {
            throw new InvalidParameterException(String.format(
                    "Parameter 'from'(%d) must be a multiple of parameter 'size'(%d)", from, size));
        }
        return eventRepository.getAllAdminFiltered(userIds, strStates, catIds, rangeStart, rangeEnd, from, size).stream()
                .map(eventMapper::toEventFullDto)
                .collect(Collectors.toList());
        //todo: append views and CR
    }

    @Override
    public EventFullDto adminUpdate(long eventId, UpdateEventDto updateDto) {
        if (LocalDateTime.now().plusHours(1).isAfter(updateDto.getEventDate())) {
            throw new RuntimeException();
        }
        Event event = getById(eventId);
        if (updateDto.getStateAction() != null) {
            if (event.getState() != EventState.PENDING) {
                throw new RuntimeException();
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
        return eventMapper.toEventFullDto(event);
    }

    @Override
    public List<EventShortDto> getAllPublicFiltered(String text, List<Long> catIds, Boolean paid,
                                                    LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                                    Boolean onlyAvailable, EventSort sort, int from, int size) {
        if (from % size != 0) {
            throw new InvalidParameterException(String.format(
                    "Parameter 'from'(%d) must be a multiple of parameter 'size'(%d)", from, size));
        }
        if (rangeStart == null && rangeEnd == null) {
            rangeStart = LocalDateTime.now();
        }
        List<EventShortDto> shortEvents = eventRepository.getAllPublicFiltered(
                text, catIds, paid, rangeStart, rangeEnd, onlyAvailable, from, size).stream()
                .map(eventMapper::toEventShortDto)
                .collect(Collectors.toList());
        //todo: append Views and CR
        if (sort == EventSort.VIEWS) {
            shortEvents.sort(Comparator.comparing(EventShortDto::getViews));
        }
        return shortEvents;
    }

    @Override
    public EventFullDto getPublicById(long id) {
        Event event = eventRepository.findByIdAndState(id, EventState.PUBLISHED).orElseThrow(() ->
                new EntityNotFoundException(String.format("Event ID = %d not found or unavailable!", id)));
        //todo: append views and CR
        return eventMapper.toEventFullDto(event);
    }

    @Override
    public EventFullDto userUpdate(long userId, long eventId, UpdateEventDto updateDto) {
        if (LocalDateTime.now().plusHours(2).isAfter(updateDto.getEventDate())) {
            throw new RuntimeException();
        }
        userService.getById(userId);
        Event event = getById(eventId);
        if (event.getState() == EventState.PUBLISHED) {
            throw new RuntimeException(); //todo: forbidden or conflict?
        }
        if (userId != event.getInitiator().getId()) {
            throw new RuntimeException();
        }
        switch (updateDto.getStateAction()) {
            case SEND_TO_REVIEW:
                event.setState(EventState.PENDING);
                break;
            case CANCEL_REVIEW:
                event.setState(EventState.CANCELED);
                break;
        }
        applyPatchChanges(event, updateDto);
        eventRepository.save(event);
        return eventMapper.toEventFullDto(event);
    }

    @Override
    public List<PartyRequestDto> getAllEventRequests(long eventId, long eventOwnerId) {
        getByIdAndOwnerId(eventId, eventOwnerId);
        return partyRequestService.getAllByEvent(eventId);
    }

    @Override
    public EventRequestStatusUpdateResult updateRequestStatuses(long eventId, long userId,
                                                                EventRequestStatusUpdateRequest updateDto) {
        Event event = getByIdAndOwnerId(eventId, eventId);
        long requestsLimit = event.getParticipantLimit();
        long confirmedRequests = partyRequestService.getNumberConfirmedRequestsByEventId(eventId);
        if (requestsLimit != 0 && requestsLimit <= confirmedRequests) {
            throw new ConflictException("The participant limit has been reached");
        }
        EventRequestStatusUpdateResult result = new EventRequestStatusUpdateResult();
        switch (updateDto.getStatus()) {
            case CONFIRMED:
                partyRequestService.confirmRequests(updateDto.getRequestIds(),
                        requestsLimit == 0 ? updateDto.getRequestIds().size() : requestsLimit - confirmedRequests)
                        .forEach(partyRequestDto -> {
                            if (partyRequestDto.getStatus() == PartyRequestStatus.CONFIRMED) {
                                result.getConfirmedRequests().add(partyRequestDto);
                            } else {
                                result.getRejectedRequests().add(partyRequestDto);
                            }
                        });
                break;

            case REJECTED:
                result.setRejectedRequests(
                        partyRequestService.rejectRequests(updateDto.getRequestIds()));
                break;
        }
        return result;
    }

    private List<EventShortDto> getFilledShortDtos(List<Event> events) {
        List<Long> eventIds = events.stream().map(Event::getId).collect(Collectors.toList());

        Map<Long, Long> mapIdViews = getMapIdViews(eventIds);
        Map<Long, Long> mapIdConfirmedReqs = getMapIdConfirmedReqs(eventIds);

        return events.stream()
                .map(eventMapper::toEventShortDto)
                .peek(eventShortDto -> eventShortDto.setViews(mapIdViews.get(eventShortDto.getId())))
                .peek(eventShortDto -> eventShortDto.setConfirmedRequests(mapIdConfirmedReqs.get(eventShortDto.getId())))
                .collect(Collectors.toList());
    }

    private Map<Long, Long> getMapIdViews(List<Long> eventIds) {
        List<String> uris = eventIds.stream()
                .map(id -> "/events/" + id)
                .collect(Collectors.toList());
        List<ViewStats> stats = statsClient.getStats(LocalDateTime.MIN, LocalDateTime.now(), true, uris);
        return stats.stream()
                .collect(Collectors.toMap(
                        viewStats -> Long.parseLong(viewStats.getUri().substring("/events/".length())),
                        ViewStats::getHits));
    }

    private Map<Long, Long> getMapIdConfirmedReqs(List<Long> eventIds) {
        return partyRequestService.getMapEventIdConfirmedReqsNumber(eventIds);
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
            event.setTitle(newAnnotation);
        }
        String newDescription = updateDto.getDescription();
        if (newDescription != null) {
            event.setTitle(newDescription);
        }
        LocalDateTime newEventDate = updateDto.getEventDate();
        if (newEventDate != null) {
            event.setEventDate(newEventDate);
        }
        Long newCategoryId = updateDto.getCategory();
        if (newCategoryId != null) {
            event.setCategory(categoryService.getById(newCategoryId));
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
