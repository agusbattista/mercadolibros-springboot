package io.github.agusbattista.mercadolibros_springboot.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.agusbattista.mercadolibros_springboot.dto.AuthorRequestDTO;
import io.github.agusbattista.mercadolibros_springboot.dto.AuthorResponseDTO;
import io.github.agusbattista.mercadolibros_springboot.dto.PagedResponse;
import io.github.agusbattista.mercadolibros_springboot.exception.ResourceNotFoundException;
import io.github.agusbattista.mercadolibros_springboot.mapper.AuthorMapper;
import io.github.agusbattista.mercadolibros_springboot.mapper.AuthorMapperImpl;
import io.github.agusbattista.mercadolibros_springboot.model.Author;
import io.github.agusbattista.mercadolibros_springboot.repository.AuthorRepository;
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
class AuthorServiceImplTest {

  @Mock private AuthorRepository authorRepository;

  private final AuthorMapper authorMapper = new AuthorMapperImpl();

  private AuthorService authorService;

  private Author author;
  private Author author2;
  private UUID uuid1;
  private UUID uuid2;

  @BeforeEach
  void setUp() {
    authorService = new AuthorServiceImpl(authorRepository, authorMapper);

    uuid1 = UUID.randomUUID();
    author = new Author();
    author.setId(1L);
    author.setUuid(uuid1);
    author.setFullName("Gabriel García Márquez");
    author.setBiography("Premio Nobel de Literatura en 1982.");

    uuid2 = UUID.randomUUID();
    author2 = new Author();
    author2.setId(2L);
    author2.setUuid(uuid2);
    author2.setFullName("Julio Cortázar");
    author2.setBiography("Escritor argentino, autor de Rayuela.");
  }

  @Test
  void findAll_ShouldReturnPagedResponse() {
    Pageable pageable = PageRequest.of(0, 10);
    Page<Author> authorPage = new PageImpl<>(List.of(author, author2), pageable, 2);
    when(authorRepository.findAll(pageable)).thenReturn(authorPage);

    PagedResponse<AuthorResponseDTO> result = authorService.findAll(pageable);

    assertThat(result).isNotNull();
    assertThat(result.content()).hasSize(2);
    assertThat(result.content().getFirst().uuid()).isEqualTo(uuid1);
    assertThat(result.totalElements()).isEqualTo(2);
    assertThat(result.totalPages()).isEqualTo(1);
    verify(authorRepository).findAll(pageable);
  }

  @Test
  void findAll_Paged_WhenEmpty_ShouldReturnEmptyPagedResponse() {
    Pageable pageable = PageRequest.of(0, 10);
    Page<Author> emptyPage = Page.empty(pageable);
    when(authorRepository.findAll(pageable)).thenReturn(emptyPage);

    PagedResponse<AuthorResponseDTO> result = authorService.findAll(pageable);

    assertThat(result.content()).isEmpty();
    assertThat(result.totalElements()).isZero();
    assertThat(result.totalPages()).isZero();
    verify(authorRepository).findAll(pageable);
  }

