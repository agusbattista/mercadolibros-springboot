package io.github.agusbattista.mercadolibros_springboot.controller;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.agusbattista.mercadolibros_springboot.dto.GenreRequestDTO;
import io.github.agusbattista.mercadolibros_springboot.dto.GenreResponseDTO;
import io.github.agusbattista.mercadolibros_springboot.dto.PagedResponse;
import io.github.agusbattista.mercadolibros_springboot.exception.DuplicateResourceException;
import io.github.agusbattista.mercadolibros_springboot.exception.ResourceNotFoundException;
import io.github.agusbattista.mercadolibros_springboot.service.GenreService;
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

@WebMvcTest(GenreController.class)
class GenreControllerTest {

  @Autowired private MockMvc mockMvc;
  @MockitoBean private GenreService genreService;
  @Autowired private ObjectMapper objectMapper;
  private GenreRequestDTO genreRequest;
  private GenreResponseDTO genreResponse;
  private PagedResponse<GenreResponseDTO> pagedResponse;

  private static final String BASE_URL = "/api/genres";
  private static final String ID_PATH_VARIABLE = "/{id}";

  @BeforeEach
  void setUp() {
    genreRequest = new GenreRequestDTO("Fantasía");

    genreResponse = new GenreResponseDTO(1L, "FANTASIA", "Fantasía");

    pagedResponse =
        new PagedResponse<>(
            List.of(genreResponse), // content
            0, // page
            5, // size
            1, // totalElements
            1, // totalPages
            true, // last
            Map.of("sorted", "NONE"));
  }

  @Test
  void findAll_ShouldReturnPagedResponse() throws Exception {
    when(genreService.findAll(any(Pageable.class))).thenReturn(pagedResponse);

    mockMvc
        .perform(get(BASE_URL).param("page", "0").param("size", "5"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(1))
        .andExpect(jsonPath("$.content[0].id").value(1))
        .andExpect(jsonPath("$.page").value(0))
        .andExpect(jsonPath("$.size").value(5))
        .andExpect(jsonPath("$.totalElements").value(1))
        .andExpect(jsonPath("$.last").value(true));

    verify(genreService).findAll(any(Pageable.class));
  }

  @Test
  void findAll_WhenNoGenreExist_ShouldReturnEmptyPagedResponse() throws Exception {
    PagedResponse<GenreResponseDTO> emptyPagedResponse =
        new PagedResponse<>(List.of(), 0, 5, 0, 0, true, Map.of("sorted", "NONE"));
    when(genreService.findAll(any(Pageable.class))).thenReturn(emptyPagedResponse);

    mockMvc
        .perform(get(BASE_URL))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(0))
        .andExpect(jsonPath("$.totalElements").value(0))
        .andExpect(jsonPath("$.last").value(true));

    verify(genreService).findAll(any(Pageable.class));
  }

  @Test
  void findAll_WhenUnexpectedErrorOccurs_ShouldReturnInternalServerError() throws Exception {
    when(genreService.findAll(any(Pageable.class)))
        .thenThrow(new RuntimeException("Ocurrió un error interno. Contacte al soporte"));

    mockMvc
        .perform(get(BASE_URL))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.status").value(500))
        .andExpect(jsonPath("$.message").exists());

    verify(genreService).findAll(any(Pageable.class));
  }

  @Test
  void findById_WhenIdExists_ShouldReturnGenre() throws Exception {
    Long id = 1L;
    String url = BASE_URL + ID_PATH_VARIABLE;
    when(genreService.findById(id)).thenReturn(Optional.of(genreResponse));

    mockMvc
        .perform(get(url, id))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.name").value(genreResponse.name()));

    verify(genreService).findById(id);
  }

  @Test
  void findById_WhenIdDoesNotExists_ShouldThrowResourceNotFoundException() throws Exception {
    Long id = 999L;
    String url = BASE_URL + ID_PATH_VARIABLE;
    when(genreService.findById(id)).thenReturn(Optional.empty());

    mockMvc
        .perform(get(url, id))
        .andExpect(status().isNotFound())
        .andExpect(
            result ->
                assertInstanceOf(ResourceNotFoundException.class, result.getResolvedException()))
        .andExpect(jsonPath("$.status").value(404));

    verify(genreService).findById(id);
  }

  @Test
  void findByCode_WhenCodeExists_ShouldReturnGenre() throws Exception {
    String code = "FANTASIA";
    String url = BASE_URL + "/code/" + code;
    when(genreService.findByCode(code)).thenReturn(Optional.of(genreResponse));

    mockMvc
        .perform(get(url))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.code").value(genreResponse.code()));

    verify(genreService).findByCode(code);
  }

  @Test
  void findByCode_WhenCodeDoesNotExist_ShouldThrowResourceNotFoundException() throws Exception {
    String code = "LITERATURA_FANTASTICA";
    String url = BASE_URL + "/code/" + code;
    when(genreService.findByCode(code)).thenReturn(Optional.empty());

    mockMvc
        .perform(get(url))
        .andExpect(status().isNotFound())
        .andExpect(
            result ->
                assertInstanceOf(ResourceNotFoundException.class, result.getResolvedException()));

    verify(genreService).findByCode(code);
  }

