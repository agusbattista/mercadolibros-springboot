package io.github.agusbattista.mercadolibros_springboot.service;

import io.github.agusbattista.mercadolibros_springboot.model.Book;
import java.util.List;
import java.util.Optional;

public interface BookService {

  List<Book> findAll();

  Optional<Book> findByIsbn(String isbn);

  Book save(Book book);

  void deleteByIsbn(String isbn);

  Book update(String isbn, Book book);
}
