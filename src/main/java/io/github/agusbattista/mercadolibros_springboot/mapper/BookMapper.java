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

  // id y deleted se generan en la BDD
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "deleted", ignore = true)
  Book toEntity(BookRequestDTO request);

  BookResponseDTO toResponse(Book book);

  List<BookResponseDTO> toResponseList(List<Book> books);

  // PARA ACTUALIZAR ==> REVISAR
  // 1. Ignoramos 'id' para que no intente sobrescribirlo (aunque el DTO no lo tenga, es buena
  // práctica).
  // 2. Ignoramos 'deleted' para que una actualización normal no pueda "revivir" o borrar un libro.
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "deleted", ignore = true)
  void updateEntityFromRequest(BookRequestDTO request, @MappingTarget Book entity);
}
