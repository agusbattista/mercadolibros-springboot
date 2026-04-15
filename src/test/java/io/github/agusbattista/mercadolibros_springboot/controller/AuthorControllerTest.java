package io.github.agusbattista.mercadolibros_springboot.controller;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.agusbattista.mercadolibros_springboot.dto.AuthorRequestDTO;
import io.github.agusbattista.mercadolibros_springboot.dto.AuthorResponseDTO;
import io.github.agusbattista.mercadolibros_springboot.dto.PagedResponse;
import io.github.agusbattista.mercadolibros_springboot.exception.ResourceNotFoundException;
import io.github.agusbattista.mercadolibros_springboot.service.AuthorService;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthorController.class)
class AuthorControllerTest {

  @Autowired private MockMvc mockMvc;
  @MockitoBean private AuthorService authorService;
  @Autowired private ObjectMapper objectMapper;
  private AuthorRequestDTO authorRequest;
  private AuthorResponseDTO authorResponse;
  private PagedResponse<AuthorResponseDTO> pagedResponse;
  private UUID uuid;

  private static final String BASE_URL = "/api/authors";
  private static final String UUID_PATH_VARIABLE = "/{uuid}";

  @BeforeEach
  void setUp() {
    uuid = UUID.randomUUID();
    authorRequest =
        new AuthorRequestDTO("Gabriel García Márquez", "Premio Nobel de Literatura 1982.");
    authorResponse =
        new AuthorResponseDTO(uuid, "Gabriel García Márquez", "Premio Nobel de Literatura 1982.");

    pagedResponse =
        new PagedResponse<>(List.of(authorResponse), 0, 5, 1, 1, true, Map.of("sorted", "NONE"));
  }

  @Test
  void findAll_ShouldReturnPagedResponse() throws Exception {
    when(authorService.findAll(any(Pageable.class))).thenReturn(pagedResponse);

    mockMvc
        .perform(get(BASE_URL).param("page", "0").param("size", "5"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(1))
        .andExpect(jsonPath("$.content[0].uuid").value(uuid.toString()))
        .andExpect(jsonPath("$.page").value(0))
        .andExpect(jsonPath("$.size").value(5))
        .andExpect(jsonPath("$.totalElements").value(1))
        .andExpect(jsonPath("$.last").value(true));

    verify(authorService).findAll(any(Pageable.class));
  }

  @Test
  void findByUuid_WhenUuidExists_ShouldReturnAuthor() throws Exception {
    String url = BASE_URL + UUID_PATH_VARIABLE;
    when(authorService.findByUuid(uuid)).thenReturn(Optional.of(authorResponse));

    mockMvc
        .perform(get(url, uuid.toString()))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.uuid").value(uuid.toString()))
        .andExpect(jsonPath("$.fullName").value(authorResponse.fullName()));

    verify(authorService).findByUuid(uuid);
  }

  @Test
  void findByUuid_WhenUuidDoesNotExists_ShouldThrowResourceNotFoundException() throws Exception {
    UUID inexistentUuid = UUID.randomUUID();
    String url = BASE_URL + UUID_PATH_VARIABLE;
    when(authorService.findByUuid(inexistentUuid)).thenReturn(Optional.empty());

    mockMvc
        .perform(get(url, inexistentUuid.toString()))
        .andExpect(status().isNotFound())
        .andExpect(
            result ->
                assertInstanceOf(ResourceNotFoundException.class, result.getResolvedException()))
        .andExpect(jsonPath("$.status").value(404));

    verify(authorService).findByUuid(inexistentUuid);
  }

  @Test
  void findByName_WhenNameExists_ShouldReturnPagedResponse() throws Exception {
    String name = "García";
    String url = BASE_URL + "/search?name=" + name;
    when(authorService.findByName(any(String.class), any(Pageable.class)))
        .thenReturn(pagedResponse);

    mockMvc
        .perform(get(url))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.content[0].uuid").value(uuid.toString()))
        .andExpect(jsonPath("$.content[0].fullName").value(authorResponse.fullName()));

    verify(authorService).findByName(any(String.class), any(Pageable.class));
  }

  @Test
  void create_WhenValidInput_ShouldReturnCreatedStatus() throws Exception {
    String requestBody = objectMapper.writeValueAsString(authorRequest);
    when(authorService.create(any(AuthorRequestDTO.class))).thenReturn(authorResponse);

    mockMvc
        .perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON).content(requestBody))
        .andExpect(status().isCreated())
        .andExpect(header().exists("Location"))
        .andExpect(jsonPath("$.uuid").value(uuid.toString()))
        .andExpect(jsonPath("$.fullName").value(authorResponse.fullName()));

