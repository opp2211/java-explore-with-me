package ru.practicum.service.compilation;

import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.dto.compilation.NewCompilationDto;

public interface CompilationService {
    CompilationDto addNew(NewCompilationDto newCompilationDto);

    void deleteById(long id);

    CompilationDto update(NewCompilationDto newCompilationDto, long compId);
}
