package io.github.agusbattista.mercadolibros_springboot.model;

import jakarta.persistence.*;

@Entity
@Table(name = "books")
public class Book {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // agregar validaciones @NotBlank, @Positivem etc.
  private String isbn;
  private String title;
  // reemplazar authors por lista de autores (entidad)
  private String authors;
  private double price;
  private String description;
  private String publisher;

  public Book() {}

  public Book(
      String isbn,
      String title,
      String authors,
      double price,
      String description,
      String publisher) {
    this.isbn = isbn;
    this.title = title;
    this.authors = authors;
    this.price = price;
    this.description = description;
    this.publisher = publisher;
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
}
