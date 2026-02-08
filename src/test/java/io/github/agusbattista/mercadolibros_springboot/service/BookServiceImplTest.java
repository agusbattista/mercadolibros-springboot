package io.github.agusbattista.mercadolibros_springboot.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.github.agusbattista.mercadolibros_springboot.dto.BookRequestDTO;
import io.github.agusbattista.mercadolibros_springboot.dto.BookResponseDTO;
import io.github.agusbattista.mercadolibros_springboot.dto.PagedResponse;
import io.github.agusbattista.mercadolibros_springboot.exception.DuplicateResourceException;
import io.github.agusbattista.mercadolibros_springboot.exception.ResourceNotFoundException;
import io.github.agusbattista.mercadolibros_springboot.mapper.BookMapper;
import io.github.agusbattista.mercadolibros_springboot.mapper.BookMapperImpl;
import io.github.agusbattista.mercadolibros_springboot.model.Book;
import io.github.agusbattista.mercadolibros_springboot.repository.BookRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class BookServiceImplTest {

  @Mock private BookRepository bookRepository;

  private final BookMapper bookMapper = new BookMapperImpl();

  private BookService bookService;

  private BookRequestDTO bookRequest;

  @BeforeEach
  void setUp() {
    bookService = new BookServiceImpl(bookRepository, bookMapper);

    bookRequest =
        new BookRequestDTO(
            "9786073155731",
            "Canción de Hielo y Fuego (Colección)",
            "George R. R. Martin",
            new BigDecimal("33.99"),
            "La saga completa de Canción de Hielo y Fuego, la obra maestra de la fantasía moderna.",
            "Plaza & Janés",
            "Fantasía",
            "https://books.google.com/books/publisher/content?id=krMsDwAAQBAJ&printsec=frontcover&img=1&zoom=4&edge=curl&source=gbs_api");
  }

  @Test
  void findByIsbn_WhenIsbnIsNull_ShouldThrowNullPointerException() {
    assertThatThrownBy(() -> bookService.findByIsbn(null)).isInstanceOf(NullPointerException.class);
  }

  @Test
  void findByIsbn_WhenIsbnExists_ShouldReturnBook() {
    String isbn = bookRequest.isbn();
    Book book = bookMapper.toEntity(bookRequest);
    when(bookRepository.findByIsbn(isbn)).thenReturn(Optional.of(book));

    Optional<BookResponseDTO> result = bookService.findByIsbn(isbn);

    assertThat(result).isPresent();
    assertThat(result.get().isbn()).isEqualTo(isbn);
    verify(bookRepository).findByIsbn(isbn);
  }

  @Test
  void findByIsbn_WhenIsbnDoesNotExist_ShouldReturnEmptyOptional() {
    String isbn = "0000000000000";
    when(bookRepository.findByIsbn(isbn)).thenReturn(Optional.empty());

    Optional<BookResponseDTO> result = bookService.findByIsbn(isbn);

    assertThat(result).isEmpty();
    verify(bookRepository).findByIsbn(isbn);
  }

  @Test
  void create_WhenBookIsNull_ShouldThrowNullPointerException() {
    assertThatThrownBy(() -> bookService.create(null)).isInstanceOf(NullPointerException.class);
  }

  @Test
  void create_WhenBookDoesNotExist_ShouldReturnSavedBook() {
    when(bookRepository.findByIsbnIncludingDeleted(bookRequest.isbn()))
        .thenReturn(Optional.empty());
    when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> invocation.getArgument(0));

    BookResponseDTO result = bookService.create(bookRequest);

    assertThat(result.isbn()).isEqualTo(bookRequest.isbn());
    verify(bookRepository).findByIsbnIncludingDeleted(bookRequest.isbn());
    verify(bookRepository).save(any(Book.class));
  }

  @Test
  void create_WhenBookExists_ShouldThrowDuplicateResourceException() {
    Book book = bookMapper.toEntity(bookRequest);
    book.setDeleted(false);
    when(bookRepository.findByIsbnIncludingDeleted(bookRequest.isbn()))
        .thenReturn(Optional.of(book));

    assertThatThrownBy(() -> bookService.create(bookRequest))
        .isInstanceOf(DuplicateResourceException.class);
    verify(bookRepository).findByIsbnIncludingDeleted(bookRequest.isbn());
  }

  @Test
  void create_WhenDeletedBookExists_ShouldReturnBook() {
    Book book = bookMapper.toEntity(bookRequest);
    book.setDeleted(true);
    when(bookRepository.findByIsbnIncludingDeleted(bookRequest.isbn()))
        .thenReturn(Optional.of(book));
    when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> invocation.getArgument(0));

    BookResponseDTO result = bookService.create(bookRequest);

    assertThat(result.isbn()).isEqualTo(bookRequest.isbn());
    verify(bookRepository).findByIsbnIncludingDeleted(bookRequest.isbn());
    verify(bookRepository).save(argThat(savedBook -> !savedBook.isDeleted()));
  }

  @Test
  void deleteByUuid_WhenUuidIsNull_ShouldThrowNullPointerException() {
    assertThatThrownBy(() -> bookService.deleteByUuid(null))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void deleteByUuid_WhenUuidExists_ShouldDeleteBook() {
    UUID uuid = UUID.randomUUID();
    Book book = bookMapper.toEntity(bookRequest);
    when(bookRepository.findByUuid(uuid)).thenReturn(Optional.of(book));

    bookService.deleteByUuid(uuid);

    verify(bookRepository).findByUuid(uuid);
    verify(bookRepository).delete(book);
  }

  @Test
  void deleteByUuid_WhenUuidDoesNotExist_ShouldThrowResourceNotFoundException() {
    UUID uuid = UUID.randomUUID();
    when(bookRepository.findByUuid(uuid)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> bookService.deleteByUuid(uuid))
        .isInstanceOf(ResourceNotFoundException.class);

    verify(bookRepository).findByUuid(uuid);
  }

  @Test
  void update_WhenUuidIsNull_ShouldThrowNullPointerException() {
    assertThatThrownBy(() -> bookService.update(null, bookRequest))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void update_WhenRequestBookIsNull_ShouldThrowNullPointerException() {
    UUID uuid = UUID.randomUUID();
    assertThatThrownBy(() -> bookService.update(uuid, null))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void update_WhenBookDoesNotExist_ShouldThrowResourceNotFoundException() {
    UUID uuid = UUID.randomUUID();
    when(bookRepository.findByUuid(uuid)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> bookService.update(uuid, bookRequest))
        .isInstanceOf(ResourceNotFoundException.class);
    verify(bookRepository).findByUuid(uuid);
  }

  @Test
  void update_WhenBookExists_ShouldUpdateBook() {
    UUID uuid = UUID.randomUUID();
    Book book = bookMapper.toEntity(bookRequest);
    book.setUuid(uuid);
    book.setTitle("Old title");
    String newTitle = bookRequest.title();
    when(bookRepository.findByUuid(uuid)).thenReturn(Optional.of(book));
    when(bookRepository.save(book)).thenReturn(book);

    BookResponseDTO result = bookService.update(uuid, bookRequest);

    assertThat(result.uuid()).isEqualTo(uuid);
    assertThat(result.title()).isEqualTo(newTitle);
    verify(bookRepository).findByUuid(uuid);
    verify(bookRepository).save(book);
  }

  @Test
  void update_WhenNewIsbnDoesNotExist_ShouldUpdateBook() {
    UUID uuid = UUID.randomUUID();
    Book book = bookMapper.toEntity(bookRequest);
    book.setUuid(uuid);
    String newIsbn = "9780321247148";
    BookRequestDTO newBookRequest =
        new BookRequestDTO(
            newIsbn,
            bookRequest.title(),
            bookRequest.authors(),
            bookRequest.price(),
            bookRequest.description(),
            bookRequest.publisher(),
            bookRequest.genre(),
            bookRequest.imageUrl());
    when(bookRepository.findByUuid(uuid)).thenReturn(Optional.of(book));
    when(bookRepository.findByIsbnIncludingDeleted(newIsbn)).thenReturn(Optional.empty());
    when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> invocation.getArgument(0));

    BookResponseDTO result = bookService.update(uuid, newBookRequest);

    assertThat(result.uuid()).isEqualTo(uuid);
    assertThat(result.isbn()).isEqualTo(newIsbn);
    verify(bookRepository).findByUuid(uuid);
    verify(bookRepository).findByIsbnIncludingDeleted(newIsbn);
    verify(bookRepository).save(any(Book.class));
  }

  @Test
  void update_WhenNewIsbnExists_ShouldThrowDuplicateResourceException() {
    UUID uuid = UUID.randomUUID();
    Book book = bookMapper.toEntity(bookRequest);
    book.setUuid(uuid);
    book.setDeleted(false);
    String newIsbn = "9780321247148";
    BookRequestDTO newBookRequest =
        new BookRequestDTO(
            newIsbn,
            bookRequest.title(),
            bookRequest.authors(),
            bookRequest.price(),
            bookRequest.description(),
            bookRequest.publisher(),
            bookRequest.genre(),
            bookRequest.imageUrl());
    Book otherBook =
        bookMapper.toEntity(
            new BookRequestDTO(
                newIsbn,
                "Refactoring",
                "Martin Fowler",
                new BigDecimal("57.99"),
                "Una guía definitiva sobre cómo mejorar el diseño del código existente sin cambiar su comportamiento externo.",
                "Addison-Wesley Professional",
                "Tecnología",
                "https://books.google.com/books/publisher/content?id=2H1_DwAAQBAJ&printsec=frontcover&img=1&zoom=4&edge=curl&source=gbs_api"));
    otherBook.setDeleted(true);
    when(bookRepository.findByUuid(uuid)).thenReturn(Optional.of(book));
    when(bookRepository.findByIsbnIncludingDeleted(newIsbn)).thenReturn(Optional.of(otherBook));

    assertThatThrownBy(() -> bookService.update(uuid, newBookRequest))
        .isInstanceOf(DuplicateResourceException.class);

    verify(bookRepository).findByUuid(uuid);
    verify(bookRepository).findByIsbnIncludingDeleted(newIsbn);
    verify(bookRepository, times(0)).save(any(Book.class));
  }

  @Test
  void findlAll_Paged_ShouldReturnPagedResponse() {
    Pageable pageable = PageRequest.of(0, 10);
    Book book = bookMapper.toEntity(bookRequest);
    Page<Book> booksPage = new PageImpl<>(List.of(book), pageable, 1);
    when(bookRepository.findAll(pageable)).thenReturn(booksPage);

    PagedResponse<BookResponseDTO> response = bookService.findAll(pageable);

    assertThat(response).isNotNull();
    assertThat(response.content()).hasSize(1);
    assertThat(response.content().getFirst().isbn()).isEqualTo(bookRequest.isbn());
    assertThat(response.page()).isZero();
    assertThat(response.totalElements()).isEqualTo(1);
    assertThat(response.totalPages()).isEqualTo(1);
    verify(bookRepository).findAll(pageable);
  }

  @Test
  void findBooksByCriteria_Paged_ShouldReturnPagedResponse() {
    String title = "Hielo";
    Pageable pageable = PageRequest.of(0, 5);
    Book book = bookMapper.toEntity(bookRequest);
    Page<Book> booksPage = new PageImpl<>(List.of(book), pageable, 1);
    when(bookRepository.findBooksByCriteria(title, null, null, null, pageable))
        .thenReturn(booksPage);

    PagedResponse<BookResponseDTO> response =
        bookService.findBooksByCriteria(title, null, null, null, pageable);

    assertThat(response).isNotNull();
    assertThat(response.content()).hasSize(1);
    assertThat(response.page()).isZero();
    assertThat(response.totalElements()).isEqualTo(1);
    assertThat(response.totalPages()).isEqualTo(1);
    assertThat(response.content().getFirst().title()).isEqualTo(bookRequest.title());
    verify(bookRepository).findBooksByCriteria(title, null, null, null, pageable);
  }

  @Test
  void findAll_Paged_WhenEmpty_ShouldReturnEmptyPagedResponse() {
    Pageable pageable = PageRequest.of(0, 10);
    Page<Book> emptyPage = Page.empty(pageable);
    when(bookRepository.findAll(pageable)).thenReturn(emptyPage);

    PagedResponse<BookResponseDTO> response = bookService.findAll(pageable);

    assertThat(response.content()).isEmpty();
    assertThat(response.totalElements()).isZero();
    assertThat(response.totalPages()).isZero();
    verify(bookRepository).findAll(pageable);
  }
}
