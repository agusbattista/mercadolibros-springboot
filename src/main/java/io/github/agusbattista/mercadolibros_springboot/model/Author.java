package io.github.agusbattista.mercadolibros_springboot.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "authors")
@SQLDelete(sql = "UPDATE authors SET deleted = true WHERE id = ?")
@SQLRestriction("deleted = false")
@Getter
@Setter
@NoArgsConstructor
public class Author {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @UuidGenerator(style = UuidGenerator.Style.RANDOM)
  @JdbcTypeCode(SqlTypes.VARCHAR)
  @Column(unique = true, nullable = false, updatable = false)
  private UUID uuid;

  /*
   * Indica si el autor ha sido eliminado lógicamente.
   * Los autores eliminados no aparecen en consultas normales.
   */
  @Column(nullable = false)
  private boolean deleted = false;

  @Column(nullable = false)
  private String fullName;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String biography;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Author author)) return false;
    return this.getUuid() != null && this.getUuid().equals(author.getUuid());
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}
