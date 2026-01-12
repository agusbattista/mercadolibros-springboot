package io.github.agusbattista.mercadolibros_springboot.service;

import io.github.agusbattista.mercadolibros_springboot.dto.BookRequestDTO;
import io.github.agusbattista.mercadolibros_springboot.dto.BookResponseDTO;
import io.github.agusbattista.mercadolibros_springboot.exception.ResourceNotFoundException;
import io.github.agusbattista.mercadolibros_springboot.mapper.BookMapper;
import io.github.agusbattista.mercadolibros_springboot.model.Book;
import io.github.agusbattista.mercadolibros_springboot.repository.BookRepository;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class BookServiceImpl implements BookService {

  private final BookRepository bookRepository;
  private final BookMapper bookMapper;

  public BookServiceImpl(BookRepository bookRepository, BookMapper bookMapper) {
    this.bookRepository = bookRepository;
    this.bookMapper = bookMapper;
  }

  @Override
  public List<BookResponseDTO> findAll() {
    return bookMapper.toResponseList(bookRepository.findAll());
  }

  @Override
  public Optional<BookResponseDTO> findByIsbn(String isbn) {
    Objects.requireNonNull(isbn, "El ISBN no puede ser nulo para realizar la búsqueda");
    return bookRepository.findByIsbn(isbn).map(bookMapper::toResponse);
  }

  @Override
  public List<BookResponseDTO> findBooksByCriteria(
      String title, String authors, String genre, String publisher) {
    return bookMapper.toResponseList(
        bookRepository.findBooksByCriteria(title, authors, genre, publisher));
  }

  @Override
  @Transactional
  public BookResponseDTO save(BookRequestDTO book) {
    Book requestBook = bookMapper.toEntity(book);
    this.validateBookInput(requestBook);
    Optional<Book> optionalBook = bookRepository.findByIsbnIncludingDeleted(requestBook.getIsbn());
    if (optionalBook.isPresent()) {
      Book existingBook = optionalBook.get();
      if (!existingBook.isDeleted()) {
        throw new IllegalArgumentException(
            "Ya existe un libro activo con el ISBN: " + requestBook.getIsbn());
      } else {
        existingBook.setDeleted(false);
        this.copyBookData(requestBook, existingBook);
        return bookMapper.toResponse(bookRepository.save(existingBook));
      }
    }
    return bookMapper.toResponse(bookRepository.save(requestBook));
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
  public BookResponseDTO update(String isbn, BookRequestDTO book) {
    Book requestBook = bookMapper.toEntity(book);
    validateBookNotNull(requestBook);
    return bookRepository
        .findByIsbn(isbn)
        .map(
            existingBook -> {
              this.copyBookData(requestBook, existingBook);
              return bookMapper.toResponse(bookRepository.save(existingBook));
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
