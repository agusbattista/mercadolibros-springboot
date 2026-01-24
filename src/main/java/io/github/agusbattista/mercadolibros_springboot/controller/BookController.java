package io.github.agusbattista.mercadolibros_springboot.controller;

import io.github.agusbattista.mercadolibros_springboot.dto.BookRequestDTO;
import io.github.agusbattista.mercadolibros_springboot.dto.BookResponseDTO;
import io.github.agusbattista.mercadolibros_springboot.dto.ValidationGroups;
import io.github.agusbattista.mercadolibros_springboot.exception.ResourceNotFoundException;
import io.github.agusbattista.mercadolibros_springboot.service.BookService;
import jakarta.validation.groups.Default;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
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

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/books")
public class BookController {

  private final BookService bookService;

  public BookController(BookService bookService) {
    this.bookService = bookService;
  }

  @GetMapping
  public ResponseEntity<List<BookResponseDTO>> findAll() {
    return ResponseEntity.ok(bookService.findAll());
  }

  @GetMapping("/{isbn}")
  public ResponseEntity<BookResponseDTO> findByIsbn(@PathVariable String isbn) {
    BookResponseDTO book =
        bookService
            .findByIsbn(isbn)
            .orElseThrow(
                () -> new ResourceNotFoundException("Libro con ISBN: " + isbn + " no encontrado"));
    return ResponseEntity.ok(book);
  }

  @GetMapping("/search")
  public ResponseEntity<List<BookResponseDTO>> findBooksByCriteria(
      @RequestParam(required = false) String title,
      @RequestParam(required = false) String authors,
      @RequestParam(required = false) String genre,
      @RequestParam(required = false) String publisher) {
    return ResponseEntity.ok(bookService.findBooksByCriteria(title, authors, genre, publisher));
  }

  @PostMapping
  public ResponseEntity<BookResponseDTO> save(
      @Validated({ValidationGroups.Create.class, Default.class}) @RequestBody BookRequestDTO book) {
    return ResponseEntity.status(HttpStatus.CREATED).body(bookService.save(book));
  }

  @PutMapping("/{isbn}")
  public ResponseEntity<BookResponseDTO> update(
      @PathVariable String isbn, @Validated(Default.class) @RequestBody BookRequestDTO book) {
    return ResponseEntity.ok(bookService.update(isbn, book));
  }

  @DeleteMapping("/{isbn}")
  public ResponseEntity<Void> delete(@PathVariable String isbn) {
    bookService.deleteByIsbn(isbn);
    return ResponseEntity.noContent().build();
  }
}
