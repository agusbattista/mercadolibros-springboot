package io.github.agusbattista.mercadolibros_springboot.repository;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.agusbattista.mercadolibros_springboot.model.Author;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

@DataJpaTest
class AuthorRepositoryTest {

  @Autowired private AuthorRepository authorRepository;

  @Autowired private TestEntityManager entityManager;

  private Author author1;
  private Author author2;
  private Author author3;

  @BeforeEach
  void setUp() {
    author1 = new Author();
    author1.setFullName("Gabriel García Márquez");
    author1.setBiography("Premio Nobel de Literatura en 1982. Colombiano y escritor.");
    author1 = entityManager.persist(author1);

    author2 = new Author();
    author2.setFullName("Julio Cortázar");
    author2.setBiography("Escritor argentino, uno de los autores más innovadores de su tiempo.");
    author2 = entityManager.persist(author2);

    author3 = new Author();
    author3.setFullName("Jorge Luis Borges");
    author3.setBiography("Escritor, poeta, ensayista y traductor argentino.");
    author3 = entityManager.persist(author3);

    entityManager.flush();
  }

  @Test
  void findByUuidIncludingDeleted_WhenAuthorIsDeleted_ShouldReturnAuthor() {
    author1.setDeleted(true);
    entityManager.merge(author1);
    entityManager.flush();

    Optional<Author> found =
        authorRepository.findByUuidIncludingDeleted(author1.getUuid().toString());

    assertThat(found).isPresent();
    assertThat(found.get().getUuid()).isEqualTo(author1.getUuid());
    assertThat(found.get().isDeleted()).isTrue();
  }

  @Test
  void findByUuidIncludingDeleted_WhenAuthorExistsAndNotDeleted_ShouldReturnAuthor() {
    Optional<Author> found =
        authorRepository.findByUuidIncludingDeleted(author1.getUuid().toString());

    assertThat(found).isPresent();
    assertThat(found.get().getUuid()).isEqualTo(author1.getUuid());
    assertThat(found.get().isDeleted()).isFalse();
  }

  @Test
  void findByUuidIncludingDeleted_WhenAuthorNotExists_ShouldReturnEmptyOptional() {
    Optional<Author> found =
        authorRepository.findByUuidIncludingDeleted(UUID.randomUUID().toString());
    assertThat(found).isEmpty();
  }

  @Test
  void countAllIncludingDeleted_WhenNotExistsDeletedAuthors_ShouldReturnThree() {
    long count = authorRepository.countAllIncludingDeleted();

    assertThat(count).isEqualTo(3);
  }

  @Test
  void countAllIncludingDeleted_WhenOneAuthorIsSoftDeleted_ShouldReturnThree() {
    author1.setDeleted(true);
    entityManager.merge(author1);
    entityManager.flush();

    long count = authorRepository.countAllIncludingDeleted();

    assertThat(count).isEqualTo(3);
  }

  @Test
  void countAllIncludingDeleted_WhenNoAuthors_ShouldReturnZero() {
    entityManager.getEntityManager().createQuery("DELETE FROM Author").executeUpdate();
    entityManager.flush();

    long count = authorRepository.countAllIncludingDeleted();

    assertThat(count).isZero();
  }
}
