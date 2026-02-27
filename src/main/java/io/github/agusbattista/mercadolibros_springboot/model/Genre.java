package io.github.agusbattista.mercadolibros_springboot.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "genres")
@SQLDelete(sql = "UPDATE genres SET deleted = true WHERE id = ?")
@SQLRestriction("deleted = false")
public class Genre {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(unique = true, nullable = false, length = 100)
  private String code;

  @Column(unique = true, nullable = false, length = 100)
  private String name;

  /*
   * Indica si el género ha sido eliminado lógicamente.
   * Los géneros eliminados no aparecen en consultas normales.
   */
  @Column(nullable = false)
  private boolean deleted = false;

  public Genre() {
    /* Requerido por JPA/Hibernate */
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public boolean isDeleted() {
    return deleted;
  }

  public void setDeleted(boolean deleted) {
    this.deleted = deleted;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Genre genre)) return false;
    return this.getCode() != null && this.getCode().equals(genre.getCode());
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}
