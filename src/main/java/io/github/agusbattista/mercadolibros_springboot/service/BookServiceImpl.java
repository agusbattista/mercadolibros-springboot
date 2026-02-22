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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class BookServiceImpl implements BookService {

  private static final String NOT_FOUND_MESSAGE = " no encontrado";

  private final BookRepository bookRepository;
  private final BookMapper bookMapper;
  private final GenreRepository genreRepository;

  public BookServiceImpl(
      BookRepository bookRepository, BookMapper bookMapper, GenreRepository genreRepository) {
    this.bookRepository = bookRepository;
    this.bookMapper = bookMapper;
    this.genreRepository = genreRepository;
  }

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
    Genre genre =
        genreRepository
            .findById(requestBook.genreId())
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "No se puede crear el libro. Género con ID: "
                            + requestBook.genreId()
                            + NOT_FOUND_MESSAGE));
    Optional<Book> optionalBook = bookRepository.findByIsbnIncludingDeleted(requestBook.isbn());
    if (optionalBook.isPresent()) {
      Book existingBook = optionalBook.get();
      if (!existingBook.isDeleted()) {
        throw new DuplicateResourceException(
            "Ya existe un libro activo con el ISBN: " + requestBook.isbn());
      } else {
        existingBook.setDeleted(false);
        existingBook.setGenre(genre);
        bookMapper.updateEntityFromRequest(requestBook, existingBook);
        return bookMapper.toResponse(bookRepository.save(existingBook));
      }
    }
    Book newBook = bookMapper.toEntity(requestBook);
    newBook.setGenre(genre);
    return bookMapper.toResponse(bookRepository.save(newBook));
  }

  @Override
  @Transactional
  public void deleteByUuid(UUID uuid) {
    Objects.requireNonNull(uuid, "El UUID no puede ser nulo para intentar la eliminación");
    Book book =
        bookRepository
            .findByUuid(uuid)
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "No se puede eliminar. " + this.uuidNotFound(uuid)));
    bookRepository.delete(book);
  }

  @Transactional
  @Override
  public BookResponseDTO update(UUID uuid, BookRequestDTO requestBook) {
    Objects.requireNonNull(uuid, "El UUID no puede ser nulo");
    Objects.requireNonNull(
        requestBook, "Los datos del libro que quiere actualizar no pueden ser nulos");
    Genre genre =
        genreRepository
            .findById(requestBook.genreId())
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "No se puede actualizar el libro. Género con ID: "
                            + requestBook.genreId()
                            + NOT_FOUND_MESSAGE));
    Book existingBook =
        bookRepository
            .findByUuid(uuid)
            .orElseThrow(() -> new ResourceNotFoundException(this.uuidNotFound(uuid)));
    if (!existingBook.getIsbn().equals(requestBook.isbn())) {
      Optional<Book> duplicateCandidate =
          bookRepository.findByIsbnIncludingDeleted(requestBook.isbn());
      if (duplicateCandidate.isPresent()) {
        throw new DuplicateResourceException(
            "No se puede actualizar. El ISBN: "
                + requestBook.isbn()
                + " ya pertenece a otro libro");
      }
    }
    existingBook.setGenre(genre);
    bookMapper.updateEntityFromRequest(requestBook, existingBook);
    return bookMapper.toResponse(bookRepository.save(existingBook));
  }

  private String uuidNotFound(UUID uuid) {
    return "Libro con UUID: " + uuid + NOT_FOUND_MESSAGE;
  }

  private PagedResponse<BookResponseDTO> toPagedResponse(Page<Book> booksPage) {
    return PagedResponse.from(booksPage.map(bookMapper::toResponse));
  }
}
