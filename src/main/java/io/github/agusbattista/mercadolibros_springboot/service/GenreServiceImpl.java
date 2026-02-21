package io.github.agusbattista.mercadolibros_springboot.service;

import io.github.agusbattista.mercadolibros_springboot.dto.GenreRequestDTO;
import io.github.agusbattista.mercadolibros_springboot.dto.GenreResponseDTO;
import io.github.agusbattista.mercadolibros_springboot.dto.PagedResponse;
import io.github.agusbattista.mercadolibros_springboot.exception.DuplicateResourceException;
import io.github.agusbattista.mercadolibros_springboot.exception.ResourceNotFoundException;
import io.github.agusbattista.mercadolibros_springboot.mapper.GenreMapper;
import io.github.agusbattista.mercadolibros_springboot.model.Genre;
import io.github.agusbattista.mercadolibros_springboot.repository.BookRepository;
import io.github.agusbattista.mercadolibros_springboot.repository.GenreRepository;
import io.github.agusbattista.mercadolibros_springboot.utils.StringFormatter;
import java.util.Objects;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class GenreServiceImpl implements GenreService {

  private final GenreRepository genreRepository;
  private final BookRepository bookRepository;
  private final GenreMapper genreMapper;

  public GenreServiceImpl(
      GenreRepository genreRepository, BookRepository bookRepository, GenreMapper genreMapper) {
    this.genreRepository = genreRepository;
    this.bookRepository = bookRepository;
    this.genreMapper = genreMapper;
  }

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
    return genreRepository.findByCode(code).map(genreMapper::toResponse);
  }

  @Override
  public Optional<GenreResponseDTO> findByName(String name) {
    Objects.requireNonNull(name, "El nombre no puede ser nulo para realizar la búsqueda");
    return genreRepository.findByName(StringFormatter.formatName(name)).map(genreMapper::toResponse);
  }

  @Override
  @Transactional
  public GenreResponseDTO create(GenreRequestDTO requestGenre) {
    Objects.requireNonNull(requestGenre, "El género que quiere guardar no puede ser nulo");
    String name = StringFormatter.formatName(requestGenre.name());
    String code = StringFormatter.generateCode(name);
    Optional<Genre> optionalGenre = genreRepository.findByCodeIncludingDeleted(code);
    if (optionalGenre.isPresent()) {
      Genre existingGenre = optionalGenre.get();
      if (!existingGenre.isDeleted()) {
        throw new DuplicateResourceException(
            "Ya existe un género activo con el código: " + code + " y el nombre: " + name);
      } else {
        existingGenre.setDeleted(false);
        existingGenre.setName(name);
        existingGenre.setCode(code);
        return genreMapper.toResponse(genreRepository.save(existingGenre));
      }
    }
    Genre newGenre = new Genre();
    newGenre.setName(name);
    newGenre.setCode(code);
    return genreMapper.toResponse(genreRepository.save(newGenre));
  }

  @Override
  @Transactional
  public void deleteById(Long id) {
    Objects.requireNonNull(id, "El ID no puede ser nulo para realizar la eliminación");
    // TODO: Lógica para eliminar el género, verificando que no tenga libros asociados
    Genre genre =
        genreRepository
            .findById(id)
            .orElseThrow(
                () ->
                    new ResourceNotFoundException("No se puede eliminar. " + this.idNotFound(id)));
    genreRepository.delete(genre);
  }

  @Override
  @Transactional
  public GenreResponseDTO update(Long id, GenreRequestDTO genre) {
    Objects.requireNonNull(id, "El ID no puede ser nulo para realizar la actualización");
    Objects.requireNonNull(genre, "El género que quiere actualizar no puede ser nulo");
    Genre existingGenre =
        genreRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(this.idNotFound(id)));
    String name = StringFormatter.formatName(genre.name());
    String code = StringFormatter.generateCode(name);
    if (!existingGenre.getName().equals(name)) {
      Optional<Genre> duplicateCandidate = genreRepository.findByCodeIncludingDeleted(code);
      if (duplicateCandidate.isPresent() && !duplicateCandidate.get().getId().equals(id)) {
        throw new DuplicateResourceException(
            "No se puede actualizar. El nombre: " + name + " ya pertenece a otro género");
      }
    }
    existingGenre.setName(name);
    existingGenre.setCode(code);
    return genreMapper.toResponse(genreRepository.save(existingGenre));
  }

  private String idNotFound(Long id) {
    return "Género con ID: " + id + " no encontrado";
  }

  private PagedResponse<GenreResponseDTO> toPagedResponse(Page<Genre> genrePage) {
    return PagedResponse.from(genrePage.map(genreMapper::toResponse));
  }
}