  @Test
  void findByName_WhenNameExists_ShouldReturnGenre() throws Exception {
    String name = genreRequest.name();
    String url = BASE_URL + "/search?name=" + name;
    when(genreService.findByName(name)).thenReturn(Optional.of(genreResponse));

    mockMvc
        .perform(get(url))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.name").value(genreResponse.name()));

    verify(genreService).findByName(name);
  }

  @Test
  void findByName_WhenNameDoesNotExist_ShouldThrowResourceNotFoundException() throws Exception {
    String name = "Ciencia Ficción";
    String url = BASE_URL + "/search?name=" + name;
    when(genreService.findByName(name)).thenReturn(Optional.empty());

    mockMvc
        .perform(get(url))
        .andExpect(status().isNotFound())
        .andExpect(
            result ->
                assertInstanceOf(ResourceNotFoundException.class, result.getResolvedException()));

    verify(genreService).findByName(name);
  }

  @Test
  void create_WhenValidInput_ShouldReturnCreatedStatus() throws Exception {
    String requestBody = objectMapper.writeValueAsString(genreRequest);
    when(genreService.create(any(GenreRequestDTO.class))).thenReturn(genreResponse);

    mockMvc
        .perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON).content(requestBody))
        .andExpect(status().isCreated())
        .andExpect(header().exists("Location"))
        .andExpect(jsonPath("$.id").value(genreResponse.id()))
        .andExpect(jsonPath("$.code").value(genreResponse.code()))
        .andExpect(jsonPath("$.name").value(genreResponse.name()));

    verify(genreService).create(any(GenreRequestDTO.class));
  }

  @Test
  void create_WhenInvalidInput_ShouldReturnBadRequest() throws Exception {
    GenreRequestDTO invalidGenreRequest = new GenreRequestDTO("");
    String requestBody = objectMapper.writeValueAsString(invalidGenreRequest);

    mockMvc
        .perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON).content(requestBody))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors").exists());

    verify(genreService, never()).create(any());
  }

  @Test
  void create_WhenGenreAlreadyExists_ShouldReturnConflict() throws Exception {
    String requestBody = objectMapper.writeValueAsString(genreRequest);
    when(genreService.create(any(GenreRequestDTO.class)))
        .thenThrow(new DuplicateResourceException("Género duplicado"));

    mockMvc
        .perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON).content(requestBody))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.status").value(409))
        .andExpect(jsonPath("$.message").exists());

    verify(genreService).create(any(GenreRequestDTO.class));
  }

  @Test
  void create_WhenMalformedJson_ShouldReturnBadRequest() throws Exception {
    String invalidJson = "{ name: ";

    mockMvc
        .perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON).content(invalidJson))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.message").exists());

    verify(genreService, never()).create(any());
  }

  @Test
  void update_WhenValidInput_ShouldReturnOkStatusAndUpdateGenre() throws Exception {
    String url = BASE_URL + ID_PATH_VARIABLE;
    GenreRequestDTO updatedGenreRequest = new GenreRequestDTO("Fantasía Épica");
    GenreResponseDTO updatedGenreResponse =
        new GenreResponseDTO(1L, "FANTASIA_EPICA", "Fantasía Épica");
    String requestBody = objectMapper.writeValueAsString(updatedGenreRequest);
    when(genreService.update(1L, updatedGenreRequest)).thenReturn(updatedGenreResponse);

    mockMvc
        .perform(put(url, 1L).contentType(MediaType.APPLICATION_JSON).content(requestBody))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.code").value(updatedGenreResponse.code()))
        .andExpect(jsonPath("$.name").value(updatedGenreResponse.name()));

    verify(genreService).update(1L, updatedGenreRequest);
  }

  @Test
  void update_WhenInvalidInput_ShouldReturnBadRequest() throws Exception {
    String url = BASE_URL + ID_PATH_VARIABLE;
    GenreRequestDTO invalidGenreRequest = new GenreRequestDTO("");
    String requestBody = objectMapper.writeValueAsString(invalidGenreRequest);

    mockMvc
        .perform(put(url, 1L).contentType(MediaType.APPLICATION_JSON).content(requestBody))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.message").exists());

    verify(genreService, never()).update(any(), any());
  }

  @Test
  void delete_WhenIdExists_ShouldReturnNoContentStatus() throws Exception {
    String url = BASE_URL + ID_PATH_VARIABLE;

    mockMvc.perform(delete(url, 1L)).andExpect(status().isNoContent());

    verify(genreService).deleteById(1L);
  }

  @Test
  void delete_WhenIdDoesNotExist_ShouldReturnNotFound() throws Exception {
    String url = BASE_URL + ID_PATH_VARIABLE;
    doThrow(new ResourceNotFoundException("Género no encontrado"))
        .when(genreService)
        .deleteById(999L);

    mockMvc
        .perform(delete(url, 999L))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(404));

    verify(genreService).deleteById(999L);
  }
}
