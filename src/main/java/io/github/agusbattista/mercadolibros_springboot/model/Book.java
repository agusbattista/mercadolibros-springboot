package io.github.agusbattista.mercadolibros_springboot.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.hibernate.validator.constraints.URL;

@Entity
@Table(name = "books")
public class Book {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(unique = true, nullable = false)
  @NotNull(message = "El ISBN no puede ser nulo")
  private String isbn;

  @NotBlank private String title;
  private String authors;
  @Positive private double price;
  private String description;
  private String publisher;
  private String genre;
  @URL private String imageUrl;

  public Book() {}

  public Book(
      String isbn,
      String title,
      String authors,
      double price,
      String description,
      String publisher,
      String genre,
      String imageUrl) {
    this.isbn = isbn;
    this.title = title;
    this.authors = authors;
    this.price = price;
    this.description = description;
    this.publisher = publisher;
    this.genre = genre;
    this.imageUrl = imageUrl;
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

  public double getPrice() {
    return price;
  }

  public void setPrice(double price) {
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
}
