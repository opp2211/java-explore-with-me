package ru.practicum.dto.event;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.dto.category.CategoryMapper;
import ru.practicum.dto.user.UserMapper;
import ru.practicum.model.Event;

@Mapper(componentModel = "spring", uses = {CategoryMapper.class, UserMapper.class})
public interface EventMapper {
    @Mapping(source = "newEventDto.location.lat", target = "locationLat")
    @Mapping(source = "newEventDto.location.lon", target = "locationLon")
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "id", ignore = true)
    Event toEvent(NewEventDto newEventDto);

    @Mapping(source = "event.locationLat", target = "location.lat")
    @Mapping(source = "event.locationLon", target = "location.lon")
    EventFullDto toEventFullDto(Event event);

    EventShortDto toEventShortDto(Event event);
}
