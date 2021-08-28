package uk.co.hadoopathome.intellij.viewer.fileformat.int96;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.parquet.io.api.Binary;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ParquetTimestampUtilsTest {

  @Test
  @DisplayName("Assert that INT96 to timestamp conversion works correctly")
  public void testInt96Convert() {
    byte[] bytes = new byte[] {0, 42, -23, 108, -14, 25, 0, 0, 78, 127, 37, 0};
    Binary tsValue = Binary.fromReusedByteArray(bytes);
    long timestampMillis = ParquetTimestampUtils.getTimestampMillis(tsValue);
    assertThat(timestampMillis).isEqualTo(1454486129000L);
  }
}