    verify(authorService).create(any(AuthorRequestDTO.class));
  }

  @Test
  void create_WhenInvalidInput_ShouldReturnBadRequest() throws Exception {
    AuthorRequestDTO invalidRequest = new AuthorRequestDTO("", "muy corta");
    String requestBody = objectMapper.writeValueAsString(invalidRequest);

    mockMvc
        .perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON).content(requestBody))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors").exists());

    verify(authorService, never()).create(any());
  }

  @Test
  void update_WhenValidInput_ShouldReturnOkStatusAndUpdateAuthor() throws Exception {
    String url = BASE_URL + UUID_PATH_VARIABLE;
    AuthorRequestDTO updatedRequest =
        new AuthorRequestDTO("Gabo Márquez", "Premio Nobel de Literatura 1982.");
    AuthorResponseDTO updatedResponse =
        new AuthorResponseDTO(uuid, "Gabo Márquez", "Premio Nobel de Literatura 1982.");
    String requestBody = objectMapper.writeValueAsString(updatedRequest);
    when(authorService.update(uuid, updatedRequest)).thenReturn(updatedResponse);

    mockMvc
        .perform(
            put(url, uuid.toString()).contentType(MediaType.APPLICATION_JSON).content(requestBody))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.uuid").value(uuid.toString()))
        .andExpect(jsonPath("$.fullName").value(updatedResponse.fullName()));

    verify(authorService).update(uuid, updatedRequest);
  }

  @Test
  void update_WhenInvalidInput_ShouldReturnBadRequest() throws Exception {
    String url = BASE_URL + UUID_PATH_VARIABLE;
    AuthorRequestDTO invalidRequest = new AuthorRequestDTO("", "muy corta");
    String requestBody = objectMapper.writeValueAsString(invalidRequest);

    mockMvc
        .perform(
            put(url, uuid.toString()).contentType(MediaType.APPLICATION_JSON).content(requestBody))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.message").exists());

    verify(authorService, never()).update(any(), any());
  }

  @Test
  void delete_WhenUuidExists_ShouldReturnNoContentStatus() throws Exception {
    String url = BASE_URL + UUID_PATH_VARIABLE;

    mockMvc.perform(delete(url, uuid.toString())).andExpect(status().isNoContent());

    verify(authorService).deleteByUuid(uuid);
  }

  @Test
  void delete_WhenUuidDoesNotExist_ShouldReturnNotFound() throws Exception {
    String url = BASE_URL + UUID_PATH_VARIABLE;
    UUID inexistentUuid = UUID.randomUUID();
    doThrow(new ResourceNotFoundException("Autor no encontrado"))
        .when(authorService)
        .deleteByUuid(inexistentUuid);

    mockMvc
        .perform(delete(url, inexistentUuid.toString()))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(404));

    verify(authorService).deleteByUuid(inexistentUuid);
  }

  @Test
  void restore_WhenUuidExists_ShouldReturnOkStatusAndRestoredAuthor() throws Exception {
    String url = BASE_URL + UUID_PATH_VARIABLE + "/restore";
    when(authorService.restore(uuid)).thenReturn(authorResponse);

    mockMvc
        .perform(patch(url, uuid.toString()))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.uuid").value(uuid.toString()))
        .andExpect(jsonPath("$.fullName").value(authorResponse.fullName()));

    verify(authorService).restore(uuid);
  }

  @Test
  void restore_WhenUuidDoesNotExist_ShouldReturnNotFound() throws Exception {
    String url = BASE_URL + UUID_PATH_VARIABLE + "/restore";
    UUID inexistentUuid = UUID.randomUUID();
    when(authorService.restore(inexistentUuid))
        .thenThrow(new ResourceNotFoundException("Autor no encontrado"));

    mockMvc
        .perform(patch(url, inexistentUuid.toString()))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(404));

    verify(authorService).restore(inexistentUuid);
  }
}
