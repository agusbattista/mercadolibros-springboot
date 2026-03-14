package io.github.agusbattista.mercadolibros_springboot.service;

import io.github.agusbattista.mercadolibros_springboot.dto.BookRequestDTO;
import io.github.agusbattista.mercadolibros_springboot.dto.BookResponseDTO;
import io.github.agusbattista.mercadolibros_springboot.dto.PagedResponse;
import io.github.agusbattista.mercadolibros_springboot.exception.DuplicateResourceException;
import io.github.agusbattista.mercadolibros_springboot.exception.ResourceNotFoundException;
import io.github.agusbattista.mercadolibros_springboot.mapper.BookMapper;
import io.github.agusbattista.mercadolibros_springboot.model.Book;
import io.github.agusbattista.mercadolibros_springboot.model.Genre;
import io.github.agusbattista.mercadolibros_springboot.repository.BookRepository;
import io.github.agusbattista.mercadolibros_springboot.repository.GenreRepository;
import io.github.agusbattista.mercadolibros_springboot.utils.StringFormatter;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

  private static final String NOT_FOUND_MESSAGE = " no encontrado";
  private final BookRepository bookRepository;
  private final BookMapper bookMapper;
  private final GenreRepository genreRepository;

  @Override
  public PagedResponse<BookResponseDTO> findAll(Pageable pageable) {
    return this.toPagedResponse(bookRepository.findAll(pageable));
  }

  @Override
  public Optional<BookResponseDTO> findByUuid(UUID uuid) {
    Objects.requireNonNull(uuid, "El UUID no puede ser nulo para realizar la búsqueda");
    return bookRepository.findByUuid(uuid).map(bookMapper::toResponse);
  }

  @Override
  public Optional<BookResponseDTO> findByIsbn(String isbn) {
    Objects.requireNonNull(isbn, "El ISBN no puede ser nulo para realizar la búsqueda");
    return bookRepository.findByIsbn(isbn).map(bookMapper::toResponse);
  }

  @Override
  public PagedResponse<BookResponseDTO> findBooksByCriteria(
      String title, String authors, String genre, String publisher, Pageable pageable) {
    String formattedGenre =
        (genre != null && !genre.isBlank()) ? StringFormatter.formatName(genre) : null;
    return this.toPagedResponse(
        bookRepository.findBooksByCriteria(title, authors, formattedGenre, publisher, pageable));
  }

  @Override
  @Transactional
  public BookResponseDTO create(BookRequestDTO requestBook) {
    Objects.requireNonNull(requestBook, "El libro que quiere guardar no puede ser nulo");
    Genre genre = this.getGenreOrThrow(requestBook.genreId(), "No se puede crear el libro");
    Optional<Book> optionalBook = bookRepository.findByIsbnIncludingDeleted(requestBook.isbn());
    if (optionalBook.isPresent()) {
      Book existingBook = optionalBook.get();
      return this.restoreAndUpdateBookOrThrow(requestBook, existingBook, genre);
    }
    Book newBook = bookMapper.toEntity(requestBook);
    newBook.setGenre(genre);
    return bookMapper.toResponse(bookRepository.save(newBook));
  }

  @Transactional
  @Override
  public BookResponseDTO update(UUID uuid, BookRequestDTO requestBook) {
    Objects.requireNonNull(uuid, "El UUID no puede ser nulo");
    Objects.requireNonNull(
        requestBook, "Los datos del libro que quiere actualizar no pueden ser nulos");
    Genre genre = this.getGenreOrThrow(requestBook.genreId(), "No se puede actualizar el libro");
    Book existingBook = this.getBookByUuidOrThrow(uuid, "No se puede actualizar el libro");
    if (!existingBook.getIsbn().equals(requestBook.isbn())) {
      this.checkIsbnIsUniqueOrThrow(requestBook.isbn());
    }
    this.updateBookEntityFromRequest(requestBook, existingBook, genre);
    return bookMapper.toResponse(bookRepository.save(existingBook));
  }

  @Override
  @Transactional
  public void deleteByUuid(UUID uuid) {
    Objects.requireNonNull(uuid, "El UUID no puede ser nulo para intentar la eliminación");
    Book book = this.getBookByUuidOrThrow(uuid, "No se puede eliminar");
    bookRepository.delete(book);
  }

  private Genre getGenreOrThrow(Long genreId, String errorMessagePrefix) {
    return genreRepository
        .findById(genreId)
        .orElseThrow(
            () ->
                new ResourceNotFoundException(
                    errorMessagePrefix + ". Género con ID: " + genreId + NOT_FOUND_MESSAGE));
  }

  private BookResponseDTO restoreAndUpdateBookOrThrow(
      BookRequestDTO requestBook, Book existingBook, Genre genre) {
    if (!existingBook.isDeleted()) {
      throw new DuplicateResourceException(
          "Ya existe un libro activo con el ISBN: " + requestBook.isbn());
    } else {
      existingBook.setDeleted(false);
      this.updateBookEntityFromRequest(requestBook, existingBook, genre);
      return bookMapper.toResponse(bookRepository.save(existingBook));
    }
  }

  private Book getBookByUuidOrThrow(UUID uuid, String errorMessagePrefix) {
    return bookRepository
        .findByUuid(uuid)
        .orElseThrow(
            () ->
                new ResourceNotFoundException(
                    errorMessagePrefix + ". " + this.buildUuidNotFoundMessage(uuid)));
  }

  private void updateBookEntityFromRequest(
      BookRequestDTO requestBook, Book existingBook, Genre genre) {
    existingBook.setGenre(genre);
    bookMapper.updateEntityFromRequest(requestBook, existingBook);
  }

  private void checkIsbnIsUniqueOrThrow(String isbn) {
    Optional<Book> duplicateCandidate = bookRepository.findByIsbnIncludingDeleted(isbn);
    if (duplicateCandidate.isPresent()) {
      throw new DuplicateResourceException(
          "No se puede actualizar. El ISBN: " + isbn + " ya pertenece a otro libro");
    }
  }

  private String buildUuidNotFoundMessage(UUID uuid) {
    return "Libro con UUID: " + uuid + NOT_FOUND_MESSAGE;
  }

  private PagedResponse<BookResponseDTO> toPagedResponse(Page<Book> booksPage) {
    return PagedResponse.from(booksPage.map(bookMapper::toResponse));
  }
}
