package io.github.agusbattista.mercadolibros_springboot.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

class PagedResponseTest {

  @Test
  void from_WhenPageIsUnsorted_ShouldMapCorrectlyWithNoneSort() {
    List<String> content = List.of("Item A", "Item B");
    Pageable pageable = PageRequest.of(0, 5);
    Page<String> page = new PageImpl<>(content, pageable, 2);

    PagedResponse<String> response = PagedResponse.from(page);

    assertThat(response.content()).isEqualTo(content);
    assertThat(response.page()).isZero();
    assertThat(response.size()).isEqualTo(5);
    assertThat(response.totalElements()).isEqualTo(2);
    assertThat(response.sort()).hasSize(1).containsEntry("sorted", "NONE");
  }

  @Test
  void from_WhenPageIsSorted_ShouldMapSortCorrectly() {
    List<String> content = List.of("Item A");
    Pageable pageable = PageRequest.of(0, 5, Sort.by("price").descending());
    Page<String> page = new PageImpl<>(content, pageable, 1);

    PagedResponse<String> response = PagedResponse.from(page);

    assertThat(response.sort()).hasSize(1).containsEntry("price", "DESC");
  }

  @Test
  void from_WhenPageHasMultipleSorts_ShouldMapAllInOrder() {
    List<String> content = List.of("Item A");
    Sort multiSort = Sort.by("title").ascending().and(Sort.by("price").descending());
    Pageable pageable = PageRequest.of(0, 5, multiSort);
    Page<String> page = new PageImpl<>(content, pageable, 1);

    PagedResponse<String> response = PagedResponse.from(page);

    assertThat(response.sort())
        .hasSize(2)
        .containsEntry("title", "ASC")
        .containsEntry("price", "DESC");
    assertThat(response.sort().keySet()).containsExactly("title", "price");
  }
}
