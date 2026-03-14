package io.github.agusbattista.mercadolibros_springboot.service;

import io.github.agusbattista.mercadolibros_springboot.dto.GenreRequestDTO;
import io.github.agusbattista.mercadolibros_springboot.dto.GenreResponseDTO;
import io.github.agusbattista.mercadolibros_springboot.dto.PagedResponse;
import io.github.agusbattista.mercadolibros_springboot.exception.DuplicateResourceException;
import io.github.agusbattista.mercadolibros_springboot.exception.ResourceInUseException;
import io.github.agusbattista.mercadolibros_springboot.exception.ResourceNotFoundException;
import io.github.agusbattista.mercadolibros_springboot.mapper.GenreMapper;
import io.github.agusbattista.mercadolibros_springboot.model.Genre;
import io.github.agusbattista.mercadolibros_springboot.repository.BookRepository;
import io.github.agusbattista.mercadolibros_springboot.repository.GenreRepository;
import io.github.agusbattista.mercadolibros_springboot.utils.StringFormatter;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GenreServiceImpl implements GenreService {

  private final GenreRepository genreRepository;
  private final BookRepository bookRepository;
  private final GenreMapper genreMapper;

  @Override
  public PagedResponse<GenreResponseDTO> findAll(Pageable pageable) {
    return this.toPagedResponse(genreRepository.findAll(pageable));
  }

  @Override
  public Optional<GenreResponseDTO> findById(Long id) {
    Objects.requireNonNull(id, "El ID no puede ser nulo para realizar la búsqueda");
    return genreRepository.findById(id).map(genreMapper::toResponse);
  }

  @Override
  public Optional<GenreResponseDTO> findByCode(String code) {
    Objects.requireNonNull(code, "El código no puede ser nulo para realizar la búsqueda");
    return genreRepository
        .findByCode(StringFormatter.generateCode(code))
        .map(genreMapper::toResponse);
  }

  @Override
  public Optional<GenreResponseDTO> findByName(String name) {
    Objects.requireNonNull(name, "El nombre no puede ser nulo para realizar la búsqueda");
    return genreRepository
        .findByCode(StringFormatter.generateCode(name))
        .map(genreMapper::toResponse);
  }

  @Override
  @Transactional
  public GenreResponseDTO create(GenreRequestDTO requestGenre) {
    Objects.requireNonNull(requestGenre, "El género que quiere guardar no puede ser nulo");
    String formattedName = StringFormatter.formatName(requestGenre.name());
    String code = StringFormatter.generateCode(formattedName);
    Optional<Genre> optionalGenre = genreRepository.findByCodeIncludingDeleted(code);
    if (optionalGenre.isPresent()) {
      Genre existingGenre = optionalGenre.get();
      return this.restoreAndUpdateGenreOrThrow(existingGenre, code, formattedName);
    }
    Genre newGenre = new Genre();
    newGenre.setName(formattedName);
    newGenre.setCode(code);
    return genreMapper.toResponse(genreRepository.save(newGenre));
  }

  @Override
  @Transactional
  public GenreResponseDTO update(Long id, GenreRequestDTO genre) {
    Objects.requireNonNull(id, "El ID no puede ser nulo para realizar la actualización");
    Objects.requireNonNull(genre, "El género que quiere actualizar no puede ser nulo");
    Genre existingGenre = this.getGenreByIdOrThrow(id, "No se puede actualizar");
    String formattedName = StringFormatter.formatName(genre.name());
    String code = StringFormatter.generateCode(formattedName);
    if (!existingGenre.getName().equals(formattedName)) {
      this.checkNameIsUniqueOrThrow(id, code);
    }
    existingGenre.setName(formattedName);
    existingGenre.setCode(code);
    return genreMapper.toResponse(genreRepository.save(existingGenre));
  }

  @Override
  @Transactional
  public void deleteById(Long id) {
    Objects.requireNonNull(id, "El ID no puede ser nulo para realizar la eliminación");
    Genre genre = this.getGenreByIdOrThrow(id, "No se puede eliminar");
    this.checkNoBooksAssociatedOrThrow(genre);
    genreRepository.delete(genre);
  }

  private GenreResponseDTO restoreAndUpdateGenreOrThrow(
      Genre existingGenre, String code, String name) {
    if (!existingGenre.isDeleted()) {
      throw new DuplicateResourceException(
          "Ya existe un género activo con el código: "
              + code
              + " y el nombre: "
              + existingGenre.getName());
    } else {
      existingGenre.setDeleted(false);
      existingGenre.setName(name);
      existingGenre.setCode(code);
      return genreMapper.toResponse(genreRepository.save(existingGenre));
    }
  }

  private Genre getGenreByIdOrThrow(Long id, String errorMessagePrefix) {
    String prefix =
        (errorMessagePrefix != null && !errorMessagePrefix.isBlank())
            ? errorMessagePrefix + ". "
            : "";
    return genreRepository
        .findById(id)
        .orElseThrow(() -> new ResourceNotFoundException(prefix + this.buildIdNotFoundMessage(id)));
  }

  private void checkNameIsUniqueOrThrow(Long id, String code) {
    Optional<Genre> duplicateCandidate = genreRepository.findByCodeIncludingDeleted(code);
    if (duplicateCandidate.isPresent() && !duplicateCandidate.get().getId().equals(id)) {
      Genre duplicate = duplicateCandidate.get();
      throw new DuplicateResourceException(
          "No se puede actualizar. El nombre: "
              + duplicate.getName()
              + " ya pertenece a otro género activo o en el registro histórico");
    }
  }

  private void checkNoBooksAssociatedOrThrow(Genre genre) {
    if (bookRepository.countByGenreIdIncludingDeleted(genre.getId()) > 0) {
      throw new ResourceInUseException(
          "No se puede eliminar el género: "
              + genre.getName()
              + " porque existen libros asociados a él en el catálogo actual o en el registro histórico");
    }
  }

  private String buildIdNotFoundMessage(Long id) {
    return "Género con ID: " + id + " no encontrado";
  }

  private PagedResponse<GenreResponseDTO> toPagedResponse(Page<Genre> genrePage) {
    return PagedResponse.from(genrePage.map(genreMapper::toResponse));
  }
}
