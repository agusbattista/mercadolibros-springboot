package io.github.agusbattista.mercadolibros_springboot.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.agusbattista.mercadolibros_springboot.model.Book;
import io.github.agusbattista.mercadolibros_springboot.model.Genre;
import io.github.agusbattista.mercadolibros_springboot.repository.BookRepository;
import io.github.agusbattista.mercadolibros_springboot.repository.GenreRepository;
import io.github.agusbattista.mercadolibros_springboot.utils.StringFormatter;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class BookDataLoader implements CommandLineRunner {

  private static final Logger log = LoggerFactory.getLogger(BookDataLoader.class);

  private final BookRepository bookRepository;
  private final GenreRepository genreRepository;
  private final ObjectMapper objectMapper;

  public BookDataLoader(
      BookRepository bookRepository, GenreRepository genreRepository, ObjectMapper objectMapper) {
    this.bookRepository = bookRepository;
    this.genreRepository = genreRepository;
    this.objectMapper = objectMapper;
  }

  private record BookSeedDTO(
      String isbn,
      String title,
      String authors,
      BigDecimal price,
      String description,
      String publisher,
      String genre,
      String imageUrl) {}

  @Override
  public void run(String... args) throws Exception {
    if (bookRepository.countAllIncludingDeleted() == 0) {
      log.info("Iniciando la carga de datos de prueba desde /data/books.json...");
      this.loadBooksFromJson();
    } else {
      log.info("La base de datos ya contiene libros. Se omite la carga inicial");
    }
  }

  private void loadBooksFromJson() throws IOException {
    InputStream inputStream = getClass().getResourceAsStream("/data/books.json");
    List<BookSeedDTO> seedBooks = objectMapper.readValue(inputStream, new TypeReference<>() {});
    Map<String, Genre> genreMap = this.saveGenres(seedBooks);
    List<Book> books = this.createBookList(seedBooks, genreMap);
    bookRepository.saveAll(books);
    log.info(
        "Carga exitosa de {} géneros y {} libros guardados en la base de datos",
        genreMap.size(),
        books.size());
  }

  @NonNull
  private List<Book> createBookList(List<BookSeedDTO> seedBooks, Map<String, Genre> genreMap) {
    return seedBooks.stream()
        .map(
            seed -> {
              Book book = new Book();
              book.setIsbn(seed.isbn());
              book.setTitle(seed.title());
              book.setAuthors(seed.authors());
              book.setPrice(seed.price());
              book.setDescription(seed.description());
              book.setPublisher(seed.publisher());
              book.setImageUrl(seed.imageUrl());
              book.setGenre(genreMap.get(seed.genre()));
              return book;
            })
        .toList();
  }

  @NonNull
  private Map<String, Genre> saveGenres(List<BookSeedDTO> seedBooks) {
    Map<String, Genre> genreMap = new HashMap<>();
    for (BookSeedDTO seed : seedBooks) {
      if (!genreMap.containsKey(seed.genre())) {
        String formattedName = StringFormatter.formatName(seed.genre());
        String code = StringFormatter.generateCode(formattedName);
        Genre genre = getGenre(code, formattedName);
        genreMap.put(seed.genre(), genre);
      }
    }
    return genreMap;
  }

  @NonNull
  private Genre getGenre(String code, String formattedName) {
    return genreRepository
        .findByCode(code)
        .orElseGet(
            () -> {
              Genre newGenre = new Genre();
              newGenre.setName(formattedName);
              newGenre.setCode(code);
              return genreRepository.save(newGenre);
            });
  }
}
