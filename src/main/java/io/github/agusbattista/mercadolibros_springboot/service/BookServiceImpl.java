package io.github.agusbattista.mercadolibros_springboot.service;

import io.github.agusbattista.mercadolibros_springboot.exception.ResourceNotFoundException;
import io.github.agusbattista.mercadolibros_springboot.model.Book;
import io.github.agusbattista.mercadolibros_springboot.repository.BookRepository;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
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
    Objects.requireNonNull(isbn, "El ISBN no puede ser nulo para realizar la búsqueda");
    return bookRepository.findByIsbn(isbn);
  }

  @Override
  public List<Book> findBooksByCriteria(
      String title, String authors, String genre, String publisher) {
    return bookRepository.findBooksByCriteria(title, authors, genre, publisher);
  }

  @Override
  @Transactional
  public Book save(Book book) {
    this.validateBookInput(book);
    Optional<Book> optionalBook = bookRepository.findByIsbnIncludingDeleted(book.getIsbn());
    if (optionalBook.isPresent()) {
      Book existingBook = optionalBook.get();
      if (!existingBook.isDeleted()) {
        throw new IllegalArgumentException(
            "Ya existe un libro activo con el ISBN: " + book.getIsbn());
      } else {
        existingBook.setDeleted(false);
        this.copyBookData(book, existingBook);
        return bookRepository.save(existingBook);
      }
    }
    return bookRepository.save(book);
  }

  @Override
  @Transactional
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

  @Transactional
  @Override
  public Book update(String isbn, Book book) {
    validateBookNotNull(book);
    return this.findByIsbn(isbn)
        .map(
            existingBook -> {
              this.copyBookData(book, existingBook);
              return bookRepository.save(existingBook);
            })
        .orElseThrow(() -> new ResourceNotFoundException(this.isbnNotFound(isbn)));
  }

  private void validateBookNotNull(Book book) {
    Objects.requireNonNull(book, "El objeto Book no puede ser nulo");
  }

  private void copyBookData(Book source, Book target) {
    target.setTitle(source.getTitle());
    target.setAuthors(source.getAuthors());
    target.setPrice(source.getPrice());
    target.setDescription(source.getDescription());
    target.setPublisher(source.getPublisher());
    target.setGenre(source.getGenre());
    target.setImageUrl(source.getImageUrl());
  }

  private String isbnNotFound(String isbn) {
    return "Libro con ISBN: " + isbn + " no encontrado";
  }

  private void validateBookInput(Book book) {
    /*
    Puede lanzar NullPointerException.
    Puede servir para indicar errores en desarrollo, si no conviene puede tratarse en GlobalExceptionHandler.
    */
    validateBookNotNull(book);
    validateIsbnNotNull(book.getIsbn());
  }

  private void validateIsbnNotNull(String isbn) {
    Objects.requireNonNull(isbn, "El ISBN es obligatorio para operar en el servicio");
  }
}
