package io.github.agusbattista.mercadolibros_springboot.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.github.agusbattista.mercadolibros_springboot.dto.BookRequestDTO;
import io.github.agusbattista.mercadolibros_springboot.dto.BookResponseDTO;
import io.github.agusbattista.mercadolibros_springboot.exception.DuplicateResourceException;
import io.github.agusbattista.mercadolibros_springboot.exception.ResourceNotFoundException;
import io.github.agusbattista.mercadolibros_springboot.mapper.BookMapper;
import io.github.agusbattista.mercadolibros_springboot.mapper.BookMapperImpl;
import io.github.agusbattista.mercadolibros_springboot.model.Book;
import io.github.agusbattista.mercadolibros_springboot.repository.BookRepository;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BookServiceImplTest {

  @Mock private BookRepository bookRepository;

  private BookMapper bookMapper = new BookMapperImpl();

  private BookServiceImpl bookService;

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
  void save_WhenBookIsNull_ShouldThrowNullPointerException() {
    assertThatThrownBy(() -> bookService.save(null)).isInstanceOf(NullPointerException.class);
  }

  @Test
  void save_WhenBookDoesNotExist_ShouldReturnSavedBook() {
    when(bookRepository.findByIsbnIncludingDeleted(bookRequest.isbn()))
        .thenReturn(Optional.empty());
    when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> invocation.getArgument(0));

    BookResponseDTO result = bookService.save(bookRequest);

    assertThat(result.isbn()).isEqualTo(bookRequest.isbn());
    verify(bookRepository).findByIsbnIncludingDeleted(bookRequest.isbn());
    verify(bookRepository).save(any(Book.class));
  }

  @Test
  void save_WhenBookExists_ShouldThrowDuplicateResourceException() {
    Book book = bookMapper.toEntity(bookRequest);
    book.setDeleted(false);
    when(bookRepository.findByIsbnIncludingDeleted(bookRequest.isbn()))
        .thenReturn(Optional.of(book));

    assertThatThrownBy(() -> bookService.save(bookRequest))
        .isInstanceOf(DuplicateResourceException.class);
    verify(bookRepository).findByIsbnIncludingDeleted(bookRequest.isbn());
  }

  @Test
  void save_WhenDeletedBookExists_ShouldReturnBook() {
    Book book = bookMapper.toEntity(bookRequest);
    book.setDeleted(true);
    when(bookRepository.findByIsbnIncludingDeleted(bookRequest.isbn()))
        .thenReturn(Optional.of(book));
    when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> invocation.getArgument(0));

    BookResponseDTO result = bookService.save(bookRequest);

    assertThat(result.isbn()).isEqualTo(bookRequest.isbn());
    verify(bookRepository).findByIsbnIncludingDeleted(bookRequest.isbn());
    verify(bookRepository).save(argThat(savedBook -> !savedBook.isDeleted()));
  }

  @Test
  void deleteByIsbn_WhenIsbnIsNull_ShouldThrowNullPointerException() {
    assertThatThrownBy(() -> bookService.deleteByIsbn(null))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void deleteByIsbn_WhenIsbnExists_ShouldDeleteBook() {
    Book book = bookMapper.toEntity(bookRequest);
    when(bookRepository.findByIsbn(bookRequest.isbn())).thenReturn(Optional.of(book));

    bookService.deleteByIsbn(bookRequest.isbn());

    verify(bookRepository).findByIsbn(bookRequest.isbn());
    verify(bookRepository).delete(book);
  }

  @Test
  void deleteByIsbn_WhenIsbnDoesNotExist_ShouldThrowResourceNotFoundException() {
    String isbn = "0000000000000";
    when(bookRepository.findByIsbn(isbn)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> bookService.deleteByIsbn(isbn))
        .isInstanceOf(ResourceNotFoundException.class);

    verify(bookRepository).findByIsbn(isbn);
  }

  @Test
  void update_WhenIsbnIsNull_ShouldThrowNullPointerException() {
    assertThatThrownBy(() -> bookService.update(null, bookRequest))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void update_WhenRequestBookIsNull_ShouldThrowNullPointerException() {
    String isbn = bookRequest.isbn();
    assertThatThrownBy(() -> bookService.update(isbn, null))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void update_WhenBookDoesNotExist_ShouldThrowResourceNotFoundException() {
    String isbn = bookRequest.isbn();
    when(bookRepository.findByIsbn(isbn)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> bookService.update(isbn, bookRequest))
        .isInstanceOf(ResourceNotFoundException.class);
    verify(bookRepository).findByIsbn(isbn);
  }

  @Test
  void update_WhenBookExists_ShouldUpdateBook() {
    String isbn = bookRequest.isbn();
    Book book = bookMapper.toEntity(bookRequest);
    book.setTitle("Old title");
    String newTitle = bookRequest.title();
    when(bookRepository.findByIsbn(isbn)).thenReturn(Optional.of(book));
    when(bookRepository.save(book)).thenReturn(book);

    BookResponseDTO result = bookService.update(isbn, bookRequest);

    assertThat(result.isbn()).isEqualTo(isbn);
    assertThat(result.title()).isEqualTo(newTitle);
    verify(bookRepository).findByIsbn(isbn);
    verify(bookRepository).save(book);
  }
}
