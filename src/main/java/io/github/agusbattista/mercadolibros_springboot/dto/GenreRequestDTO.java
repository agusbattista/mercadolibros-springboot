package io.github.agusbattista.mercadolibros_springboot.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record GenreRequestDTO(
    @NotBlank(message = "El nombre del género es obligatorio")
        @Size(max = 100, message = "El nombre del género no puede superar los 100 caracteres")
        String name) {}
