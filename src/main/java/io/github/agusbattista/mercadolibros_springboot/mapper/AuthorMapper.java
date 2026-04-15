package io.github.agusbattista.mercadolibros_springboot.mapper;

import io.github.agusbattista.mercadolibros_springboot.dto.AuthorRequestDTO;
import io.github.agusbattista.mercadolibros_springboot.dto.AuthorResponseDTO;
import io.github.agusbattista.mercadolibros_springboot.model.Author;
import org.mapstruct.InheritConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface AuthorMapper {

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "uuid", ignore = true)
  @Mapping(target = "deleted", ignore = true)
  Author toEntity(AuthorRequestDTO request);

  AuthorResponseDTO toResponse(Author author);

  @InheritConfiguration
  void updateEntityFromRequest(AuthorRequestDTO request, @MappingTarget Author entity);
}
