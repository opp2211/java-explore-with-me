package ru.practicum.service.compilation;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.dto.ViewStats;
import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.dto.compilation.CompilationMapper;
import ru.practicum.dto.compilation.NewCompilationDto;
import ru.practicum.dto.compilation.UpdateCompilationDto;
import ru.practicum.dto.party_request.ParticipantsNumber;
import ru.practicum.exception.NotFoundException;
import ru.practicum.model.Compilation;
import ru.practicum.model.Event;
import ru.practicum.model.PartyRequestStatus;
import ru.practicum.repository.CompilationRepository;
import ru.practicum.repository.EventRepository;
import ru.practicum.repository.PartyRequestRepository;
import ru.practicum.statsclient.StatsClient;
import ru.practicum.validator.StaticValidator;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class CompilationServiceImpl implements CompilationService {
    private final CompilationMapper compilationMapper;
    private final CompilationRepository compilationRepository;
    private final PartyRequestRepository partyRequestRepository;
    private final StatsClient statsClient;
    private final EventRepository eventRepository;
    @Override
    public CompilationDto addNew(NewCompilationDto newCompilationDto) {
        List<Long> eventIds = newCompilationDto.getEvents();
        List<Event> events = getEventsByIds(eventIds);
        Compilation compilation = compilationMapper.toCompilation(newCompilationDto, events);
        Compilation savedCompilation = compilationRepository.save(compilation);
        return compilationMapper.toCompilationDto(
                savedCompilation, getEventsViewsMap(eventIds), getEventsConfReqsMap(eventIds));
    }

    @Override
    public void deleteById(long id) {
        getById(id);
        compilationRepository.deleteById(id);
    }

    @Override
    public CompilationDto update(UpdateCompilationDto updateDto, long compId) {
        Compilation compilation = getById(compId);
        if (updateDto.getTitle() != null) {
            compilation.setTitle(updateDto.getTitle());
        }
        if (updateDto.getPinned() != null) {
            compilation.setPinned(updateDto.getPinned());
        }
        if (updateDto.getEvents() != null) {
            compilation.setEvents(getEventsByIds(updateDto.getEvents()));
        }
        Compilation updatedComp = compilationRepository.save(compilation);
        return compilationMapper.toCompilationDto(
                updatedComp, getEventsViewsMap(updateDto.getEvents()), getEventsConfReqsMap(updateDto.getEvents()));
    }

    @Override
    public List<CompilationDto> getAllDtos(Boolean pinned, int from, int size) {
        StaticValidator.validateFromSize(from, size);

        Pageable pageable = PageRequest.of(from/size, size);
        List<Compilation> compilations = pinned != null ?
                compilationRepository.findAllByPinned(pinned, pageable) :
                compilationRepository.findAll(pageable).toList();

        Set<Long> eventIds = compilations.stream()
                .flatMap(compilation -> compilation.getEvents().stream())
                .map(Event::getId)
                .collect(Collectors.toSet());

        return compilationMapper.toCompilationDtoList(
                compilations, getEventsViewsMap(eventIds), getEventsConfReqsMap(eventIds));
    }

    @Override
    public CompilationDto getDtoById(long compId) {
        Compilation compilation = getById(compId);
        List<Long> eventIds = compilation.getEvents().stream()
                .map(Event::getId)
                .collect(Collectors.toList());
        return compilationMapper.toCompilationDto(
                compilation, getEventsViewsMap(eventIds), getEventsConfReqsMap(eventIds));
    }

    private Compilation getById(long compId) {
        return compilationRepository.findById(compId).orElseThrow(() ->
                new NotFoundException(String.format("Compilation ID = %d not found!", compId)));
    }

    private Map<Long, Long> getEventsViewsMap(Collection<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            return Collections.EMPTY_MAP;
        }
        Set<String> urisForStatsMap = eventIds.stream()
                .map(id -> "/events/" + id)
                .collect(Collectors.toSet());
        return statsClient.getStats(LocalDateTime.ofInstant(Instant.EPOCH, ZoneId.systemDefault()),
                        LocalDateTime.now(), true, urisForStatsMap)
                .stream()
                .collect(Collectors.toMap(
                        viewStats -> Long.parseLong(viewStats.getUri().substring("/events/".length())),
                        ViewStats::getHits));
    }

    private Map<Long, Long> getEventsConfReqsMap(Collection<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            return Collections.EMPTY_MAP;
        }
        return partyRequestRepository
                .getAllParticipantsNumberByStatusAndEventIdIn(PartyRequestStatus.CONFIRMED, eventIds)
                .stream()
                .collect(Collectors.toMap(
                        ParticipantsNumber::getEventId,
                        ParticipantsNumber::getNumber));
    }

    private List<Event> getEventsByIds(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.EMPTY_LIST;
        }
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
}
