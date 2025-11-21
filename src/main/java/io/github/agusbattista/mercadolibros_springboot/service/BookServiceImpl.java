package io.github.agusbattista.mercadolibros_springboot.service;

import io.github.agusbattista.mercadolibros_springboot.model.Book;
import io.github.agusbattista.mercadolibros_springboot.repository.BookRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BookServiceImpl implements BookService {

  private final BookRepository bookRepository;

  @Autowired
  public BookServiceImpl(BookRepository bookRepository) {
    this.bookRepository = bookRepository;
  }

  @Override
  public List<Book> findAll() {
    return bookRepository.findAll();
  }

  @Override
  public Optional<Book> findById(Long id) {
    return bookRepository.findById(id);
  }

  @Override
  public Book save(Book book) {
    return bookRepository.save(book);
  }

  @Override
  public void deleteById(Long id) {
    bookRepository.deleteById(id);
  }

  @Override
  public Book update(Long id, Book book) {
    return this.findById(id)
        .map(
            existingBook -> {
              existingBook.setIsbn(book.getIsbn());
              existingBook.setTitle(book.getTitle());
              existingBook.setAuthors(book.getAuthors());
              existingBook.setPrice(book.getPrice());
              existingBook.setDescription(book.getDescription());
              existingBook.setPublisher(book.getPublisher());
              return bookRepository.save(existingBook);
            })
        .orElseThrow(() -> new RuntimeException("Book not found with id: " + id));
  }
}
