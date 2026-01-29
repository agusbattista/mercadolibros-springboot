package io.github.agusbattista.mercadolibros_springboot.controller;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.agusbattista.mercadolibros_springboot.dto.BookRequestDTO;
import io.github.agusbattista.mercadolibros_springboot.dto.BookResponseDTO;
import io.github.agusbattista.mercadolibros_springboot.dto.PagedResponse;
import io.github.agusbattista.mercadolibros_springboot.exception.DuplicateResourceException;
import io.github.agusbattista.mercadolibros_springboot.exception.ResourceNotFoundException;
import io.github.agusbattista.mercadolibros_springboot.service.BookService;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(BookController.class)
class BookControllerTest {

  @Autowired private MockMvc mockMvc;
  @MockitoBean private BookService bookService;
  @Autowired private ObjectMapper objectMapper;
  private BookRequestDTO bookRequest;
  private BookResponseDTO bookResponse;
  private PagedResponse<BookResponseDTO> pagedResponse;

  private static final String BASE_URL = "/api/books";
  private static final String ISBN_PATH_VARIABLE = "/{isbn}";

  @BeforeEach
  void setUp() {
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

    bookResponse =
        new BookResponseDTO(
            "9786073155731",
            "Canción de Hielo y Fuego (Colección)",
            "George R. R. Martin",
            new BigDecimal("33.99"),
            "La saga completa de Canción de Hielo y Fuego, la obra maestra de la fantasía moderna.",
            "Plaza & Janés",
            "Fantasía",
            "https://books.google.com/books/publisher/content?id=krMsDwAAQBAJ&printsec=frontcover&img=1&zoom=4&edge=curl&source=gbs_api");

    pagedResponse =
        new PagedResponse<>(
            List.of(bookResponse), // content
            0, // page
            5, // size
            1, // totalElements
            1, // totalPages
            true, // last
            Map.of("sorted", "NONE"));
  }

  @Test
  void findAll_ShouldReturnPagedResponse() throws Exception {
    when(bookService.findAll(any(Pageable.class))).thenReturn(pagedResponse);

    mockMvc
        .perform(get(BASE_URL).param("page", "0").param("size", "5"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(1))
        .andExpect(jsonPath("$.content[0].isbn").value(bookResponse.isbn()))
        .andExpect(jsonPath("$.page").value(0))
        .andExpect(jsonPath("$.size").value(5))
        .andExpect(jsonPath("$.totalElements").value(1))
        .andExpect(jsonPath("$.last").value(true));

    verify(bookService).findAll(any(Pageable.class));
  }

  @Test
  void findAll_WhenNoBooksExist_ShouldReturnEmptyPagedResponse() throws Exception {
    PagedResponse<BookResponseDTO> emptyPagedResponse =
        new PagedResponse<>(List.of(), 0, 5, 0, 0, true, Map.of("sorted", "NONE"));
    when(bookService.findAll(any(Pageable.class))).thenReturn(emptyPagedResponse);

    mockMvc
        .perform(get(BASE_URL))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(0))
        .andExpect(jsonPath("$.totalElements").value(0))
        .andExpect(jsonPath("$.last").value(true));

    verify(bookService).findAll(any(Pageable.class));
  }

  @Test
  void findAll_WhenUnexpectedErrorOccurs_ShouldReturnInternalServerError() throws Exception {
    when(bookService.findAll(any(Pageable.class))).thenThrow(new RuntimeException("DB crashed"));

    mockMvc
        .perform(get(BASE_URL))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.status").value(500))
        .andExpect(jsonPath("$.message").exists());

    verify(bookService).findAll(any(Pageable.class));
  }

  @Test
  void findByIsbn_WhenIsbnExists_ShouldReturnBook() throws Exception {
    String isbn = bookRequest.isbn();
    String url = BASE_URL + "/" + isbn;
    when(bookService.findByIsbn(isbn)).thenReturn(Optional.of(bookResponse));

    mockMvc
        .perform(get(url))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("isbn").value(isbn));

