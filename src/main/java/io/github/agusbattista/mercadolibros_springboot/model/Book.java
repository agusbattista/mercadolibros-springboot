package io.github.agusbattista.mercadolibros_springboot.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "books")
@SQLDelete(sql = "UPDATE books SET deleted = true WHERE id = ?")
@SQLRestriction("deleted = false")
public class Book {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(unique = true, nullable = false)
  private String isbn;

  /*
   * Indica si el libro ha sido eliminado lógicamente.
   * Los libros eliminados no aparecen en consultas normales.
   */
  @Column(nullable = false)
  private boolean deleted = false;

  @Column(nullable = false, length = 255)
  private String title;

  @Column(nullable = false, length = 255)
  private String authors;

  @Column(nullable = false, precision = 10, scale = 2)
  private BigDecimal price;

  @Column(columnDefinition = "TEXT")
  private String description;

  @Column(nullable = false, length = 255)
  private String publisher;

  @Column(nullable = false, length = 100)
  private String genre;

  @Column(nullable = false, length = 500)
  private String imageUrl;

  public Book() {
    /* Jackson necesita el constructor vacío */
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
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
