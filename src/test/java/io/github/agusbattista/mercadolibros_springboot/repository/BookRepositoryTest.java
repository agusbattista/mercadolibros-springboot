package io.github.agusbattista.mercadolibros_springboot.repository;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.agusbattista.mercadolibros_springboot.model.Book;
import io.github.agusbattista.mercadolibros_springboot.model.Genre;
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
    Genre genre1 = new Genre();
    genre1.setName("Fantasía");
    genre1.setCode("FANTASIA");
    entityManager.persist(genre1);

    book1 = new Book();
    book1.setIsbn("9786073155731");
    book1.setTitle("Canción de Hielo y Fuego (Colección)");
    book1.setAuthors("George R. R. Martin");
    book1.setPrice(new BigDecimal("33.99"));
    book1.setDescription(
        "La saga completa de Canción de Hielo y Fuego, la obra maestra de la fantasía moderna.");
    book1.setPublisher("Plaza & Janés");
    book1.setGenre(genre1);
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
    book2.setGenre(genre1);
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
    book3.setGenre(genre1);
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
        bookRepository.findBooksByCriteria(null, null, "Fantasía", null, Pageable.unpaged());
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
        bookRepository.findBooksByCriteria(null, null, "Fantasía", "Janés", Pageable.unpaged());
    assertThat(found.getContent())
        .hasSize(2)
        .extracting(Book::getTitle)
        .containsExactlyInAnyOrder("Canción de Hielo y Fuego (Colección)", "El nombre del viento");
    Page<Book> otherFound =
        bookRepository.findBooksByCriteria("viento", null, "Fantasía", "Janés", Pageable.unpaged());
    assertThat(otherFound.getContent()).hasSize(1);
    Page<Book> otherFound2 =
        bookRepository.findBooksByCriteria(
            "viento", "patricK", "Fantasía", "Janés", Pageable.unpaged());
    assertThat(otherFound2.getContent()).hasSize(1);
  }

  @Test
  void findBooksByCriteria_CaseInsensitiveSearch_ShouldReturnMatchingBooks() {
    Page<Book> foundByTitle =
        bookRepository.findBooksByCriteria("hIeLo", null, null, null, Pageable.unpaged());
    assertThat(foundByTitle.getContent()).hasSize(1);

    Page<Book> foundByAuthor =
        bookRepository.findBooksByCriteria(null, "tOLkiEn", null, null, Pageable.unpaged());
    assertThat(foundByAuthor.getContent()).hasSize(1);

    Page<Book> foundByPublisher =
        bookRepository.findBooksByCriteria(null, null, null, "jANés", Pageable.unpaged());
    assertThat(foundByPublisher.getContent()).hasSize(2);
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
    Genre genre2 = new Genre();
    genre2.setName("Cocina");
    genre2.setCode("COCINA");
    entityManager.persist(genre2);

    Book distractorBook = new Book();
    distractorBook.setIsbn("1111111111111");
    distractorBook.setTitle("Libro de Cocina");
    distractorBook.setAuthors("Chef X");
    distractorBook.setPrice(new BigDecimal("10.00"));
    distractorBook.setGenre(genre2);
    distractorBook.setPublisher("Editorial X");
    distractorBook.setDescription("Recetas...");
    distractorBook.setImageUrl("http://image.url");
    entityManager.persist(distractorBook);
    entityManager.flush();

    Pageable pageable = PageRequest.of(0, 2);
    Page<Book> found = bookRepository.findBooksByCriteria(null, null, "Fantasía", null, pageable);

    assertThat(found.getTotalElements()).isEqualTo(3);
    assertThat(found.getContent()).hasSize(2);
    assertThat(found.isLast()).isFalse();
    assertThat(found.getContent()).extracting(b -> b.getGenre().getName()).containsOnly("Fantasía");
  }

  private void persistNewBook() {
    Genre genre2 = new Genre();
    genre2.setName("Ciencia Ficción");
    genre2.setCode("CIENCIA_FICCION");
    entityManager.persist(genre2);

    Book book4 = new Book();
    book4.setIsbn("9788466353779");
    book4.setTitle("Dune");
    book4.setAuthors("Frank Herbert");
    book4.setPrice(new BigDecimal("14.99"));
    book4.setDescription(
        "La mayor epopeya de ciencia ficción de todos los tiempos ambientada en Arrakis.");
    book4.setPublisher("Debolsillo");
    book4.setGenre(genre2);
    book4.setImageUrl(
        "https://books.google.com/books/publisher/content?id=uf5NEAAAQBAJ&printsec=frontcover&img=1&zoom=4&edge=curl&source=gbs_api");
    entityManager.persist(book4);
    entityManager.flush();
  }

  @Test
  void findBooksByCriteria_Paged_AndSortedByGenreName_ShouldOrderResults() {
    persistNewBook();
    Pageable pageable = PageRequest.of(0, 5, Sort.by("genre.name").ascending());

    Page<Book> found = bookRepository.findAll(pageable);

    assertThat(found.getContent().getFirst().getGenre().getName()).isEqualTo("Ciencia Ficción");
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

  private Book saveNewTechBook(Genre genre3) {
    Book book = new Book();
    book.setIsbn("9780134757599");
    book.setTitle("Refactoring");
    book.setAuthors("Martin Fowler");
    book.setPrice(new BigDecimal("57.99"));
    book.setGenre(genre3);
    book.setPublisher("Addison-Wesley Professional");
    book.setDescription(
        "Una guía definitiva sobre cómo mejorar el diseño del código existente sin cambiar su comportamiento externo.");
    book.setImageUrl(
        "https://books.google.com/books/publisher/content?id=2H1_DwAAQBAJ&printsec=frontcover&img=1&zoom=4&edge=curl&source=gbs_api");
    Book savedBook = entityManager.persist(book);
    entityManager.flush();
    return savedBook;
  }

  @Test
  void countByGenreIdIncludingDeleted_WhenDatabaseIsEmpty_ShouldReturnZero() {
    entityManager.getEntityManager().createQuery("DELETE FROM Book").executeUpdate();
    entityManager.flush();

    assertThat(bookRepository.countByGenreIdIncludingDeleted(1L)).isZero();
  }

  @Test
  void countByGenreIdIncludingDeleted_WhenNoAssociatedBooksExist_ShouldReturnZero() {
    assertThat(bookRepository.countByGenreIdIncludingDeleted(999L)).isZero();
  }

  @Test
  void countByGenreIdIncludingDeleted_WhenOneBookIsAssociated_ShouldReturnOne() {
    Genre genreTech = new Genre();
    genreTech.setCode("TECNOLOGIA");
    genreTech.setName("Tecnología");
    entityManager.persist(genreTech);
    saveNewTechBook(genreTech);

    assertThat(bookRepository.countByGenreIdIncludingDeleted(genreTech.getId())).isEqualTo(1);
  }

  @Test
  void countByGenreIdIncludingDeleted_WhenOneBookIsSoftDeletedAndAssociated_ShouldReturnOne() {
    Genre genreTech = new Genre();
    genreTech.setCode("TECNOLOGIA");
    genreTech.setName("Tecnología");
    entityManager.persist(genreTech);
    Book techBook = saveNewTechBook(genreTech);
    techBook.setDeleted(true);
    entityManager.persist(techBook);
    entityManager.flush();

    assertThat(bookRepository.countByGenreIdIncludingDeleted(genreTech.getId())).isEqualTo(1);
  }
}
