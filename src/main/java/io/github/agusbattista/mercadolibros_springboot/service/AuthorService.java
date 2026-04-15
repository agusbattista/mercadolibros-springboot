package io.github.agusbattista.mercadolibros_springboot.service;

import io.github.agusbattista.mercadolibros_springboot.dto.AuthorRequestDTO;
import io.github.agusbattista.mercadolibros_springboot.dto.AuthorResponseDTO;
import io.github.agusbattista.mercadolibros_springboot.dto.PagedResponse;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;

public interface AuthorService {

  PagedResponse<AuthorResponseDTO> findAll(Pageable pageable);

  Optional<AuthorResponseDTO> findByUuid(UUID uuid);

  PagedResponse<AuthorResponseDTO> findByName(String name, Pageable pageable);

  AuthorResponseDTO create(AuthorRequestDTO author);

  void deleteByUuid(UUID uuid);

  AuthorResponseDTO update(UUID uuid, AuthorRequestDTO author);

  AuthorResponseDTO restore(UUID uuid);
}
