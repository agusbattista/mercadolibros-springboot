package io.github.agusbattista.mercadolibros_springboot.utils;

import java.text.Normalizer;

public class StringFormatter {

  private StringFormatter() {}

  private static String toLowerCaseAndTrim(String text) {
    return text.trim().toLowerCase();
  }

  private static String toUpperCaseAndTrim(String text) {
    return text.trim().toUpperCase();
  }

  private static String[] splitString(String text) {
    return text.split("\\s+");
  }

  private static String buildResult(String[] words) {
    StringBuilder result = new StringBuilder();
    for (int i = 0; i < words.length; i++) {
      String word = words[i];
      if (!word.isBlank()) {
        String upperCaseFirstCharacterWord = word.substring(0, 1).toUpperCase() + word.substring(1);
        result.append(upperCaseFirstCharacterWord);
      }
      if (i < words.length - 1) {
        result.append(" ");
      }
    }
    return result.toString();
  }

  public static String formatName(String text) {
    if (text == null || text.isBlank()) return "";
    String lowercaseAndTrimmed = toLowerCaseAndTrim(text);
    String[] words = splitString(lowercaseAndTrimmed);
    return buildResult(words);
  }

  public static String generateCode(String text) {
    if (text == null || text.isBlank()) return "";
    String uppercaseAndTrimmed = toUpperCaseAndTrim(text);
    String normalized = Normalizer.normalize(uppercaseAndTrimmed, Normalizer.Form.NFD);
    String withoutAccents = normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    String snakeCase = withoutAccents.replaceAll("[^A-Z0-9]+", "_");
    return snakeCase.replaceAll("(^_+)|(_+$)", "");
  }
}
