package io.github.agusbattista.mercadolibros_springboot.service;

import io.github.agusbattista.mercadolibros_springboot.dto.BookRequestDTO;
import io.github.agusbattista.mercadolibros_springboot.dto.BookResponseDTO;
import io.github.agusbattista.mercadolibros_springboot.exception.DuplicateResourceException;
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
  public BookResponseDTO save(BookRequestDTO requestBook) {
    Objects.requireNonNull(requestBook, "El libro que quiere guardar no puede ser nulo");
    Optional<Book> optionalBook = bookRepository.findByIsbnIncludingDeleted(requestBook.isbn());
    if (optionalBook.isPresent()) {
      Book existingBook = optionalBook.get();
      if (!existingBook.isDeleted()) {
        throw new DuplicateResourceException(
            "Ya existe un libro activo con el ISBN: " + requestBook.isbn());
      } else {
        existingBook.setDeleted(false);
        bookMapper.updateEntityFromRequest(requestBook, existingBook);
        return bookMapper.toResponse(bookRepository.save(existingBook));
      }
    }
    Book newBook = bookMapper.toEntity(requestBook);
    return bookMapper.toResponse(bookRepository.save(newBook));
  }

  @Override
  @Transactional
  public void deleteByIsbn(String isbn) {
    Objects.requireNonNull(isbn, "El ISBN no puede ser nulo para intentar la eliminación");
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
  public BookResponseDTO update(String isbn, BookRequestDTO requestBook) {
    Objects.requireNonNull(isbn, "El ISBN no puede ser nulo");
    Objects.requireNonNull(
        requestBook, "Los datos del libro que quiere actualizar no pueden ser nulos");
    if (!isbn.equals(requestBook.isbn())) {
      throw new IllegalArgumentException(
          "El ISBN proporcionado no coincide con el del libro que quiere actualizar. No se permite modificar el ISBN");
    }
    Book existingBook =
        bookRepository
            .findByIsbn(isbn)
            .orElseThrow(() -> new ResourceNotFoundException(this.isbnNotFound(isbn)));
    bookMapper.updateEntityFromRequest(requestBook, existingBook);
    return bookMapper.toResponse(bookRepository.save(existingBook));
  }

  private String isbnNotFound(String isbn) {
    return "Libro con ISBN: " + isbn + " no encontrado";
  }
}
