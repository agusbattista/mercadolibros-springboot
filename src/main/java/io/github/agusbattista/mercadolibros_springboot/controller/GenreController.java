package io.github.agusbattista.mercadolibros_springboot.controller;

import io.github.agusbattista.mercadolibros_springboot.dto.GenreRequestDTO;
import io.github.agusbattista.mercadolibros_springboot.dto.GenreResponseDTO;
import io.github.agusbattista.mercadolibros_springboot.dto.PagedResponse;
import io.github.agusbattista.mercadolibros_springboot.exception.ResourceNotFoundException;
import io.github.agusbattista.mercadolibros_springboot.service.GenreService;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
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
@RequestMapping("/api/genres")
public class GenreController {

  private final GenreService genreService;

  public GenreController(GenreService genreService) {
    this.genreService = genreService;
  }

  @GetMapping
  public ResponseEntity<PagedResponse<GenreResponseDTO>> findAll(Pageable pageable) {
    return ResponseEntity.ok(genreService.findAll(pageable));
  }

  @GetMapping("/{id}")
  public ResponseEntity<GenreResponseDTO> findById(@PathVariable Long id) {
    GenreResponseDTO genre =
        genreService
            .findById(id)
            .orElseThrow(
                () -> new ResourceNotFoundException("Género con ID: " + id + " no encontrado"));
    return ResponseEntity.ok(genre);
  }

  @GetMapping("/code/{code}")
  public ResponseEntity<GenreResponseDTO> findByCode(@PathVariable String code) {
    GenreResponseDTO genre =
        genreService
            .findByCode(code)
            .orElseThrow(
                () ->
                    new ResourceNotFoundException("Género con código: " + code + " no encontrado"));
    return ResponseEntity.ok(genre);
  }

  @GetMapping("/search")
  public ResponseEntity<GenreResponseDTO> findByName(@RequestParam String name) {
    GenreResponseDTO genre =
        genreService
            .findByName(name)
            .orElseThrow(
                () ->
                    new ResourceNotFoundException("Género con nombre: " + name + " no encontrado"));
    return ResponseEntity.ok(genre);
  }

  @PostMapping
  public ResponseEntity<GenreResponseDTO> create(@Valid @RequestBody GenreRequestDTO genre) {
    GenreResponseDTO newGenre = genreService.create(genre);
    URI uri = buildUri(newGenre);
    return ResponseEntity.created(uri).body(newGenre);
  }

  @PutMapping("/{id}")
  public ResponseEntity<GenreResponseDTO> update(
      @PathVariable Long id, @Valid @RequestBody GenreRequestDTO genre) {
    return ResponseEntity.ok(genreService.update(id, genre));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    genreService.deleteById(id);
    return ResponseEntity.noContent().build();
  }

  private URI buildUri(GenreResponseDTO book) {
    return ServletUriComponentsBuilder.fromCurrentRequest()
        .path("/{id}")
        .buildAndExpand(book.id())
        .toUri();
  }
}
