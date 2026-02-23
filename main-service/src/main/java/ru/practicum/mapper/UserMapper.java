package ru.practicum.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.dto.user.NewUserRequest;
import ru.practicum.dto.user.UserDto;
import ru.practicum.dto.user.UserShortDto;
import ru.practicum.model.User;

@Component
public class UserMapper {
    public UserDto toDto(User user) {
        if (user == null) {
            return null;
        }
        return UserDto
                .builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }

    public UserShortDto toShortDto(User user) {
        if (user == null) {
            return null;
        }
        return UserShortDto
                .builder()
                .id(user.getId())
                .name(user.getName())
                .build();
    }

    public User fromNewDto(NewUserRequest dto) {
        if (dto == null) {
            return null;
        }
        return User
                .builder()
                .name(dto.name())
                .email(dto.email())
                .build();
    }
}
