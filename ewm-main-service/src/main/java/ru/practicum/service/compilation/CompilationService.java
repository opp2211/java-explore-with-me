package ru.practicum.service.compilation;

import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.dto.compilation.NewCompilationDto;

import java.util.List;

public interface CompilationService {
    CompilationDto addNew(NewCompilationDto newCompilationDto);

    void deleteById(long id);

    CompilationDto update(NewCompilationDto newCompilationDto, long compId);

    List<CompilationDto> getAllDtos(boolean pinned, int from, int size);

    CompilationDto getDtoById(long compId);
}
