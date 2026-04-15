package io.github.agusbattista.mercadolibros_springboot.service;

import io.github.agusbattista.mercadolibros_springboot.dto.AuthorRequestDTO;
import io.github.agusbattista.mercadolibros_springboot.dto.AuthorResponseDTO;
import io.github.agusbattista.mercadolibros_springboot.dto.PagedResponse;
import io.github.agusbattista.mercadolibros_springboot.exception.ResourceNotFoundException;
import io.github.agusbattista.mercadolibros_springboot.mapper.AuthorMapper;
import io.github.agusbattista.mercadolibros_springboot.model.Author;
import io.github.agusbattista.mercadolibros_springboot.repository.AuthorRepository;
import io.github.agusbattista.mercadolibros_springboot.utils.StringFormatter;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AuthorServiceImpl implements AuthorService {

  private final AuthorRepository authorRepository;
  private final AuthorMapper authorMapper;

  @Override
  public PagedResponse<AuthorResponseDTO> findAll(Pageable pageable) {
    return this.toPagedResponse(authorRepository.findAll(pageable));
  }

  @Override
  public Optional<AuthorResponseDTO> findByUuid(UUID uuid) {
    Objects.requireNonNull(uuid, "El UUID no puede ser nulo para realizar la búsqueda");
    return authorRepository.findByUuid(uuid).map(authorMapper::toResponse);
  }

  @Override
  public PagedResponse<AuthorResponseDTO> findByName(String name, Pageable pageable) {
    Objects.requireNonNull(name, "El nombre no puede ser nulo para realizar la búsqueda");
    if (name.isBlank()) {
      return this.findAll(pageable);
    }
    String formattedName = StringFormatter.formatName(name);
    return this.toPagedResponse(
        authorRepository.findByFullNameContainingIgnoreCase(formattedName, pageable));
  }

  @Transactional
  @Override
  public AuthorResponseDTO create(AuthorRequestDTO requestAuthor) {
    Objects.requireNonNull(requestAuthor, "El autor que quiere guardar no puede ser nulo");
    Author newAuthor = authorMapper.toEntity(requestAuthor);
    newAuthor.setFullName(StringFormatter.formatName(newAuthor.getFullName()));
    return authorMapper.toResponse(authorRepository.save(newAuthor));
  }

  @Transactional
  @Override
  public AuthorResponseDTO update(UUID uuid, AuthorRequestDTO author) {
    Objects.requireNonNull(uuid, "El UUID no puede ser nulo para realizar la actualización");
    Objects.requireNonNull(author, "El autor no puede ser nulo para realizar la actualización");
    Author existingAuthor = this.getAuthorByUuidOrThrow(uuid, "No se puede actualizar");
    authorMapper.updateEntityFromRequest(author, existingAuthor);
    existingAuthor.setFullName(StringFormatter.formatName(existingAuthor.getFullName()));
    return authorMapper.toResponse(authorRepository.save(existingAuthor));
  }

  @Transactional
  @Override
  public void deleteByUuid(UUID uuid) {
    Objects.requireNonNull(uuid, "El UUID no puede ser nulo para realizar la eliminación");
    Author author = this.getAuthorByUuidOrThrow(uuid, "No se puede eliminar");
    authorRepository.delete(author);
  }

  @Transactional
  @Override
  public AuthorResponseDTO restore(UUID uuid) {
    Objects.requireNonNull(uuid, "El UUID no puede ser nulo para realizar la restauración");
    Optional<Author> optionalAuthor = authorRepository.findByUuidIncludingDeleted(uuid.toString());
    if (optionalAuthor.isPresent()) {
      Author author = optionalAuthor.get();
      return processRestore(author);
    } else {
      throw new ResourceNotFoundException(
          "No se puede restaurar. " + this.buildUuidNotFoundMessage(uuid));
    }
  }

  private AuthorResponseDTO processRestore(Author author) {
    if (!author.isDeleted()) {
      return authorMapper.toResponse(author);
    }
    author.setDeleted(false);
    return authorMapper.toResponse(authorRepository.save(author));
  }

  private PagedResponse<AuthorResponseDTO> toPagedResponse(Page<Author> authorPage) {
    return PagedResponse.from(authorPage.map(authorMapper::toResponse));
  }

  private Author getAuthorByUuidOrThrow(UUID uuid, String errorMessagePrefix) {
    String prefix =
        (errorMessagePrefix != null && !errorMessagePrefix.isBlank())
            ? errorMessagePrefix + ". "
            : "";
    return authorRepository
        .findByUuid(uuid)
        .orElseThrow(
            () -> new ResourceNotFoundException(prefix + this.buildUuidNotFoundMessage(uuid)));
  }

  private String buildUuidNotFoundMessage(UUID uuid) {
    return "Autor con UUID: " + uuid + " no encontrado";
  }
}
