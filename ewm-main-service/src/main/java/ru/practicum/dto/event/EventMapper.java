package ru.practicum.dto.event;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.dto.category.CategoryMapper;
import ru.practicum.dto.user.UserMapper;
import ru.practicum.model.Category;
import ru.practicum.model.Event;
import ru.practicum.model.EventState;
import ru.practicum.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Mapper(componentModel = "spring", uses = {CategoryMapper.class, UserMapper.class},
        imports = {EventState.class, LocalDateTime.class})
public interface EventMapper {
    @Mapping(source = "newEventDto.location.lat", target = "locationLat")
    @Mapping(source = "newEventDto.location.lon", target = "locationLon")
    @Mapping(target = "category", source = "category")
    @Mapping(target = "initiator", source = "initiator")
    @Mapping(target = "createdOn", expression = "java(LocalDateTime.now())")
    @Mapping(target = "state", expression = "java(EventState.PENDING)")
    @Mapping(target = "id", ignore = true)
    Event toEvent(NewEventDto newEventDto, Category category, User initiator);

    @Mapping(source = "event.locationLat", target = "location.lat")
    @Mapping(source = "event.locationLon", target = "location.lon")
    @Mapping(target = "views", source = "views")
    @Mapping(target = "confirmedRequests", source = "confirmedRequests")
    EventFullDto toEventFullDto(Event event, Long views, Long confirmedRequests);

    @Mapping(target = "views", source = "views")
    @Mapping(target = "confirmedRequests", source = "confirmedRequests")
    EventShortDto toEventShortDto(Event event, Long views, Long confirmedRequests);

    default List<EventShortDto> toEventShortDtoList(
            List<Event> events, Map<Long, Long> eventsViewsMap, Map<Long, Long> eventsConfReqsMap) {
        if (events == null) {
            return null;
        }

        List<EventShortDto> list = new ArrayList<>(events.size());
        for (Event event : events) {
            list.add(toEventShortDto(event, eventsViewsMap.get(event.getId()), eventsConfReqsMap.get(event.getId())));
        }
        return list;
    }

    default List<EventFullDto> toEventFullDtoList(
            List<Event> events, Map<Long, Long> eventsViewsMap, Map<Long, Long> eventsConfReqsMap) {
        if (events == null) {
            return null;
        }

        List<EventFullDto> list = new ArrayList<>(events.size());
        for (Event event : events) {
            list.add(toEventFullDto(event, eventsViewsMap.get(event.getId()), eventsConfReqsMap.get(event.getId())));
        }
        return list;
    }
}
