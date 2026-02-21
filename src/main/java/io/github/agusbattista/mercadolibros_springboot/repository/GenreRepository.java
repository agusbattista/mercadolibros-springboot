package io.github.agusbattista.mercadolibros_springboot.repository;

import io.github.agusbattista.mercadolibros_springboot.model.Genre;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface GenreRepository extends JpaRepository<Genre, Long> {

  Optional<Genre> findByCode(String code);

  Optional<Genre> findByName(String name);

  @Query(value = "SELECT * FROM genres WHERE code = :code", nativeQuery = true)
  Optional<Genre> findByCodeIncludingDeleted(@Param("code") String code);

  @Query(value = "SELECT COUNT(*) FROM genres", nativeQuery = true)
  long countAllIncludingDeleted();
}
