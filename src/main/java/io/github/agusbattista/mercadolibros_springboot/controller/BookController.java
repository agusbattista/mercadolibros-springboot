package io.github.agusbattista.mercadolibros_springboot.controller;

import io.github.agusbattista.mercadolibros_springboot.dto.BookRequestDTO;
import io.github.agusbattista.mercadolibros_springboot.dto.BookResponseDTO;
import io.github.agusbattista.mercadolibros_springboot.dto.PagedResponse;
import io.github.agusbattista.mercadolibros_springboot.exception.ResourceNotFoundException;
import io.github.agusbattista.mercadolibros_springboot.service.BookService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
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
@RequestMapping("/api/books")
public class BookController {

  private final BookService bookService;

  public BookController(BookService bookService) {
    this.bookService = bookService;
  }

  @GetMapping
  public ResponseEntity<PagedResponse<BookResponseDTO>> findAll(Pageable pageable) {
    return ResponseEntity.ok(bookService.findAll(pageable));
  }

  @GetMapping("/{uuid}")
  public ResponseEntity<BookResponseDTO> findByUuid(@PathVariable UUID uuid) {
    BookResponseDTO book =
        bookService
            .findByUuid(uuid)
            .orElseThrow(
                () -> new ResourceNotFoundException("Libro con UUID: " + uuid + " no encontrado"));
    return ResponseEntity.ok(book);
  }

  @GetMapping("/isbn/{isbn}")
  public ResponseEntity<BookResponseDTO> findByIsbn(@PathVariable String isbn) {
    BookResponseDTO book =
        bookService
            .findByIsbn(isbn)
            .orElseThrow(
                () -> new ResourceNotFoundException("Libro con ISBN: " + isbn + " no encontrado"));
    return ResponseEntity.ok(book);
  }

  @GetMapping("/search")
  public ResponseEntity<PagedResponse<BookResponseDTO>> findBooksByCriteria(
      @RequestParam(required = false) String title,
      @RequestParam(required = false) String authors,
      @RequestParam(required = false) String genre,
      @RequestParam(required = false) String publisher,
      Pageable pageable) {
    return ResponseEntity.ok(
        bookService.findBooksByCriteria(title, authors, genre, publisher, pageable));
  }

  @PostMapping
  public ResponseEntity<BookResponseDTO> create(@Valid @RequestBody BookRequestDTO book) {
    BookResponseDTO newBook = bookService.create(book);
    URI uri = buildUri(newBook, "/{uuid}");
    return ResponseEntity.created(uri).body(newBook);
  }

  @PutMapping("/{uuid}")
  public ResponseEntity<BookResponseDTO> update(
      @PathVariable UUID uuid, @Valid @RequestBody BookRequestDTO book) {
    return ResponseEntity.ok(bookService.update(uuid, book));
  }

  @DeleteMapping("/{uuid}")
  public ResponseEntity<Void> delete(@PathVariable UUID uuid) {
    bookService.deleteByUuid(uuid);
    return ResponseEntity.noContent().build();
  }

  private URI buildUri(BookResponseDTO book, String path) {
    return ServletUriComponentsBuilder.fromCurrentRequest()
        .path(path)
        .buildAndExpand(book.uuid())
        .toUri();
  }
}
