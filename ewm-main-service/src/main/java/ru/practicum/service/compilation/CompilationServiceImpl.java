package ru.practicum.service.compilation;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.dto.compilation.CompilationMapper;
import ru.practicum.dto.compilation.NewCompilationDto;
import ru.practicum.model.Compilation;
import ru.practicum.repository.CompilationRepository;
import ru.practicum.service.event.EventService;

import javax.persistence.EntityNotFoundException;
import java.security.InvalidParameterException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class CompilationServiceImpl implements CompilationService {
    private final CompilationMapper compilationMapper;
    private final CompilationRepository compilationRepository;
    private final EventService eventService;
    @Override
    public CompilationDto addNew(NewCompilationDto newCompilationDto) {
        Compilation compilation = compilationMapper.toCompilation(newCompilationDto);
        compilation.setEvents(eventService.getAllByIds(newCompilationDto.getEvents()));
        Compilation savedCompilation = compilationRepository.save(compilation);
        return compilationMapper.toCompilationDto(savedCompilation);
    }

    @Override
    public void deleteById(long id) {
        compilationRepository.deleteById(id);
    }

    @Override
    public CompilationDto update(NewCompilationDto newCompilationDto, long compId) {
        if (!compilationRepository.existsById(compId)) {
            throw new EntityNotFoundException(String.format("User ID = %d not found!", compId));
        }
        Compilation compilation = compilationMapper.toCompilation(newCompilationDto);
        compilation.setId(compId);
        Compilation updatedComp = compilationRepository.save(compilation);
        return compilationMapper.toCompilationDto(updatedComp);
    }

    @Override
    public List<CompilationDto> getAllDtos(boolean pinned, int from, int size) {
        if (from % size != 0) {
            throw new InvalidParameterException(String.format(
                    "Parameter 'from'(%d) must be a multiple of parameter 'size'(%d)", from, size));
        }
        List<Compilation> compilations =
                compilationRepository.findAllByPinned(pinned, PageRequest.of(from/size, size));
        //todo: fill views and confirmedReq
        return compilations.stream()
                .map(compilationMapper::toCompilationDto)
                .collect(Collectors.toList());
    }

    @Override
    public CompilationDto getDtoById(long compId) {
        return compilationMapper.toCompilationDto(getById(compId));
        //todo: fill views and confirmedReq
    }

    private Compilation getById(long compId) {
        return compilationRepository.findById(compId).orElseThrow(() ->
                new EntityNotFoundException(String.format("Compilation ID = %d not found!", compId)));
    }
}
