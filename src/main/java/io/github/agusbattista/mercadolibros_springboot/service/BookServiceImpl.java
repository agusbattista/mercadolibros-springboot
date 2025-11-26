package io.github.agusbattista.mercadolibros_springboot.service;

import io.github.agusbattista.mercadolibros_springboot.exception.ResourceNotFoundException;
import io.github.agusbattista.mercadolibros_springboot.model.Book;
import io.github.agusbattista.mercadolibros_springboot.repository.BookRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BookServiceImpl implements BookService {

  private final BookRepository bookRepository;

  @Autowired
  public BookServiceImpl(BookRepository bookRepository) {
    this.bookRepository = bookRepository;
  }

  @Override
  public List<Book> findAll() {
    return bookRepository.findAll();
  }

  @Override
  public Optional<Book> findByIsbn(String isbn) {
    return bookRepository.findByIsbn(isbn);
  }

  @Override
  public Book save(Book book) {
    return bookRepository.save(book);
  }

  @Override
  public void deleteByIsbn(String isbn) {
    Book book =
        bookRepository
            .findByIsbn(isbn)
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "No se puede eliminar. " + this.isbnNotFound(isbn)));
    bookRepository.delete(book);
  }

  @Override
  public Book update(String isbn, Book book) {
    return this.findByIsbn(isbn)
        .map(
            existingBook -> {
              existingBook.setTitle(book.getTitle());
              existingBook.setAuthors(book.getAuthors());
              existingBook.setPrice(book.getPrice());
              existingBook.setDescription(book.getDescription());
              existingBook.setPublisher(book.getPublisher());
              return this.save(existingBook);
            })
        .orElseThrow(() -> new ResourceNotFoundException(this.isbnNotFound(isbn)));
  }

  private String isbnNotFound(String isbn) {
    return "Libro con ISBN: " + isbn + " no encontrado.";
  }
}
