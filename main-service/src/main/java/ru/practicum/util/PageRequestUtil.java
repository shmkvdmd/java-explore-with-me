package ru.practicum.util;

import lombok.experimental.UtilityClass;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@UtilityClass
public class PageRequestUtil {
    public static Pageable toPageable(int from, int size) {
        if (size <= 0) {
            throw new IllegalArgumentException("Size must be greater than 0");
        }
        return PageRequest.of(from / size, size);
    }
}
