package ru.practicum.service;

import ru.practicum.dto.CategoryDto;
import ru.practicum.dto.NewCategoryDto;

import java.util.List;

public interface CategoryService {
    CategoryDto createCategory(NewCategoryDto dto);

    CategoryDto updateCategory(Long categoryId, CategoryDto dto);

    void deleteCategory(Long categoryId);

    List<CategoryDto> getCategories(int from, int size);

    CategoryDto getCategory(Long categoryId);
}
