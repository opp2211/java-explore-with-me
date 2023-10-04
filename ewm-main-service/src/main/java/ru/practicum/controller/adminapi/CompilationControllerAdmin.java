package ru.practicum.controller.adminapi;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.dto.compilation.NewCompilationDto;
import ru.practicum.service.compilation.CompilationService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;

@RestController
@RequestMapping("/admin/compilations")
@RequiredArgsConstructor
@Validated
public class CompilationControllerAdmin {
    private final CompilationService compilationService;

    @PostMapping
    public CompilationDto addNew(@Valid @RequestBody NewCompilationDto newCompilationDto) {
        return compilationService.addNew(newCompilationDto);
    }

    @DeleteMapping("/{compId}")
    public void deleteById(@Positive @PathVariable long compId) {
        compilationService.deleteById(compId);
    }

    @PatchMapping("/{compId}")
    public CompilationDto update(@Valid @RequestBody NewCompilationDto newCompilationDto,
                                 @Positive @PathVariable long compId) {
        return compilationService.update(newCompilationDto, compId);
    }
}
