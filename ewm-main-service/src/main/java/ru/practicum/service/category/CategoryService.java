package ru.practicum.service.category;

import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.category.NewCategoryDto;
import ru.practicum.model.Category;

import java.util.List;

public interface CategoryService {
    CategoryDto addNew(NewCategoryDto newCategoryDto);
    void deleteById(long catId);
    CategoryDto update(long catId, NewCategoryDto newCategoryDto);
    Category getById(long id);
    CategoryDto getDtoById(long id);
    List<CategoryDto> getAllDtos(int from, int size);

}
