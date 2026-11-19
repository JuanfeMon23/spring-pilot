package dev.piloto.core.web;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

/** Respuesta paginada común: {@code {items, total, page, size}} (page base 1). */
public record PageDto<T>(List<T> items, long total, int page, int size) {

    public static <E, T> PageDto<T> from(Page<E> page, Function<E, T> mapper) {
        return new PageDto<>(page.getContent().stream().map(mapper).toList(),
                page.getTotalElements(), page.getNumber() + 1, page.getSize());
    }
}
