package io.github.agusbattista.mercadolibros_springboot.dto;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.lang.NonNull;

public record PagedResponse<T>(
    List<T> content,
    int page,
    int size,
    long totalElements,
    int totalPages,
    boolean last,
    Map<String, String> sort) {

  public static <T> PagedResponse<T> from(Page<T> page) {
    Map<String, String> sortInfo = createSortMap(page);
    return new PagedResponse<>(
        page.getContent(),
        page.getNumber(),
        page.getSize(),
        page.getTotalElements(),
        page.getTotalPages(),
        page.isLast(),
        sortInfo);
  }

  private static <T> @NonNull Map<String, String> createSortMap(Page<T> page) {
    Map<String, String> sortInfo = new LinkedHashMap<>();
    if (page.getSort().isUnsorted()) {
      sortInfo.put("sorted", "NONE");
    } else {
      sortInfo =
          page.getSort().stream()
              .collect(
                  Collectors.toMap(
                      Sort.Order::getProperty,
                      order -> order.getDirection().name(),
                      (oldValue, newValue) -> oldValue,
                      LinkedHashMap::new));
    }
    return sortInfo;
  }
}
