package io.github.agusbattista.mercadolibros_springboot.dto;

import java.math.BigDecimal;

public record BookResponseDTO(
    String isbn,
    String title,
    String authors,
    BigDecimal price,
    String description,
    String publisher,
    String genre,
    String imageUrl) {}
