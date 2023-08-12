package ru.practicum.dto.category;

import org.mapstruct.Mapper;
import ru.practicum.model.Category;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    CategoryDto toCategoryDto(Category category);
    Category toCategory(NewCategoryDto newCategoryDto);
}
