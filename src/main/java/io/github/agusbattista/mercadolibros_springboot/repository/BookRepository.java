package io.github.agusbattista.mercadolibros_springboot.repository;

import io.github.agusbattista.mercadolibros_springboot.model.Book;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

  Optional<Book> findByIsbn(String isbn);

  @Query(
      "SELECT b FROM Book b WHERE "
          + "(:title IS NULL OR LOWER(b.title) LIKE LOWER(CONCAT('%', :title, '%'))) AND "
          + "(:authors IS NULL OR LOWER(b.authors) LIKE LOWER(CONCAT('%', :authors, '%'))) AND "
          + "(:genre IS NULL OR LOWER(b.genre) = LOWER(:genre)) AND "
          + "(:publisher IS NULL OR LOWER(b.publisher) LIKE LOWER(CONCAT('%', :publisher, '%')))")
  List<Book> findBooksByCriteria(
      @Param("title") String title,
      @Param("authors") String authors,
      @Param("genre") String genre,
      @Param("publisher") String publisher);
}
