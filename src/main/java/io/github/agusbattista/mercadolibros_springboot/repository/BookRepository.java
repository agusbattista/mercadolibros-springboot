package io.github.agusbattista.mercadolibros_springboot.repository;

import io.github.agusbattista.mercadolibros_springboot.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {}
