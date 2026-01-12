package io.github.agusbattista.mercadolibros_springboot.mapper;

import io.github.agusbattista.mercadolibros_springboot.dto.BookRequestDTO;
import io.github.agusbattista.mercadolibros_springboot.dto.BookResponseDTO;
import io.github.agusbattista.mercadolibros_springboot.model.Book;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface BookMapper {

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "deleted", ignore = true)
  Book toEntity(BookRequestDTO request);

  BookResponseDTO toResponse(Book book);

  List<BookResponseDTO> toResponseList(List<Book> books);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "deleted", ignore = true)
  void updateEntityFromRequest(BookRequestDTO request, @MappingTarget Book entity);
}
