package io.github.agusbattista.mercadolibros_springboot.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.agusbattista.mercadolibros_springboot.dto.GenreRequestDTO;
import io.github.agusbattista.mercadolibros_springboot.dto.GenreResponseDTO;
import io.github.agusbattista.mercadolibros_springboot.exception.DuplicateResourceException;
import io.github.agusbattista.mercadolibros_springboot.exception.ResourceInUseException;
import io.github.agusbattista.mercadolibros_springboot.exception.ResourceNotFoundException;
import io.github.agusbattista.mercadolibros_springboot.mapper.GenreMapper;
import io.github.agusbattista.mercadolibros_springboot.mapper.GenreMapperImpl;
import io.github.agusbattista.mercadolibros_springboot.model.Genre;
import io.github.agusbattista.mercadolibros_springboot.repository.BookRepository;
import io.github.agusbattista.mercadolibros_springboot.repository.GenreRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GenreServiceImplTest {

  @Mock private GenreRepository genreRepository;
  @Mock private BookRepository bookRepository;

  private final GenreMapper genreMapper = new GenreMapperImpl();

  private GenreService genreService;

  private Genre genre;
  private Genre genre2;

  @BeforeEach
  void setUp() {
    genreService = new GenreServiceImpl(genreRepository, bookRepository, genreMapper);

    genre = new Genre();
    genre.setId(1L);
    genre.setName("Fantasía");
    genre.setCode("FANTASIA");

    genre2 = new Genre();
    genre2.setId(2L);
    genre2.setName("Ciencia Ficción");
    genre2.setCode("CIENCIA_FICCION");
  }

  @Test
  void findByCode_WhenCodeIsNull_ShouldThrowNullPointerException() {
    assertThatThrownBy(() -> genreService.findById(null)).isInstanceOf(NullPointerException.class);
  }

  @Test
  void findByCode_WhenCodeExists_ShoulReturnGenre() {
    String code = "FANTASIA";
    when(genreRepository.findByCode(code)).thenReturn(Optional.of(genre));

    Optional<GenreResponseDTO> result = genreService.findByCode(code);

    assertThat(result).isPresent();
    assertThat(result.get().code()).isEqualTo(code);
    verify(genreRepository).findByCode(code);
  }

  @Test
  void findByCode_WhenCodeDoesNotExist_ShouldReturnEmptyOptional() {
    String code = "INEXISTENTE";
    when(genreRepository.findByCode(code)).thenReturn(Optional.empty());

    Optional<GenreResponseDTO> result = genreService.findByCode(code);

    assertThat(result).isNotPresent();
    verify(genreRepository).findByCode(code);
  }

  @Test
  void findByCode_WhenCodeRequiresFormatting_ShouldReturnGenre() {
    String code = "  fAntaSíA  ";
    when(genreRepository.findByCode("FANTASIA")).thenReturn(Optional.of(genre));

    Optional<GenreResponseDTO> result = genreService.findByCode(code);

    assertThat(result).isPresent();
    assertThat(result.get().code()).isEqualTo(genre.getCode());
    verify(genreRepository).findByCode("FANTASIA");
  }

  @Test
  void findByName_WhenNameIsNull_ShouldThrowNullPointerException() {
    assertThatThrownBy(() -> genreService.findByName(null))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void findByName_WhenNameExists_ShouldReturnGenre() {
    String name = "Fantasía";
    when(genreRepository.findByCode("FANTASIA")).thenReturn(Optional.of(genre));

    Optional<GenreResponseDTO> result = genreService.findByName(name);

    assertThat(result).isPresent();
    assertThat(result.get().name()).isEqualTo(name);
    verify(genreRepository).findByCode("FANTASIA");
  }

  @Test
  void findByName_WhenNameDoesNotExist_ShouldReturnEmptyOptional() {
    String name = "Inexistente";
    when(genreRepository.findByCode("INEXISTENTE")).thenReturn(Optional.empty());

    Optional<GenreResponseDTO> result = genreService.findByName(name);

    assertThat(result).isNotPresent();
    verify(genreRepository).findByCode("INEXISTENTE");
  }

  @Test
  void findByName_WhenNameRequiresFormatting_ShouldReturnGenre() {
    String name = "  ciencia  ficcion  ";
    when(genreRepository.findByCode("CIENCIA_FICCION")).thenReturn(Optional.of(genre2));

    Optional<GenreResponseDTO> result = genreService.findByName(name);

    assertThat(result).isPresent();
    assertThat(result.get().name()).isEqualTo(genre2.getName());
    verify(genreRepository).findByCode("CIENCIA_FICCION");
  }

  @Test
  void create_WhenGenreIsNull_ShouldThrowNullPointerException() {
    assertThatThrownBy(() -> genreService.create(null)).isInstanceOf(NullPointerException.class);
  }

  @Test
  void create_WhenGenreDoesNotExist_ShouldReturnSavedGenre() {
    GenreRequestDTO genreRequest = new GenreRequestDTO("aventura");
    Genre genre3 = new Genre();
    genre3.setId(3L);
    genre3.setName("Aventura");
    genre3.setCode("AVENTURA");
    when(genreRepository.findByCodeIncludingDeleted("AVENTURA")).thenReturn(Optional.empty());
    when(genreRepository.save(any(Genre.class))).thenReturn(genre3);

    GenreResponseDTO result = genreService.create(genreRequest);

    assertThat(result).isNotNull();
    assertThat(result.id()).isEqualTo(3L);
    assertThat(result.name()).isEqualTo("Aventura");
    assertThat(result.code()).isEqualTo("AVENTURA");
    verify(genreRepository).findByCodeIncludingDeleted("AVENTURA");
    verify(genreRepository).save(any(Genre.class));
  }

  @Test
  void create_WhenDeletedGenreExists_ShouldReturnGenre() {
    genre.setDeleted(true);
    GenreRequestDTO genreRequest = new GenreRequestDTO("Fantasía");
    when(genreRepository.findByCodeIncludingDeleted(genre.getCode()))
        .thenReturn(Optional.of(genre));
    when(genreRepository.save(genre)).thenReturn(genre);

    GenreResponseDTO result = genreService.create(genreRequest);

    assertThat(result).isNotNull();
    assertThat(result.id()).isEqualTo(genre.getId());
    assertThat(result.name()).isEqualTo(genre.getName());
    assertThat(result.code()).isEqualTo(genre.getCode());
    assertThat(genre.isDeleted()).isFalse();
    verify(genreRepository).findByCodeIncludingDeleted(genre.getCode());
    verify(genreRepository).save(genre);
  }

  @Test
  void create_WhenActiveGenreExists_ShouldThrowDuplicateResourceException() {
    genre.setDeleted(false);
    GenreRequestDTO genreRequest = new GenreRequestDTO("Fantasía");
    when(genreRepository.findByCodeIncludingDeleted(genre.getCode()))
        .thenReturn(Optional.of(genre));

    assertThatThrownBy(() -> genreService.create(genreRequest))
        .isInstanceOf(DuplicateResourceException.class);

    verify(genreRepository).findByCodeIncludingDeleted(genre.getCode());
    verify(genreRepository, times(0)).save(genre);
  }

  @Test
  void deleteById_WhenIdIsNull_ShouldThrowNullPointerException() {
    assertThatThrownBy(() -> genreService.deleteById(null))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void deleteById_WhenGenreExistsAndHasNoAssociatedBooks_ShouldDeleteGenre() {
    when(genreRepository.findById(genre.getId())).thenReturn(Optional.of(genre));
    when(bookRepository.countByGenreIdIncludingDeleted(genre.getId())).thenReturn(0L);

    genreService.deleteById(genre.getId());

    verify(genreRepository).findById(genre.getId());
    verify(bookRepository).countByGenreIdIncludingDeleted(genre.getId());
    verify(genreRepository).delete(genre);
  }

  @Test
  void deleteById_WhenGenreExistsAndHasAssociatedBooks_ShouldThrowResourceInUseException() {
    Long genreId = genre.getId();
    when(genreRepository.findById(genreId)).thenReturn(Optional.of(genre));
    when(bookRepository.countByGenreIdIncludingDeleted(genreId)).thenReturn(1L);

    assertThatThrownBy(() -> genreService.deleteById(genreId))
        .isInstanceOf(ResourceInUseException.class);

    verify(genreRepository).findById(genreId);
    verify(bookRepository).countByGenreIdIncludingDeleted(genreId);
    verify(genreRepository, times(0)).delete(genre);
  }

  @Test
  void deleteById_WhenGenreDoesNotExist_ShouldThrowResourceNotFoundException() {
    Long inexistentId = 999L;
    when(genreRepository.findById(inexistentId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> genreService.deleteById(inexistentId))
        .isInstanceOf(ResourceNotFoundException.class);

    verify(genreRepository).findById(inexistentId);
    verify(genreRepository, times(0)).delete(any(Genre.class));
  }

  @Test
  void update_WhenIdIsNull_ShouldThrowNullPointerException() {
    GenreRequestDTO genreRequest = new GenreRequestDTO("Fantasía");
    assertThatThrownBy(() -> genreService.update(null, genreRequest))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void update_WhenGenreIsNull_ShouldThrowNullPointerException() {
    Long id = genre.getId();
    assertThatThrownBy(() -> genreService.update(id, null))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void update_WhenGenreExists_ShouldReturnUpdatedGenre() {
    Long id = genre.getId();
    // Se utiliza el mismo nombre pero sin tilde para comprobar que se puede actualizar exitosamente
    GenreRequestDTO genreRequest = new GenreRequestDTO("Fantasia");
    Genre updatedGenre = new Genre();
    updatedGenre.setId(id);
    updatedGenre.setName(genreRequest.name());
    updatedGenre.setCode(genre.getCode());
    when(genreRepository.findById(id)).thenReturn(Optional.of(genre));
    when(genreRepository.findByCodeIncludingDeleted(genre.getCode()))
        .thenReturn(Optional.of(genre));
    when(genreRepository.save(genre)).thenReturn(updatedGenre);

    GenreResponseDTO result = genreService.update(id, genreRequest);

    assertThat(result).isNotNull();
    assertThat(result.id()).isEqualTo(id);
    assertThat(result.name()).isEqualTo(genreRequest.name());
    assertThat(result.code()).isEqualTo(genre.getCode());
    verify(genreRepository).findById(id);
    verify(genreRepository).findByCodeIncludingDeleted(genre.getCode());
    verify(genreRepository).save(genre);
  }

  @Test
  void update_WhenGenreDoesNotExist_ShouldThrowResourceNotFoundException() {
    Long inexistentId = 999L;
    GenreRequestDTO genreRequest = new GenreRequestDTO("Fantasía");
    when(genreRepository.findById(inexistentId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> genreService.update(inexistentId, genreRequest))
        .isInstanceOf(ResourceNotFoundException.class);

    verify(genreRepository).findById(inexistentId);
    verify(genreRepository, times(0)).save(any(Genre.class));
  }

  @Test
  void update_WhenNameExists_ShouldThrowDuplicateResourceException() {
    Long id = genre.getId();
    GenreRequestDTO genreRequest = new GenreRequestDTO("Ciencia Ficción");
    when(genreRepository.findById(id)).thenReturn(Optional.of(genre));
    when(genreRepository.findByCodeIncludingDeleted("CIENCIA_FICCION"))
        .thenReturn(Optional.of(genre2));

    assertThatThrownBy(() -> genreService.update(id, genreRequest))
        .isInstanceOf(DuplicateResourceException.class);

    verify(genreRepository).findById(id);
    verify(genreRepository).findByCodeIncludingDeleted("CIENCIA_FICCION");
    verify(genreRepository, times(0)).save(any(Genre.class));
  }
}
