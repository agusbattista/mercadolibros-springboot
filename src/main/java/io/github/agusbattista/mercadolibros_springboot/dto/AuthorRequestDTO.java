package io.github.agusbattista.mercadolibros_springboot.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AuthorRequestDTO(
    @NotBlank(message = "El nombre del autor es obligatorio")
        @Size(max = 255, message = "El nombre del autor no puede exceder los 255 caracteres")
        String fullName,
    @NotBlank(message = "Se requiere una breve biografía del autor")
        @Size(min = 20, max = 5000, message = "La biografía debe tener entre 20 y 5000 caracteres")
        String biography) {}
