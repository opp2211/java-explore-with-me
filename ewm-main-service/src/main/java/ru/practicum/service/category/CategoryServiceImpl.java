package ru.practicum.service.category;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.category.CategoryMapper;
import ru.practicum.dto.category.NewCategoryDto;
import ru.practicum.exception.NotFoundException;
import ru.practicum.model.Category;
import ru.practicum.repository.CategoryRepository;

import java.security.InvalidParameterException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    public CategoryDto addNew(NewCategoryDto newCategoryDto) {
        Category newCategory = categoryRepository.save(categoryMapper.toCategory(newCategoryDto));
        return categoryMapper.toCategoryDto(newCategory);
        //todo: настройка хендлера под обработку исключения "Integrity constraint has been violated."
    }

    @Override
    public void deleteById(long catId) {
        categoryRepository.deleteById(catId);
        //todo: проверка на наличие событий в категории или проброс ислючение от БД при ошибке удаления
        //todo: ? проверка на наличие категории с переданным ID
    }

    @Override
    public CategoryDto update(long catId, NewCategoryDto newCategoryDto) {
        Category category = getById(catId);
        category.setName(newCategoryDto.getName());
        categoryRepository.save(category);
        return categoryMapper.toCategoryDto(category);
        //todo: настройка хендлера под обработку исключения "Integrity constraint has been violated."
    }

    @Override
    public Category getById(long id) {
        return categoryRepository.findById(id).orElseThrow(() ->
                new NotFoundException(String.format("Category ID = %d not found!", id)));
    }

    @Override
    public CategoryDto getDtoById(long id) {
        return categoryMapper.toCategoryDto(getById(id));
    }

    @Override
    public List<CategoryDto> getAllDtos(int from, int size) {
        if (from % size != 0) {
            throw new InvalidParameterException(String.format(
                    "Parameter 'from'(%d) must be a multiple of parameter 'size'(%d)", from, size));
        }
        return categoryRepository.findAll(PageRequest.of(from/size, size)).stream()
                .map(categoryMapper::toCategoryDto)
                .collect(Collectors.toList());
    }
}
