package io.github.agusbattista.mercadolibros_springboot;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.agusbattista.mercadolibros_springboot.controller.BookController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class MercadolibrosSpringbootApplicationTests {

  @Autowired private BookController controller;

  @Test
  void contextLoads() {
    assertThat(controller).isNotNull();
  }
}
