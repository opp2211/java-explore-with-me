package ru.practicum.controller.adminapi;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.category.NewCategoryDto;
import ru.practicum.service.category.CategoryService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;

@RestController
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
@Validated
public class CategoryController {
    private final CategoryService categoryService;

    @PostMapping
    public CategoryDto addNew(@Valid @RequestBody NewCategoryDto newCategoryDto) {
        return categoryService.addNew(newCategoryDto);
    }

    @DeleteMapping("/{catId}")
    public void deleteById(@Positive @PathVariable long catId) {
        categoryService.deleteById(catId);
    }

    @PatchMapping("/{catId}")
    public CategoryDto update(@Positive @PathVariable long catId,
                       @Valid @RequestBody NewCategoryDto newCategoryDto) {
        return categoryService.update(catId, newCategoryDto);
    }
}
