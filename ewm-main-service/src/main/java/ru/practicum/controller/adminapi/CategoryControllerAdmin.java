package ru.practicum.controller.adminapi;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.category.NewCategoryDto;
import ru.practicum.dto.category.UpdateCategoryDto;
import ru.practicum.service.category.CategoryService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;

@RestController
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
@Validated
public class CategoryControllerAdmin {
    private final CategoryService categoryService;

    @PostMapping
    public CategoryDto addNew(@RequestBody @Valid NewCategoryDto newCategoryDto) {
        return categoryService.addNew(newCategoryDto);
    }

    @DeleteMapping("/{catId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteById(@PathVariable @Positive long catId) {
        categoryService.deleteById(catId);
    }

    @PatchMapping("/{catId}")
    public CategoryDto update(@PathVariable @Positive long catId,
                              @RequestBody @Valid UpdateCategoryDto updateCategoryDto) {
        return categoryService.update(catId, updateCategoryDto);
    }
}
