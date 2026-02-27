package io.github.agusbattista.mercadolibros_springboot.utils;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class StringFormatterTest {

  @Test
  void formatName_WhenStringIsNull_ShouldReturnEmptyString() {
    assertThat(StringFormatter.formatName(null)).isEmpty();
  }

  @Test
  void formatName_WhenStringIsEmpty_ShouldReturnEmptyString() {
    assertThat(StringFormatter.formatName("")).isEmpty();
  }

  @Test
  void formatName_WhenStringHasOnlySpaces_ShouldReturnEmptyString() {
    assertThat(StringFormatter.formatName("   ")).isEmpty();
  }

  @Test
  void formatName_WhenStringIsSingleWordWithSpaces_ShouldReturnFormattedWord() {
    assertThat(StringFormatter.formatName("  fantasía  ")).isEqualTo("Fantasía");
  }

  @Test
  void formatName_WhenStringIsAlreadyFormatted_ShouldReturnSameString() {
    assertThat(StringFormatter.formatName("Ciencia Ficción")).isEqualTo("Ciencia Ficción");
  }

  @Test
  void formatName_WhenStringHasTabs_ShouldReturnFormattedString() {
    assertThat(StringFormatter.formatName("\t ciencia \t ficción \t")).isEqualTo("Ciencia Ficción");
  }

  @Test
  void formatName_WhenStringHasMixedCase_ShouldReturnFormattedString() {
    assertThat(StringFormatter.formatName("  cienCIa       FICCIÓN  "))
        .isEqualTo("Ciencia Ficción");
  }

  @Test
  void generateCode_WhenStringIsNull_ShouldReturnEmptyString() {
    assertThat(StringFormatter.generateCode(null)).isEmpty();
  }

  @Test
  void generateCode_WhenStringIsEmpty_ShouldReturnEmptyString() {
    assertThat(StringFormatter.generateCode("")).isEmpty();
  }

  @Test
  void generateCode_WhenStringHasOnlySpaces_ShouldReturnEmptyString() {
    assertThat(StringFormatter.generateCode("   ")).isEmpty();
  }

  @Test
  void generateCode_WhenStringIsSingleWordWithSpaces_ShouldReturnFormattedCode() {
    assertThat(StringFormatter.generateCode("  fantasía  ")).isEqualTo("FANTASIA");
  }

  @Test
  void generateCode_WhenStringIsAlreadyFormatted_ShouldReturnSameString() {
    assertThat(StringFormatter.generateCode("CIENCIA_FICCION")).isEqualTo("CIENCIA_FICCION");
  }

  @Test
  void generateCode_WhenStringHasTabs_ShouldReturnFormattedCode() {
    assertThat(StringFormatter.generateCode("\t ciencia \t ficción \t"))
        .isEqualTo("CIENCIA_FICCION");
  }

  @Test
  void generateCode_WhenStringHasMixedCase_ShouldReturnFormattedCode() {
    assertThat(StringFormatter.generateCode("  cienCIa       FICCIÓN  "))
        .isEqualTo("CIENCIA_FICCION");
  }

  @Test
  void generateCode_WhenStringHasSpecialCharacters_ShouldReplaceWithUnderscores() {
    assertThat(StringFormatter.generateCode("Auto-ayuda & Motivación!"))
        .isEqualTo("AUTO_AYUDA_MOTIVACION");
  }
}
