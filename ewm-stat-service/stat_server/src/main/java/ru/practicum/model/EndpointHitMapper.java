package ru.practicum.model;

import org.mapstruct.Mapper;
import ru.practicum.dto.EndpointHitDto;

@Mapper(componentModel = "spring")
public abstract class EndpointHitMapper {
    public abstract EndpointHit toEndpointHit(EndpointHitDto endpointHitDto);
}
