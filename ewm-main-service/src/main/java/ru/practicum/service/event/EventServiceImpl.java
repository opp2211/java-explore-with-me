package ru.practicum.service.event;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.dto.ViewStats;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventMapper;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.dto.event.NewEventDto;
import ru.practicum.model.Event;
import ru.practicum.model.EventState;
import ru.practicum.repository.EventRepository;
import ru.practicum.service.category.CategoryService;
import ru.practicum.service.user.UserService;
import ru.practicum.statsclient.StatsClient;

import javax.persistence.EntityNotFoundException;
import java.security.InvalidParameterException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
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
}
