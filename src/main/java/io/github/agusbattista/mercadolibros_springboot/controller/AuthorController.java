package io.github.agusbattista.mercadolibros_springboot.controller;

import io.github.agusbattista.mercadolibros_springboot.dto.AuthorRequestDTO;
import io.github.agusbattista.mercadolibros_springboot.dto.AuthorResponseDTO;
import io.github.agusbattista.mercadolibros_springboot.dto.PagedResponse;
import io.github.agusbattista.mercadolibros_springboot.exception.ResourceNotFoundException;
import io.github.agusbattista.mercadolibros_springboot.service.AuthorService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/authors")
@RequiredArgsConstructor
public class AuthorController {

  private final AuthorService authorService;

  @GetMapping
  public ResponseEntity<PagedResponse<AuthorResponseDTO>> findAll(Pageable pageable) {
    return ResponseEntity.ok(authorService.findAll(pageable));
  }

  @GetMapping("/{uuid}")
  public ResponseEntity<AuthorResponseDTO> findByUuid(@PathVariable UUID uuid) {
    AuthorResponseDTO author =
        authorService
            .findByUuid(uuid)
            .orElseThrow(
                () -> new ResourceNotFoundException("Autor con UUID: " + uuid + " no encontrado"));
    return ResponseEntity.ok(author);
  }

  @GetMapping("/search")
  public ResponseEntity<PagedResponse<AuthorResponseDTO>> findByName(
      @RequestParam String name, Pageable pageable) {
    return ResponseEntity.ok(authorService.findByName(name, pageable));
  }

  @PostMapping
  public ResponseEntity<AuthorResponseDTO> create(@Valid @RequestBody AuthorRequestDTO author) {
    AuthorResponseDTO newAuthor = authorService.create(author);
    URI uri = this.buildUri(newAuthor);
    return ResponseEntity.created(uri).body(newAuthor);
  }

  @PutMapping("/{uuid}")
  public ResponseEntity<AuthorResponseDTO> update(
      @PathVariable UUID uuid, @Valid @RequestBody AuthorRequestDTO author) {
    return ResponseEntity.ok(authorService.update(uuid, author));
  }

  @DeleteMapping("/{uuid}")
  public ResponseEntity<Void> delete(@PathVariable UUID uuid) {
    authorService.deleteByUuid(uuid);
    return ResponseEntity.noContent().build();
  }

  @PatchMapping("/{uuid}/restore")
  public ResponseEntity<AuthorResponseDTO> restore(@PathVariable UUID uuid) {
    return ResponseEntity.ok(authorService.restore(uuid));
  }

  private URI buildUri(AuthorResponseDTO author) {
    return ServletUriComponentsBuilder.fromCurrentRequest()
        .path("/{uuid}")
        .buildAndExpand(author.uuid())
        .toUri();
  }
}
