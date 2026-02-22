package io.github.agusbattista.mercadolibros_springboot.exception;

public class ResourceInUseException extends RuntimeException {

  public ResourceInUseException(String message) {
    super(message);
  }
}
