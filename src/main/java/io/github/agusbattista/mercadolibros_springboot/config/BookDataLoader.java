package io.github.agusbattista.mercadolibros_springboot.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.agusbattista.mercadolibros_springboot.dto.BookRequestDTO;
import io.github.agusbattista.mercadolibros_springboot.mapper.BookMapper;
import io.github.agusbattista.mercadolibros_springboot.model.Book;
import io.github.agusbattista.mercadolibros_springboot.repository.BookRepository;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class BookDataLoader implements CommandLineRunner {

  private static final Logger log = LoggerFactory.getLogger(BookDataLoader.class);

  private final BookRepository bookRepository;
  private final BookMapper bookMapper;
  private final ObjectMapper objectMapper;

  public BookDataLoader(
      BookRepository bookRepository, BookMapper bookMapper, ObjectMapper objectMapper) {
    this.bookRepository = bookRepository;
    this.bookMapper = bookMapper;
    this.objectMapper = objectMapper;
  }

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
    List<BookRequestDTO> dtos = objectMapper.readValue(inputStream, new TypeReference<>() {});
    List<Book> books = dtos.stream().map(bookMapper::toEntity).toList();
    bookRepository.saveAll(books);
    log.info("Carga exitosa: {} libros guardados en la base de datos", books.size());
  }
}
