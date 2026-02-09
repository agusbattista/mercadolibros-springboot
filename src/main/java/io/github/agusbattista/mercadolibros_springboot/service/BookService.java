package io.github.agusbattista.mercadolibros_springboot.service;

import io.github.agusbattista.mercadolibros_springboot.dto.BookRequestDTO;
import io.github.agusbattista.mercadolibros_springboot.dto.BookResponseDTO;
import io.github.agusbattista.mercadolibros_springboot.dto.PagedResponse;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;

public interface BookService {

  PagedResponse<BookResponseDTO> findAll(Pageable pageable);

  Optional<BookResponseDTO> findByUuid(UUID uuid);

  Optional<BookResponseDTO> findByIsbn(String isbn);

  PagedResponse<BookResponseDTO> findBooksByCriteria(
      String title, String authors, String genre, String publisher, Pageable pageable);

  BookResponseDTO create(BookRequestDTO book);

  void deleteByUuid(UUID uuid);

  BookResponseDTO update(UUID uuid, BookRequestDTO book);
}
