package io.github.agusbattista.mercadolibros_springboot.service;

import io.github.agusbattista.mercadolibros_springboot.dto.BookRequestDTO;
import io.github.agusbattista.mercadolibros_springboot.dto.BookResponseDTO;
import io.github.agusbattista.mercadolibros_springboot.dto.PagedResponse;
import io.github.agusbattista.mercadolibros_springboot.exception.DuplicateResourceException;
import io.github.agusbattista.mercadolibros_springboot.exception.ResourceNotFoundException;
import io.github.agusbattista.mercadolibros_springboot.mapper.BookMapper;
import io.github.agusbattista.mercadolibros_springboot.model.Book;
import io.github.agusbattista.mercadolibros_springboot.repository.BookRepository;
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

  private final BookRepository bookRepository;
  private final BookMapper bookMapper;

  public BookServiceImpl(BookRepository bookRepository, BookMapper bookMapper) {
    this.bookRepository = bookRepository;
    this.bookMapper = bookMapper;
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
    return this.toPagedResponse(
        bookRepository.findBooksByCriteria(title, authors, genre, publisher, pageable));
  }

  @Override
  @Transactional
  public BookResponseDTO create(BookRequestDTO requestBook) {
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
    bookMapper.updateEntityFromRequest(requestBook, existingBook);
    return bookMapper.toResponse(bookRepository.save(existingBook));
  }

  private String uuidNotFound(UUID uuid) {
    return "Libro con UUID: " + uuid + " no encontrado";
  }

  private PagedResponse<BookResponseDTO> toPagedResponse(Page<Book> booksPage) {
    return PagedResponse.from(booksPage.map(bookMapper::toResponse));
  }
}
