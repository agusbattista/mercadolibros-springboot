package io.github.agusbattista.mercadolibros_springboot.service;

import io.github.agusbattista.mercadolibros_springboot.dto.GenreRequestDTO;
import io.github.agusbattista.mercadolibros_springboot.dto.GenreResponseDTO;
import io.github.agusbattista.mercadolibros_springboot.dto.PagedResponse;
import java.util.Optional;
import org.springframework.data.domain.Pageable;

public interface GenreService {

  PagedResponse<GenreResponseDTO> findAll(Pageable pageable);

  Optional<GenreResponseDTO> findById(Long id);

  Optional<GenreResponseDTO> findByCode(String code);

  Optional<GenreResponseDTO> findByName(String name);

  GenreResponseDTO create(GenreRequestDTO genre);

  void deleteById(Long id);

  GenreResponseDTO update(Long id, GenreRequestDTO genre);
}
