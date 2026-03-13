package io.github.agusbattista.mercadolibros_springboot.utils;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.stream.Collectors;

public class StringFormatter {

  private StringFormatter() {}

  public static String formatName(String text) {
    if (text == null || text.isBlank()) return "";
    return Arrays.stream(text.trim().toLowerCase().split("\\s+"))
        .filter(word -> !word.isBlank())
        .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1))
        .collect(Collectors.joining(" "));
  }

  public static String generateCode(String text) {
    if (text == null || text.isBlank()) return "";
    String uppercaseAndTrimmed = text.trim().toUpperCase();
    String normalized = Normalizer.normalize(uppercaseAndTrimmed, Normalizer.Form.NFD);
    String withoutAccents = normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    String snakeCase = withoutAccents.replaceAll("[^A-Z0-9]+", "_");
    return snakeCase.replaceAll("(^_+)|(_+$)", "");
  }
}
