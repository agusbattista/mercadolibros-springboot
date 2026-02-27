package io.github.agusbattista.mercadolibros_springboot.repository;

import io.github.agusbattista.mercadolibros_springboot.model.Book;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

  @Override
  @NonNull
  @EntityGraph(attributePaths = {"genre"})
  Page<Book> findAll(@NonNull Pageable pageable);

  @EntityGraph(attributePaths = {"genre"})
  Optional<Book> findByUuid(UUID uuid);

  @EntityGraph(attributePaths = {"genre"})
  Optional<Book> findByIsbn(String isbn);

  @EntityGraph(attributePaths = {"genre"})
  @Query(
      "SELECT b FROM Book b WHERE "
          + "(:title IS NULL OR LOWER(b.title) LIKE LOWER(CONCAT('%', :title, '%'))) AND "
          + "(:authors IS NULL OR LOWER(b.authors) LIKE LOWER(CONCAT('%', :authors, '%'))) AND "
          + "(:genre IS NULL OR (b.genre.name) = (:genre)) AND "
          + "(:publisher IS NULL OR LOWER(b.publisher) LIKE LOWER(CONCAT('%', :publisher, '%')))")
  Page<Book> findBooksByCriteria(
      @Param("title") String title,
      @Param("authors") String authors,
      @Param("genre") String genre,
      @Param("publisher") String publisher,
      Pageable pageable);

  @Query(value = "SELECT * FROM books WHERE isbn = :isbn", nativeQuery = true)
  Optional<Book> findByIsbnIncludingDeleted(@Param("isbn") String isbn);

  @Query(value = "SELECT COUNT(*) FROM books", nativeQuery = true)
  long countAllIncludingDeleted();

  @Query(value = "SELECT COUNT(*) FROM books WHERE genre_id = :genreId", nativeQuery = true)
  long countByGenreIdIncludingDeleted(@Param("genreId") Long genreId);
}