    verify(bookService).findByIsbn(isbn);
  }

  @Test
  void findByIsbn_WhenIsbnDoesNotExist_ShouldThrowResourceNotFoundException() throws Exception {
    String isbn = "0000000000000";
    String url = BASE_URL + "/" + isbn;
    when(bookService.findByIsbn(isbn)).thenReturn(Optional.empty());

    mockMvc
        .perform(get(url))
        .andExpect(status().isNotFound())
        .andExpect(
            result ->
                assertInstanceOf(ResourceNotFoundException.class, result.getResolvedException()));

    verify(bookService).findByIsbn(isbn);
  }

  @Test
  void findBooksByCriteria_WhenParamsProvided_ShouldReturnFilteredPagedResponse() throws Exception {
    String isbn = bookResponse.isbn();
    String title = "Fuego";
    String genre = "Fantasía";
    String url = BASE_URL + "/search";
    when(bookService.findBooksByCriteria(
            eq(title), isNull(), eq(genre), isNull(), any(Pageable.class)))
        .thenReturn(pagedResponse);

    mockMvc
        .perform(
            get(url)
                .param("title", title)
                .param("genre", genre)
                .param("page", "0")
                .param("size", "5"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(1))
        .andExpect(jsonPath("$.content[0].isbn").value(isbn))
        .andExpect(jsonPath("$.page").value(0))
        .andExpect(jsonPath("$.size").value(5))
        .andExpect(jsonPath("$.totalElements").value(1))
        .andExpect(jsonPath("$.last").value(true));

    verify(bookService)
        .findBooksByCriteria(eq(title), isNull(), eq(genre), isNull(), any(Pageable.class));
  }

  @Test
  void save_WhenValidInput_ShouldReturnCreatedAndBook() throws Exception {
    when(bookService.save(any(BookRequestDTO.class))).thenReturn(bookResponse);
    String requestBody = objectMapper.writeValueAsString(bookRequest);
    String isbn = bookResponse.isbn();

    mockMvc
        .perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON).content(requestBody))
        .andExpect(status().isCreated())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.isbn").value(isbn));

    verify(bookService).save(any(BookRequestDTO.class));
  }

  @Test
  void save_WhenInvalidInput_ShouldReturnBadRequest() throws Exception {
    BookRequestDTO invalidRequest =
        new BookRequestDTO(null, null, null, null, null, null, null, null);
    String requestBody = objectMapper.writeValueAsString(invalidRequest);

    mockMvc
        .perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON).content(requestBody))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors").exists());

    verify(bookService, times(0)).save(any());
  }

  @Test
  void save_WhenBookAlreadyExists_ShouldReturnConflict() throws Exception {
    String requestBody = objectMapper.writeValueAsString(bookRequest);
    doThrow(new DuplicateResourceException("Libro duplicado"))
        .when(bookService)
        .save(any(BookRequestDTO.class));

    mockMvc
        .perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON).content(requestBody))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.status").value(409))
        .andExpect(jsonPath("$.message").exists());

    verify(bookService).save(any(BookRequestDTO.class));
  }

  @Test
  void save_WhenMalformedJson_ShouldReturnBadRequest() throws Exception {
    String invalidJson = "{ isbn: ";

    mockMvc
        .perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON).content(invalidJson))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.message").exists());

    verify(bookService, times(0)).save(any());
  }

  @Test
  void update_WhenValidInput_ShouldReturnOkStatusAndUpdatedBook() throws Exception {
    String url = BASE_URL + ISBN_PATH_VARIABLE;
    String isbn = bookRequest.isbn();
    String requestBody = objectMapper.writeValueAsString(bookRequest);
    when(bookService.update(eq(isbn), any(BookRequestDTO.class))).thenReturn(bookResponse);

    mockMvc
        .perform(put(url, isbn).contentType(MediaType.APPLICATION_JSON).content(requestBody))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.isbn").value(isbn));

    verify(bookService).update(eq(isbn), any(BookRequestDTO.class));
  }

  @Test
  void update_WhenInvalidInput_ShouldReturnBadRequest() throws Exception {
    String url = BASE_URL + ISBN_PATH_VARIABLE;
    String isbn = bookRequest.isbn();
    BookRequestDTO invalidRequest =
        new BookRequestDTO(null, null, null, null, null, null, null, null);
    String requestBody = objectMapper.writeValueAsString(invalidRequest);

    mockMvc
        .perform(put(url, isbn).contentType(MediaType.APPLICATION_JSON).content(requestBody))
        .andExpect(status().isBadRequest());

    verify(bookService, times(0)).update(any(), any());
  }

  @Test
  void delete_WhenIsbnExists_ShouldReturnNoContentStatus() throws Exception {
    String url = BASE_URL + ISBN_PATH_VARIABLE;
    String isbn = bookRequest.isbn();

    mockMvc.perform(delete(url, isbn)).andExpect(status().isNoContent());

    verify(bookService).deleteByIsbn(isbn);
  }

  @Test
  void delete_WhenIsbnDoesNotExist_ShouldReturnNotFound() throws Exception {
    String url = BASE_URL + ISBN_PATH_VARIABLE;
    String isbn = "0000000000000";
    doThrow(new ResourceNotFoundException("Libro no encontrado"))
        .when(bookService)
        .deleteByIsbn(isbn);

    mockMvc.perform(delete(url, isbn)).andExpect(status().isNotFound());

    verify(bookService).deleteByIsbn(isbn);
  }
}
