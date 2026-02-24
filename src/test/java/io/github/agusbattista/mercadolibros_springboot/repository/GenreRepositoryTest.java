package io.github.agusbattista.mercadolibros_springboot.repository;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.agusbattista.mercadolibros_springboot.model.Genre;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

@DataJpaTest
class GenreRepositoryTest {

  @Autowired private GenreRepository genreRepository;

  @Autowired private TestEntityManager entityManager;

  private Genre genre1;
  private Genre genre2;
  private Genre genre3;

  @BeforeEach
  void setUp() {
    genre1 = new Genre();
    genre1.setName("Tecnología");
    genre1.setCode("TECNOLOGIA");
    entityManager.persist(genre1);

    genre2 = new Genre();
    genre2.setName("Fantasía");
    genre2.setCode("FANTASIA");
    entityManager.persist(genre2);

    genre3 = new Genre();
    genre3.setName("Ciencia Ficción");
    genre3.setCode("CIENCIA_FICCION");
    entityManager.persist(genre3);

    entityManager.flush();
  }

  @Test
  void findByCodeIncludingDeleted_WhenGenreIsDeleted_ShouldReturnGenre() {
    genre1.setDeleted(true);
    entityManager.merge(genre1);
    entityManager.flush();

    Optional<Genre> found = genreRepository.findByCodeIncludingDeleted(genre1.getCode());

    assertThat(found).isPresent();
    assertThat(found.get().getCode()).isEqualTo(genre1.getCode());
    assertThat(found.get().isDeleted()).isTrue();
  }

  @Test
  void findByCodeIncludingDeleted_WhenGenreExistsAndNotDeleted_ShouldReturnGenre() {
    Optional<Genre> found = genreRepository.findByCodeIncludingDeleted(genre1.getCode());

    assertThat(found).isPresent();
    assertThat(found.get().getCode()).isEqualTo(genre1.getCode());
    assertThat(found.get().isDeleted()).isFalse();
  }

  @Test
  void findByCodeIncludingDeleted_WhenGenreNotExists_ShouldReturnEmptyOptional() {
    Optional<Genre> found = genreRepository.findByCodeIncludingDeleted("NOT_EXISTING_CODE");
    assertThat(found).isEmpty();
  }

  @Test
  void countAllIncludingDeleted_WhenNotExistsDeletedGenres_ShouldReturnThree() {
    long count = genreRepository.countAllIncludingDeleted();

    assertThat(count).isEqualTo(3);
  }

  @Test
  void countAllIncludingDeleted_WhenOneGenreIsSoftDeleted_ShouldReturnThree() {
    genre1.setDeleted(true);
    entityManager.merge(genre1);
    entityManager.flush();

    long count = genreRepository.countAllIncludingDeleted();

    assertThat(count).isEqualTo(3);
  }

  @Test
  void countAllIncludingDeleted_WhenNoGenres_ShouldReturnZero() {
    entityManager.getEntityManager().createQuery("DELETE FROM Genre").executeUpdate();
    entityManager.flush();

    long count = genreRepository.countAllIncludingDeleted();

    assertThat(count).isZero();
  }
}
