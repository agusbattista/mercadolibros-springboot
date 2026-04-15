package io.github.agusbattista.mercadolibros_springboot.repository;

import io.github.agusbattista.mercadolibros_springboot.model.Author;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthorRepository extends JpaRepository<Author, Long> {

  @NonNull
  Page<Author> findAll(@NonNull Pageable pageable);

  Optional<Author> findByUuid(UUID uuid);

  Page<Author> findByFullNameContainingIgnoreCase(String fullName, Pageable pageable);

  @Query(value = "SELECT * FROM authors WHERE uuid = :uuid", nativeQuery = true)
  Optional<Author> findByUuidIncludingDeleted(@Param("uuid") String uuid);

  @Query(value = "SELECT COUNT(*) FROM authors", nativeQuery = true)
  long countAllIncludingDeleted();
}
