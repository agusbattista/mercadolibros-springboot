package io.github.agusbattista.mercadolibros_springboot.dto;

import java.util.UUID;

public record AuthorResponseDTO(UUID uuid, String fullName, String biography) {}
