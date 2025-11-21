package io.github.agusbattista.mercadolibros_springboot.service;

import io.github.agusbattista.mercadolibros_springboot.model.Book;
import java.util.List;
import java.util.Optional;

public interface BookService {

  List<Book> findAll();

  Optional<Book> findById(Long id);

  Book save(Book book);

  void deleteById(Long id);

  Book update(Long id, Book book);
}
