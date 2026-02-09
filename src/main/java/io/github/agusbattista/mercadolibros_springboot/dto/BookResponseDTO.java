package io.github.agusbattista.mercadolibros_springboot.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record BookResponseDTO(
    UUID uuid,
    String isbn,
    String title,
    String authors,
    BigDecimal price,
    String description,
    String publisher,
    String genre,
    String imageUrl) {}
