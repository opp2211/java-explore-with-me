package ru.practicum.service.category;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.category.CategoryMapper;
import ru.practicum.dto.category.NewCategoryDto;
import ru.practicum.dto.category.UpdateCategoryDto;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.model.Category;
import ru.practicum.repository.CategoryRepository;
import ru.practicum.repository.EventRepository;
import ru.practicum.validator.StaticValidator;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final EventRepository eventRepository;

    @Override
    public CategoryDto addNew(NewCategoryDto newCategoryDto) {
        Category newCategory = categoryRepository.save(categoryMapper.toCategory(newCategoryDto));
        return categoryMapper.toCategoryDto(newCategory);
    }

    @Override
    public void deleteById(long catId) {
        getById(catId);
        if (eventRepository.existsByCategoryId(catId)) {
            throw new ConflictException("The category is not empty");
        }
        categoryRepository.deleteById(catId);
    }

    @Override
    public CategoryDto update(long catId, UpdateCategoryDto updateDto) {
        Category category = getById(catId);
        if (updateDto.getName() != null) {
            category.setName(updateDto.getName());
        }
        categoryRepository.save(category);
        return categoryMapper.toCategoryDto(category);
    }

    @Transactional(readOnly = true)
    @Override
    public CategoryDto getDtoById(long id) {
        return categoryMapper.toCategoryDto(getById(id));
    }

    @Transactional(readOnly = true)
    @Override
    public List<CategoryDto> getAllDtos(int from, int size) {
        StaticValidator.validateFromSize(from, size);
        return categoryRepository.findAll(PageRequest.of(from / size, size)).stream()
                .map(categoryMapper::toCategoryDto)
                .collect(Collectors.toList());
    }

    private Category getById(long id) {
        return categoryRepository.findById(id).orElseThrow(() ->
                new NotFoundException(String.format("Category ID = %d not found!", id)));
    }
}
