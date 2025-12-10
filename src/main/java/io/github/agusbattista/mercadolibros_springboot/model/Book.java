package io.github.agusbattista.mercadolibros_springboot.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.validator.constraints.ISBN;
import org.hibernate.validator.constraints.URL;

@Entity
@Table(name = "books")
@SQLDelete(sql = "UPDATE books SET deleted = true WHERE id = ?")
@SQLRestriction("deleted = false")
public class Book {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(unique = true, nullable = false)
  @NotNull(message = "El ISBN es obligatorio")
  // type = ANY acepta ISBN-10 y ISBN-13
  @ISBN(type = ISBN.Type.ANY, message = "El formato del ISBN no es válido")
  private String isbn;

  /*
   * Indica si el libro ha sido eliminado lógicamente.
   * Los libros eliminados no aparecen en consultas normales.
   */
  @Column(nullable = false)
  private boolean deleted = false;

  @NotBlank(message = "El título es obligatorio")
  @Size(max = 255, message = "El título no puede exceder los 255 caracteres")
  @Column(nullable = false, length = 255)
  private String title;

  @NotBlank(message = "El autor es obligatorio")
  @Size(
      max = 255,
      message = "El nombre del autor o de los autores no puede exceder los 255 caracteres")
  @Column(nullable = false, length = 255)
  private String authors;

  @Column(nullable = false, precision = 10, scale = 2)
  @NotNull(message = "El precio es obligatorio")
  @PositiveOrZero(message = "El precio debe ser mayor o igual a cero")
  @Digits(
      integer = 8,
      fraction = 2,
      message = "El precio debe tener formato monetario correcto. Ejemplo: 100.00")
  private BigDecimal price;

  @NotBlank(message = "La descripción es obligatoria")
  @Size(max = 5000, message = "La descripción no puede superar los 5000 caracteres")
  @Column(columnDefinition = "TEXT")
  private String description;

  @NotBlank(message = "La editorial es obligatoria")
  @Size(max = 255, message = "La editorial no puede superar los 255 caracteres")
  @Column(nullable = false, length = 255)
  private String publisher;

  @NotBlank(message = "El género es obligatorio")
  @Size(max = 100, message = "El género no puede superar los 100 caracteres")
  @Column(nullable = false, length = 100)
  private String genre;

  @NotBlank(message = "La URL de la imagen es obligatoria")
  @URL(message = "La URL de la imagen debe ser válida")
  @Size(max = 500, message = "La URL de la imagen no puede superar los 500 caracteres")
  @Column(nullable = false, length = 500)
  private String imageUrl;

  public Book() {
    /* Jackson necesita el constructor vacío */
  }

  public String getIsbn() {
    return isbn;
  }

  public void setIsbn(String isbn) {
    this.isbn = isbn;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getAuthors() {
    return authors;
  }

  public void setAuthors(String authors) {
    this.authors = authors;
  }

  public BigDecimal getPrice() {
    return price;
  }

  public void setPrice(BigDecimal price) {
    this.price = price;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getPublisher() {
    return publisher;
  }

  public void setPublisher(String publisher) {
    this.publisher = publisher;
  }

  public String getGenre() {
    return genre;
  }

  public void setGenre(String genre) {
    this.genre = genre;
  }

  public String getImageUrl() {
    return imageUrl;
  }

  public void setImageUrl(String imageUrl) {
    this.imageUrl = imageUrl;
  }

  @JsonIgnore
  public boolean isDeleted() {
    return deleted;
  }

  public void setDeleted(boolean deleted) {
    this.deleted = deleted;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Book book)) return false;
    return this.getIsbn() != null && this.getIsbn().equals(book.getIsbn());
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}
