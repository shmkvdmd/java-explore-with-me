package ru.practicum.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.category.NewCategoryDto;
import ru.practicum.model.Category;

@Component
public class CategoryMapper {
    public CategoryDto toDto(Category category) {
        if (category == null) {
            return null;
        }
        return CategoryDto
                .builder()
                .id(category.getId())
                .name(category.getName())
                .build();
    }

    public Category fromNewDto(NewCategoryDto dto) {
        if (dto == null) {
            return null;
        }
        return Category
                .builder()
                .name(dto.name())
                .build();
    }

    public void update(Category category, CategoryDto dto) {
        if (category == null || dto == null) {
            return;
        }
        if (dto.name() != null) {
            category.setName(dto.name());
        }
    }
}
