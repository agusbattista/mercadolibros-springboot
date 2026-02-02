package io.github.agusbattista.mercadolibros_springboot.repository;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.agusbattista.mercadolibros_springboot.model.Book;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

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
    book1.setPrice(new BigDecimal("33.99"));
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
    book2.setPrice(new BigDecimal("12.99"));
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
    book3.setPrice(new BigDecimal("17.99"));
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
    Page<Book> found =
        bookRepository.findBooksByCriteria("hielo", null, null, null, Pageable.unpaged());
    assertThat(found.getContent())
        .hasSize(1)
        .extracting(Book::getTitle)
        .containsExactly("Canción de Hielo y Fuego (Colección)");
    Page<Book> otherFound =
        bookRepository.findBooksByCriteria("n", null, null, null, Pageable.unpaged());
    assertThat(otherFound.getContent()).hasSize(3);
  }

  @Test
  void findBooksByCriteria_ByTitle_ShouldNotReturnBooks() {
    Page<Book> found =
        bookRepository.findBooksByCriteria("Refactoring", null, null, null, Pageable.unpaged());
    assertThat(found.getContent()).isEmpty();
  }

  @Test
  void findBooksByCriteria_ByAuthors_ShouldReturnMatchingBooks() {
    Page<Book> found =
        bookRepository.findBooksByCriteria(null, "martin", null, null, Pageable.unpaged());
    assertThat(found.getContent())
        .hasSize(1)
        .extracting(Book::getAuthors)
        .containsExactly("George R. R. Martin");
  }

  @Test
  void findBooksByCriteria_ByAuthors_ShouldNotReturnBooks() {
    Page<Book> found =
        bookRepository.findBooksByCriteria(null, "martina", null, null, Pageable.unpaged());
    assertThat(found.getContent()).isEmpty();
  }

  @Test
  void findBooksByCriteria_ByGenre_ShouldReturnMatchingBooks() {
    Page<Book> found =
        bookRepository.findBooksByCriteria(null, null, "FANTASÍA", null, Pageable.unpaged());
    assertThat(found.getContent()).hasSize(3);
  }

  @Test
  void findBooksByCriteria_ByGenre_ShouldNotReturnBooks() {
    Page<Book> found =
        bookRepository.findBooksByCriteria(null, null, "FANTASY", null, Pageable.unpaged());
    assertThat(found.getContent()).isEmpty();
  }

  @Test
  void findBooksByCriteria_ByPublisher_ShouldReturnMatchingBooks() {
    Page<Book> found =
        bookRepository.findBooksByCriteria(null, null, null, "Janés", Pageable.unpaged());
    assertThat(found.getContent()).hasSize(2);
  }

  @Test
  void findBooksByCriteria_ByPublisher_ShouldNotReturnBooks() {
    Page<Book> found =
        bookRepository.findBooksByCriteria(null, null, null, "Debolsillo", Pageable.unpaged());
    assertThat(found.getContent()).isEmpty();
  }

  @Test
  void findBooksByCriteria_WithMultipleCriteria_ShouldFilterCorrectly() {
    Page<Book> found =
        bookRepository.findBooksByCriteria(null, null, "FANTASÍA", "Janés", Pageable.unpaged());
    assertThat(found.getContent())
        .hasSize(2)
        .extracting(Book::getTitle)
        .containsExactlyInAnyOrder("Canción de Hielo y Fuego (Colección)", "El nombre del viento");
    Page<Book> otherFound =
        bookRepository.findBooksByCriteria("viento", null, "FANTASÍA", "Janés", Pageable.unpaged());
    assertThat(otherFound.getContent()).hasSize(1);
    Page<Book> otherFound2 =
        bookRepository.findBooksByCriteria(
            "viento", "patricK", "FANTASÍA", "Janés", Pageable.unpaged());
    assertThat(otherFound2.getContent()).hasSize(1);
  }

  @Test
  void findBooksByCriteria_WithNoCriteria_ShouldReturnAll() {
    Page<Book> found =
        bookRepository.findBooksByCriteria(null, null, null, null, Pageable.unpaged());
    assertThat(found.getContent()).hasSize(3);
  }

  @Test
  void findBooksByCriteria_Paged_ShouldLimitResults() {
    Pageable pageable = PageRequest.of(0, 1);

    Page<Book> found = bookRepository.findBooksByCriteria(null, null, null, null, pageable);

    assertThat(found.getContent()).hasSize(1);
    assertThat(found.getTotalElements()).isEqualTo(3);
    assertThat(found.getTotalPages()).isEqualTo(3);
    assertThat(found.isLast()).isFalse();
  }

  @Test
  void findBooksByCriteria_Paged_ShouldNavigateToLastPage() {
    Pageable pageable = PageRequest.of(2, 1);

    Page<Book> found = bookRepository.findBooksByCriteria(null, null, null, null, pageable);

    assertThat(found.getContent()).isNotEmpty();
    assertThat(found.getNumber()).isEqualTo(2);
    assertThat(found.isLast()).isTrue();
  }

  @Test
  void findBooksByCriteria_Paged_AndSorted_ShouldOrderResults() {
    Pageable pageable = PageRequest.of(0, 10, Sort.by("title").descending());

    Page<Book> found = bookRepository.findBooksByCriteria(null, null, null, null, pageable);

    assertThat(found.getContent())
        .extracting(Book::getTitle)
        .containsExactly(book2.getTitle(), book3.getTitle(), book1.getTitle());
  }

  @Test
  void findBooksByCriteria_Paged_WithFilters_ShouldCountOnlyMatchingBooks() {
    Book distractorBook = new Book();
    distractorBook.setIsbn("1111111111111");
    distractorBook.setTitle("Libro de Cocina");
    distractorBook.setAuthors("Chef X");
    distractorBook.setPrice(new BigDecimal("10.00"));
    distractorBook.setGenre("Cocina");
    distractorBook.setPublisher("Editorial X");
    distractorBook.setDescription("Recetas...");
    distractorBook.setImageUrl("http://image.url");
    entityManager.persist(distractorBook);
    entityManager.flush();

    Pageable pageable = PageRequest.of(0, 2);
    Page<Book> found = bookRepository.findBooksByCriteria(null, null, "FANTASÍA", null, pageable);

    assertThat(found.getTotalElements()).isEqualTo(3);
    assertThat(found.getContent()).hasSize(2);
    assertThat(found.isLast()).isFalse();
    assertThat(found.getContent()).extracting(Book::getGenre).containsOnly("Fantasía");
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
  void findByIsbnIncludingDeleted_WhenBookNotExists_ShouldReturnEmptyOptional() {
    Optional<Book> found = bookRepository.findByIsbnIncludingDeleted("0000000000000");
    assertThat(found).isEmpty();
  }

  @Test
  void countAllIncludingDeleted_WhenNotExistsDeletedBooks_ShouldReturnThree() {
    assertThat(bookRepository.countAllIncludingDeleted()).isEqualTo(3);
  }

  @Test
  void countAllIncludingDeleted_WhenOneBookIsSoftDeleted_ShouldStillReturnThree() {
    book3.setDeleted(true);
    bookRepository.save(book3);
    entityManager.flush();

    assertThat(bookRepository.countAllIncludingDeleted()).isEqualTo(3);
  }

  @Test
  void count_WhenOneBookIsSoftDeleted_ShouldReturnTwo() {
    book3.setDeleted(true);
    bookRepository.save(book3);
    entityManager.flush();
    entityManager.clear();

    assertThat(bookRepository.count()).isEqualTo(2);
  }

  @Test
  void countAllIncludingDeleted_WhenDatabaseIsEmpty_ShouldReturnZero() {
    entityManager.getEntityManager().createQuery("DELETE FROM Book").executeUpdate();
    entityManager.flush();

    assertThat(bookRepository.countAllIncludingDeleted()).isZero();
  }
}
