package dev.piloto.core.web;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/** Parámetros de paginación de entrada: {@code page} base 1, {@code q} búsqueda libre opcional. */
public record PageRequestDto(
        @Min(value = 1, message = "debe ser al menos 1") Integer page,
        @Min(value = 1, message = "debe ser al menos 1") @Max(value = 100, message = "máximo 100") Integer size,
        String q) {

    public static final int DEFAULT_PAGE = 1;
    public static final int DEFAULT_SIZE = 20;

    public PageRequestDto {
        page = page == null ? DEFAULT_PAGE : page;
        size = size == null ? DEFAULT_SIZE : size;
    }

    public Pageable toPageable(Sort sort) {
        return PageRequest.of(page - 1, size, sort);
    }
}
