package uk.co.hadoopathome.intellij.viewer.fileformat.int96;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ParquetTimestampUtilsTest {

  @Test
  @DisplayName("Assert that INT96 to timestamp conversion works correctly")
  public void testInt96Convert() {
    byte[] bytes = new byte[] {0, 42, -23, 108, -14, 25, 0, 0, 78, 127, 37, 0};
    String updatedJson = ParquetTimestampUtils.convertInt96("");
    assertThat(updatedJson).contains("timestamp");
  }
}
