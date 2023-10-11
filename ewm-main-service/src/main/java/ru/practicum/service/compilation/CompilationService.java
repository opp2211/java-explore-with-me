package ru.practicum.service.compilation;

import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.dto.compilation.NewCompilationDto;
import ru.practicum.dto.compilation.UpdateCompilationDto;

import java.util.List;

public interface CompilationService {
    CompilationDto addNew(NewCompilationDto newCompilationDto);

    void deleteById(long id);

    CompilationDto update(UpdateCompilationDto updateCompilationDto, long compId);

    List<CompilationDto> getAllDtos(Boolean pinned, int from, int size);

    CompilationDto getDtoById(long compId);
}
