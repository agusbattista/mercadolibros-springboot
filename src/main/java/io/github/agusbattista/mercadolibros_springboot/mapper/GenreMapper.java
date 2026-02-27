package io.github.agusbattista.mercadolibros_springboot.mapper;

import io.github.agusbattista.mercadolibros_springboot.dto.GenreResponseDTO;
import io.github.agusbattista.mercadolibros_springboot.model.Genre;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface GenreMapper {
  GenreResponseDTO toResponse(Genre genre);
}
