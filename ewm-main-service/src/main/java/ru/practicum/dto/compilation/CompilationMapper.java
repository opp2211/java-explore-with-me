package ru.practicum.dto.compilation;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;
import ru.practicum.dto.event.EventMapper;
import ru.practicum.model.Compilation;
import ru.practicum.model.Event;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Mapper(componentModel = "spring")
public abstract class CompilationMapper {

    @Autowired
    EventMapper eventMapper;
    @Mapping(target = "events", source = "events")
    public abstract Compilation toCompilation(NewCompilationDto newCompilationDto, List<Event> events);

    @Mapping(target = "events", expression =
            "java(eventMapper.toEventShortDtoList(compilation.getEvents(), eventsViewsMap, eventsConfReqsMap))")
    public abstract CompilationDto toCompilationDto(
            Compilation compilation, Map<Long, Long> eventsViewsMap, Map<Long, Long> eventsConfReqsMap);

    public List<CompilationDto> toCompilationDtoList(List<Compilation> compilations,
                                                      Map<Long, Long> eventsViewsMap,
                                                      Map<Long, Long> eventsConfReqsMap) {
        if (compilations == null)
            return null;

        List<CompilationDto> list = new ArrayList<>(compilations.size());
        for (Compilation compilation : compilations) {
            list.add(toCompilationDto(compilation, eventsViewsMap, eventsConfReqsMap));
        }
        return list;
    }
}
