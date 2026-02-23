package ru.practicum.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.CategoryDto;
import ru.practicum.dto.NewCategoryDto;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.CategoryMapper;
import ru.practicum.model.Category;
import ru.practicum.repository.CategoryRepository;
import ru.practicum.repository.EventRepository;
import ru.practicum.service.CategoryService;
import ru.practicum.util.PageRequestUtil;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;
    private final CategoryMapper categoryMapper;

    @Override
    @Transactional
    public CategoryDto createCategory(NewCategoryDto dto) {
        if (categoryRepository.existsByName(dto.name())) {
            throw new ConflictException("Category with name='" + dto.name() + "' already exists");
        }
        Category category = categoryMapper.fromNewDto(dto);
        return categoryMapper.toDto(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public CategoryDto updateCategory(Long categoryId, CategoryDto dto) {
        Category category = findCategory(categoryId);
        if (dto.name() != null && !dto.name().equals(category.getName()) && categoryRepository.existsByName(dto.name())) {
            throw new ConflictException("Category with name='" + dto.name() + "' already exists");
        }
        categoryMapper.update(category, dto);
        return categoryMapper.toDto(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public void deleteCategory(Long categoryId) {
        Category category = findCategory(categoryId);
        boolean usedByEvents = eventRepository.existsByCategoryId(categoryId);
        if (usedByEvents) {
            throw new ConflictException("The category is not empty");
        }
        categoryRepository.delete(category);
    }

    @Override
    public List<CategoryDto> getCategories(int from, int size) {
        return categoryRepository
                .findAll(PageRequestUtil.toPageable(from, size))
                .stream()
                .map(categoryMapper::toDto)
                .toList();
    }

    @Override
    public CategoryDto getCategory(Long categoryId) {
        return categoryMapper.toDto(findCategory(categoryId));
    }

    private Category findCategory(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Category with id=" + categoryId + " was not found"));
    }
}
