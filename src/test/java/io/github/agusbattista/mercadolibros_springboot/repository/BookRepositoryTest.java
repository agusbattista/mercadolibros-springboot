package io.github.agusbattista.mercadolibros_springboot.repository;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.agusbattista.mercadolibros_springboot.model.Book;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

@DataJpaTest
class BookRepositoryTest {

  @Autowired private BookRepository bookRepository;

  @Autowired private TestEntityManager entityManager;

  private Book book1;
  private Book book2;
  private Book book3;

  @BeforeEach
  void setUp() {
    book1 = new Book();
    book1.setIsbn("9786073155731");
    book1.setTitle("Canción de Hielo y Fuego (Colección)");
    book1.setAuthors("George R. R. Martin");
    book1.setPrice(new BigDecimal(33.99));
    book1.setDescription(
        "La saga completa de Canción de Hielo y Fuego, la obra maestra de la fantasía moderna.");
    book1.setPublisher("Plaza & Janés");
    book1.setGenre("Fantasía");
    book1.setImageUrl(
        "https://books.google.com/books/publisher/content?id=krMsDwAAQBAJ&printsec=frontcover&img=1&zoom=4&edge=curl&source=gbs_api");
    entityManager.persist(book1);

    book2 = new Book();
    book2.setIsbn("9788445073728");
    book2.setTitle("La Comunidad del Anillo");
    book2.setAuthors("J. R. R. Tolkien");
    book2.setPrice(new BigDecimal(12.99));
    book2.setDescription("La primera parte de la historia de la Guerra del Anillo.");
    book2.setPublisher("Minotauro");
    book2.setGenre("Fantasía");
    book2.setImageUrl(
        "https://books.google.com/books/content?id=DYmUGGwZ8_oC&printsec=frontcover&img=1&zoom=4&edge=curl&source=gbs_api");
    entityManager.persist(book2);

    book3 = new Book();
    book3.setIsbn("9788401337208");
    book3.setTitle("El nombre del viento");
    book3.setAuthors("Patrick Rothfuss");
    book3.setPrice(new BigDecimal(17.99));
    book3.setDescription(
        "La historia de Kvothe, músico, mendigo, ladrón, estudiante, mago, héroe y asesino.");
    book3.setPublisher("Plaza & Janés");
    book3.setGenre("Fantasía");
    book3.setImageUrl(
        "https://books.google.com/books/publisher/content?id=IZWWDwAAQBAJ&printsec=frontcover&img=1&zoom=4&edge=curl&source=gbs_api");
    entityManager.persist(book3);

    entityManager.flush();
  }

  @Test
  void findBooksByCriteria_ByTitle_ShouldReturnMatchingBooks() {
    List<Book> found = bookRepository.findBooksByCriteria("hielo", null, null, null);
    assertThat(found)
        .hasSize(1)
        .extracting(Book::getTitle)
        .containsExactly("Canción de Hielo y Fuego (Colección)");
    List<Book> otherFound = bookRepository.findBooksByCriteria("n", null, null, null);
    assertThat(otherFound).hasSize(3);
  }

  @Test
  void findBooksByCriteria_ByTitle_ShouldNotReturnBooks() {
    List<Book> found = bookRepository.findBooksByCriteria("Refactoring", null, null, null);
    assertThat(found).isEmpty();
  }

  @Test
  void findBooksByCriteria_ByAuthors_ShouldReturnMatchingBooks() {
    List<Book> found = bookRepository.findBooksByCriteria(null, "martin", null, null);
    assertThat(found)
        .hasSize(1)
        .extracting(Book::getAuthors)
        .containsExactly("George R. R. Martin");
  }

  @Test
  void findBooksByCriteria_ByAuthors_ShouldNotReturnBooks() {
    List<Book> found = bookRepository.findBooksByCriteria(null, "martina", null, null);
    assertThat(found).isEmpty();
  }

  @Test
  void findBooksByCriteria_ByGenre_ShouldReturnMatchingBooks() {
    List<Book> found = bookRepository.findBooksByCriteria(null, null, "FANTASÍA", null);
    assertThat(found).hasSize(3);
  }

  @Test
  void findBooksByCriteria_ByGenre_ShouldNotReturnBooks() {
    List<Book> found = bookRepository.findBooksByCriteria(null, null, "FANTASY", null);
    assertThat(found).isEmpty();
  }

  @Test
  void findBooksByCriteria_ByPublisher_ShouldReturnMatchingBooks() {
    List<Book> found = bookRepository.findBooksByCriteria(null, null, null, "Janés");
    assertThat(found).hasSize(2);
  }

  @Test
  void findBooksByCriteria_ByPublisher_ShouldNotReturnBooks() {
    List<Book> found = bookRepository.findBooksByCriteria(null, null, null, "Debolsillo");
    assertThat(found).isEmpty();
  }

  @Test
  void findBooksByCriteria_WithMultipleCriteria_ShouldFilterCorrectly() {
    List<Book> found = bookRepository.findBooksByCriteria(null, null, "FANTASÍA", "Janés");
    assertThat(found)
        .hasSize(2)
        .extracting(Book::getTitle)
        .containsExactlyInAnyOrder("Canción de Hielo y Fuego (Colección)", "El nombre del viento");
    List<Book> otherFound = bookRepository.findBooksByCriteria("viento", null, "FANTASÍA", "Janés");
    assertThat(otherFound).hasSize(1);
    List<Book> otherFound2 =
        bookRepository.findBooksByCriteria("viento", "patricK", "FANTASÍA", "Janés");
    assertThat(otherFound2).hasSize(1);
  }

  @Test
  void findBooksByCriteria_WithNoCriteria_ShouldReturnAll() {
    List<Book> found = bookRepository.findBooksByCriteria(null, null, null, null);
    assertThat(found).hasSize(3);
  }

  @Test
  void findByIsbnIncludingDeleted_WhenBookIsDeleted_ShouldReturnBook() {
    book1.setDeleted(true);
    entityManager.merge(book1);
    entityManager.flush();
    Optional<Book> found = bookRepository.findByIsbnIncludingDeleted("9786073155731");
    assertThat(found).isPresent();
    assertThat(found.get().getTitle()).isEqualTo("Canción de Hielo y Fuego (Colección)");
    assertThat(found.get().isDeleted()).isTrue();
  }

  @Test
  void findByIsbnIncludingDeleted_WhenBookExistsAndNotDeleted_ShouldReturnBook() {
    Optional<Book> found = bookRepository.findByIsbnIncludingDeleted("9788445073728");
    assertThat(found).isPresent();
    assertThat(found.get().isDeleted()).isFalse();
  }

  @Test
  void findByIsbnIncludingDeleted_WhenBookNotExists_ShouldReturnEmpty() {
    Optional<Book> found = bookRepository.findByIsbnIncludingDeleted("0000000000000");
    assertThat(found).isEmpty();
  }
}
