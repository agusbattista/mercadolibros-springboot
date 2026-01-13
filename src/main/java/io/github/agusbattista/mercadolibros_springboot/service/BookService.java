package io.github.agusbattista.mercadolibros_springboot.service;

import io.github.agusbattista.mercadolibros_springboot.dto.BookRequestDTO;
import io.github.agusbattista.mercadolibros_springboot.dto.BookResponseDTO;
import java.util.List;
import java.util.Optional;

public interface BookService {

  List<BookResponseDTO> findAll();

  Optional<BookResponseDTO> findByIsbn(String isbn);

  List<BookResponseDTO> findBooksByCriteria(
      String title, String authors, String genre, String publisher);

  BookResponseDTO save(BookRequestDTO book);

  void deleteByIsbn(String isbn);

  BookResponseDTO update(String isbn, BookRequestDTO book);
}