  @Test
  void findByUuid_WhenUuidIsNull_ShouldThrowNullPointerException() {
    assertThatThrownBy(() -> authorService.findByUuid(null))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void findByUuid_WhenUuidExists_ShouldReturnAuthor() {
    when(authorRepository.findByUuid(uuid1)).thenReturn(Optional.of(author));

    Optional<AuthorResponseDTO> result = authorService.findByUuid(uuid1);

    assertThat(result).isPresent();
    assertThat(result.get().uuid()).isEqualTo(uuid1);
    verify(authorRepository).findByUuid(uuid1);
  }

  @Test
  void findByUuid_WhenUuidDoesNotExist_ShouldReturnEmptyOptional() {
    UUID inexistentUuid = UUID.randomUUID();
    when(authorRepository.findByUuid(inexistentUuid)).thenReturn(Optional.empty());

    Optional<AuthorResponseDTO> result = authorService.findByUuid(inexistentUuid);

    assertThat(result).isNotPresent();
    verify(authorRepository).findByUuid(inexistentUuid);
  }

  @Test
  void findByName_WhenNameIsNull_ShouldThrowNullPointerException() {
    assertThatThrownBy(() -> authorService.findByName(null, PageRequest.of(0, 10)))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void findByName_WhenNameExists_ShouldReturnPagedResponse() {
    String name = " GarcíA  márquez ";
    Pageable pageable = PageRequest.of(0, 10);
    Page<Author> authorPage = new PageImpl<>(List.of(author), pageable, 1);

    when(authorRepository.findByFullNameContainingIgnoreCase("García Márquez", pageable))
        .thenReturn(authorPage);

    PagedResponse<AuthorResponseDTO> result = authorService.findByName(name, pageable);

    assertThat(result.content()).hasSize(1);
    assertThat(result.content().getFirst().uuid()).isEqualTo(uuid1);
    verify(authorRepository).findByFullNameContainingIgnoreCase("García Márquez", pageable);
  }

  @Test
  void findByName_WhenNameIsEmpty_ShouldReturnPagedResponse() {
    String name = "  ";
    Pageable pageable = PageRequest.of(0, 10);
    Page<Author> authorPage = new PageImpl<>(List.of(author, author2), pageable, 2);

    when(authorRepository.findAll(pageable)).thenReturn(authorPage);

    PagedResponse<AuthorResponseDTO> result = authorService.findByName(name, pageable);

    assertThat(result.content()).hasSize(2);
    verify(authorRepository).findAll(pageable);
    verify(authorRepository, never()).findByFullNameContainingIgnoreCase(any(), any());
  }

  @Test
  void create_WhenAuthorIsNull_ShouldThrowNullPointerException() {
    assertThatThrownBy(() -> authorService.create(null)).isInstanceOf(NullPointerException.class);
  }

  @Test
  void create_WhenValidAuthor_ShouldReturnSavedAuthor() {
    AuthorRequestDTO request = new AuthorRequestDTO("jorge luis borges", "Escritor argentino.");
    Author savedAuthor = new Author();
    savedAuthor.setId(3L);
    savedAuthor.setUuid(UUID.randomUUID());
    savedAuthor.setFullName("Jorge Luis Borges");
    savedAuthor.setBiography("Escritor argentino.");

    when(authorRepository.save(any(Author.class))).thenReturn(savedAuthor);

    AuthorResponseDTO result = authorService.create(request);

    assertThat(result).isNotNull();
    assertThat(result.fullName()).isEqualTo("Jorge Luis Borges");
    verify(authorRepository).save(any(Author.class));
  }

  @Test
  void update_WhenUuidIsNull_ShouldThrowNullPointerException() {
    AuthorRequestDTO request = new AuthorRequestDTO("Jorge Luis Borges", "Biografía...");
    assertThatThrownBy(() -> authorService.update(null, request))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void update_WhenAuthorIsNull_ShouldThrowNullPointerException() {
    assertThatThrownBy(() -> authorService.update(UUID.randomUUID(), null))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void update_WhenAuthorExists_ShouldReturnUpdatedAuthor() {
    AuthorRequestDTO request = new AuthorRequestDTO("Gabriel Garcia Marquez", "Nueva biografía");
    Author updatedAuthor = new Author();
    updatedAuthor.setId(author.getId());
    updatedAuthor.setUuid(author.getUuid());
    updatedAuthor.setFullName("Gabriel Garcia Marquez");
    updatedAuthor.setBiography("Nueva biografía");

    when(authorRepository.findByUuid(uuid1)).thenReturn(Optional.of(author));
    when(authorRepository.save(any(Author.class))).thenReturn(updatedAuthor);

    AuthorResponseDTO result = authorService.update(uuid1, request);

    assertThat(result).isNotNull();
    assertThat(result.uuid()).isEqualTo(uuid1);
    assertThat(result.fullName()).isEqualTo("Gabriel Garcia Marquez");
    assertThat(result.biography()).isEqualTo("Nueva biografía");
    verify(authorRepository).findByUuid(uuid1);
    verify(authorRepository).save(any(Author.class));
  }

  @Test
  void update_WhenAuthorDoesNotExist_ShouldThrowResourceNotFoundException() {
    UUID inexistentUuid = UUID.randomUUID();
    AuthorRequestDTO request = new AuthorRequestDTO("Gabriel García Márquez", "Biografía...");
    when(authorRepository.findByUuid(inexistentUuid)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> authorService.update(inexistentUuid, request))
        .isInstanceOf(ResourceNotFoundException.class);

    verify(authorRepository).findByUuid(inexistentUuid);
    verify(authorRepository, never()).save(any(Author.class));
  }

  @Test
  void deleteByUuid_WhenUuidIsNull_ShouldThrowNullPointerException() {
    assertThatThrownBy(() -> authorService.deleteByUuid(null))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void deleteByUuid_WhenAuthorExists_ShouldDeleteAuthor() {
    when(authorRepository.findByUuid(uuid1)).thenReturn(Optional.of(author));

    authorService.deleteByUuid(uuid1);

    verify(authorRepository).findByUuid(uuid1);
    verify(authorRepository).delete(author);
  }

  @Test
  void deleteByUuid_WhenAuthorDoesNotExist_ShouldThrowResourceNotFoundException() {
    UUID inexistentUuid = UUID.randomUUID();
    when(authorRepository.findByUuid(inexistentUuid)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> authorService.deleteByUuid(inexistentUuid))
        .isInstanceOf(ResourceNotFoundException.class);

    verify(authorRepository).findByUuid(inexistentUuid);
    verify(authorRepository, never()).delete(any(Author.class));
  }

  @Test
  void restore_WhenUuidIsNull_ShouldThrowNullPointerException() {
    assertThatThrownBy(() -> authorService.restore(null)).isInstanceOf(NullPointerException.class);
  }

  @Test
  void restore_WhenAuthorDoesNotExist_ShouldThrowResourceNotFoundException() {
    UUID inexistentUuid = UUID.randomUUID();
    when(authorRepository.findByUuidIncludingDeleted(inexistentUuid.toString()))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> authorService.restore(inexistentUuid))
        .isInstanceOf(ResourceNotFoundException.class);

    verify(authorRepository).findByUuidIncludingDeleted(inexistentUuid.toString());
    verify(authorRepository, never()).save(any(Author.class));
  }

  @Test
  void restore_WhenAuthorIsDeleted_ShouldRestoreAndReturnAuthor() {
    author.setDeleted(true);
    when(authorRepository.findByUuidIncludingDeleted(uuid1.toString()))
        .thenReturn(Optional.of(author));
    when(authorRepository.save(author)).thenReturn(author);

    AuthorResponseDTO result = authorService.restore(uuid1);

    assertThat(result).isNotNull();
    assertThat(result.uuid()).isEqualTo(uuid1);
    assertThat(author.isDeleted()).isFalse();
    verify(authorRepository).findByUuidIncludingDeleted(uuid1.toString());
    verify(authorRepository).save(author);
  }

  @Test
  void restore_WhenAuthorIsNotDeleted_ShouldReturnAuthorWithoutSaving() {
    when(authorRepository.findByUuidIncludingDeleted(uuid1.toString()))
        .thenReturn(Optional.of(author));

    AuthorResponseDTO result = authorService.restore(uuid1);

    assertThat(result).isNotNull();
    assertThat(result.uuid()).isEqualTo(uuid1);
    assertThat(author.isDeleted()).isFalse();
    verify(authorRepository).findByUuidIncludingDeleted(uuid1.toString());
    verify(authorRepository, never()).save(any(Author.class));
  }
}
