package ru.practicum.service.category;

import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.category.NewCategoryDto;
import ru.practicum.dto.category.UpdateCategoryDto;

import java.util.List;

public interface CategoryService {
    CategoryDto addNew(NewCategoryDto newCategoryDto);

    void deleteById(long catId);

    CategoryDto update(long catId, UpdateCategoryDto updateCategoryDto);

    CategoryDto getDtoById(long id);

    List<CategoryDto> getAllDtos(int from, int size);

}
