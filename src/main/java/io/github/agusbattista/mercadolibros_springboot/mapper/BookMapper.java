package io.github.agusbattista.mercadolibros_springboot.mapper;

import io.github.agusbattista.mercadolibros_springboot.dto.BookRequestDTO;
import io.github.agusbattista.mercadolibros_springboot.dto.BookResponseDTO;
import io.github.agusbattista.mercadolibros_springboot.model.Book;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(
    componentModel = "spring",
    injectionStrategy = InjectionStrategy.CONSTRUCTOR,
    uses = {GenreMapper.class})
public interface BookMapper {

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "uuid", ignore = true)
  @Mapping(target = "deleted", ignore = true)
  @Mapping(target = "genre", ignore = true)
  Book toEntity(BookRequestDTO request);

  BookResponseDTO toResponse(Book book);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "uuid", ignore = true)
  @Mapping(target = "deleted", ignore = true)
  @Mapping(target = "genre", ignore = true)
  void updateEntityFromRequest(BookRequestDTO request, @MappingTarget Book entity);
}
