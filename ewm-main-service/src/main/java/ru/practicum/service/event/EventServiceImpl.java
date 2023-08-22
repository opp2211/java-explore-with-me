package ru.practicum.service.event;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.dto.ViewStats;
import ru.practicum.dto.event.*;
import ru.practicum.model.Event;
import ru.practicum.model.EventState;
import ru.practicum.repository.EventRepository;
import ru.practicum.service.category.CategoryService;
import ru.practicum.service.user.UserService;
import ru.practicum.statsclient.StatsClient;

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
        if (from % size != 0) {
            throw new InvalidParameterException(String.format(
                    "Parameter 'from'(%d) must be a multiple of parameter 'size'(%d)", from, size));
        }
        List<EventShortDto> shortEvents = eventRepository
                .findAllByInitiatorId(userId, PageRequest.of(from/size, size))
                .stream()
                .map(eventMapper::toEventShortDto)
                .collect(Collectors.toList());

        if (!shortEvents.isEmpty()) {
            List<String> uris = shortEvents.stream()
                    .map(shortEvent -> "/events/" + shortEvent.getId())
                    .collect(Collectors.toList());
            List<ViewStats> stats = statsClient.getStats(LocalDateTime.MIN, LocalDateTime.now(), false, uris);
            Map<Long, Long> mapStats = stats.stream()
                    .collect(Collectors.toMap(
                            viewStats -> Long.parseLong(viewStats.getUri().substring("/events/".length())),
                            ViewStats::getHits));
            shortEvents.forEach(shortEvent -> {
                if (mapStats.containsKey(shortEvent.getId())) {
                    shortEvent.setViews(mapStats.get(shortEvent.getId()));
                }
            });
        }
        //todo: peek confirmedRequests
        return shortEvents;
    }

    @Override
    public EventFullDto getByIdAndUserId(long userId, long eventId) {
        EventFullDto eventFullDto = eventMapper.toEventFullDto(
                eventRepository.findByIdAndInitiatorId(eventId, userId));
        if (eventFullDto != null) {
            List<String> uris = List.of("/events/" + eventFullDto.getId());
            List<ViewStats> stats = statsClient.getStats(LocalDateTime.MIN, LocalDateTime.now(), false, uris);
            Map<Long, Long> mapStats = stats.stream()
                    .collect(Collectors.toMap(
                            viewStats -> Long.parseLong(viewStats.getUri().substring("/events/".length())),
                            ViewStats::getHits));
            if (mapStats.containsKey(eventFullDto.getId())) {
                eventFullDto.setViews(mapStats.get(eventFullDto.getId()));
            }
        }
        //todo
        return eventFullDto;
    }

    @Override
    public Event getById(long id) {
        return eventRepository.findById(id).orElseThrow(() ->
                new EntityNotFoundException(String.format("Event ID = %d not found!", id)));
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
            throw new EntityNotFoundException(String.format("Event IDs = %s not found!", setIds));
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
            throw new RuntimeException();
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
