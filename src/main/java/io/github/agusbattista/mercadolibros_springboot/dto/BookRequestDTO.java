package io.github.agusbattista.mercadolibros_springboot.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import org.hibernate.validator.constraints.ISBN;
import org.hibernate.validator.constraints.URL;

public record BookRequestDTO(
    @NotNull(message = "El ISBN es obligatorio")
        // type = ANY acepta ISBN-10 o ISBN-13
        @ISBN(type = ISBN.Type.ANY, message = "El formato del ISBN no es válido")
        String isbn,
    @NotBlank(message = "El título es obligatorio")
        @Size(max = 255, message = "El título no puede exceder los 255 caracteres")
        String title,
    @NotBlank(message = "El autor es obligatorio")
        @Size(
            max = 255,
            message = "El nombre del autor o de los autores no puede exceder los 255 caracteres")
        String authors,
    @NotNull(message = "El precio es obligatorio")
        @PositiveOrZero(message = "El precio debe ser mayor o igual a cero")
        @Digits(
            integer = 8,
            fraction = 2,
            message = "El precio debe tener formato monetario correcto. Ejemplo: 100.00")
        BigDecimal price,
    @NotBlank(message = "La descripción es obligatoria")
        @Size(
            min = 20,
            max = 5000,
            message = "La descripción debe tener entre 20 y 5000 caracteres")
        String description,
    @NotBlank(message = "La editorial es obligatoria")
        @Size(max = 255, message = "La editorial no puede superar los 255 caracteres")
        String publisher,
    @NotNull(message = "El género es obligatorio")
        @Positive(message = "El ID del género debe ser un número positivo")
        Long genreId,
    @NotBlank(message = "La URL de la imagen es obligatoria")
        @URL(message = "La URL de la imagen debe ser válida")
        @Size(max = 500, message = "La URL de la imagen no puede superar los 500 caracteres")
        String imageUrl) {}
