package io.github.agusbattista.mercadolibros_springboot.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.agusbattista.mercadolibros_springboot.model.Book;
import io.github.agusbattista.mercadolibros_springboot.model.Genre;
import io.github.agusbattista.mercadolibros_springboot.repository.BookRepository;
import io.github.agusbattista.mercadolibros_springboot.repository.GenreRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BookDataLoaderTest {

  @Mock private BookRepository bookRepository;
  @Mock private GenreRepository genreRepository;
  private final ObjectMapper objectMapper = new ObjectMapper();
  private BookDataLoader bookDataLoader;

  @BeforeEach
  void setUp() {
    bookDataLoader = new BookDataLoader(bookRepository, genreRepository, objectMapper);
  }

  @Test
  void run_WhenDatabaseIsEmpty_ShouldLoadBooks() throws Exception {
    when(bookRepository.countAllIncludingDeleted()).thenReturn(0L);
    when(genreRepository.save(any(Genre.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    bookDataLoader.run();

    @SuppressWarnings("unchecked")
    ArgumentCaptor<List<Book>> listCaptor = ArgumentCaptor.forClass(List.class);
    verify(bookRepository).saveAll(listCaptor.capture());
    List<Book> savedBooks = listCaptor.getValue();

    assertThat(savedBooks).isNotEmpty();
    assertThat(savedBooks.getFirst().getTitle()).isNotBlank();
    assertThat(savedBooks.getFirst().getGenre()).isNotNull();
    verify(bookRepository).countAllIncludingDeleted();
  }

  @Test
  void run_WhenDatabaseHasData_ShouldNotLoadBooks() throws Exception {
    when(bookRepository.countAllIncludingDeleted()).thenReturn(1L);

    bookDataLoader.run();

    verify(bookRepository).countAllIncludingDeleted();
    verify(bookRepository, never()).saveAll(anyList());
    verifyNoInteractions(genreRepository);
  }
}
