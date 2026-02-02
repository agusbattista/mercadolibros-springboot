package io.github.agusbattista.mercadolibros_springboot.service;

import io.github.agusbattista.mercadolibros_springboot.dto.BookRequestDTO;
import io.github.agusbattista.mercadolibros_springboot.dto.BookResponseDTO;
import io.github.agusbattista.mercadolibros_springboot.dto.PagedResponse;
import java.util.Optional;
import org.springframework.data.domain.Pageable;

public interface BookService {

  PagedResponse<BookResponseDTO> findAll(Pageable pageable);

  Optional<BookResponseDTO> findByIsbn(String isbn);

  PagedResponse<BookResponseDTO> findBooksByCriteria(
      String title, String authors, String genre, String publisher, Pageable pageable);

  BookResponseDTO save(BookRequestDTO book);

  void deleteByIsbn(String isbn);

  BookResponseDTO update(String isbn, BookRequestDTO book);
}
