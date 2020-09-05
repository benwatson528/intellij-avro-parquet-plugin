package uk.co.hadoopathome.intellij.viewer.fileformat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class AvroFileReaderTest {

  private static final String TWITTER_AVRO_FILE = "avro/twitter.avro";
  // https://github.com/apache/incubator-gobblin/blob/master/gobblin-core/src/test/resources/converter/pickfields_nested_with_union.avro
  private static final String COMPLEX_AVRO_FILE = "avro/pickfields_nested_with_union.avro";
  private static final String DECIMAL_LOGICAL_TYPE = "avro/decimal_logical_type.avro";
  private static final String INVALID_AVRO_FILE = "avro/invalid.avro";

  @Test
  @DisplayName("Assert that a schema can be extracted from an Avro file")
  public void testGetSchema() throws IOException {
    File file = new File(getClass().getClassLoader().getResource(TWITTER_AVRO_FILE).getFile());
    Reader avroReader = new AvroFileReader(file);
    String schema = avroReader.getSchema();
    assertThat(schema).contains("A basic schema for storing Twitter messages");
  }

  @Test
  @DisplayName("Assert that one record can be extracted from an Avro file")
  public void testGetRecords() throws IOException {
    List<String> records = readRecords(TWITTER_AVRO_FILE, 1);
    assertThat(records).hasSize(1);
    String firstRecord = records.get(0);
    assertThat(firstRecord).contains("Nerf paper");
  }

  @Test
  @DisplayName("Assert that all records can be extracted from an Avro file")
  public void testGetAllRecords() throws IOException {
    List<String> records = readRecords(TWITTER_AVRO_FILE, 100);
    assertThat(records).hasSize(2);
    String firstRecord = records.get(0);
    assertThat(firstRecord).contains("Nerf paper");
  }

  @Test
  @DisplayName("Assert that an Avro file with complex nesting is correctly parsed")
  public void testComplexNesting() throws IOException {
    List<String> records = readRecords(COMPLEX_AVRO_FILE, 100);
    assertThat(records).hasSize(6);
    String firstRecord = records.get(0);
    assertThat(firstRecord).contains("btnzlrfptk");
  }

  @Test
  @DisplayName("Assert that an Avro file with a decimal LogicalType is correctly parsed")
  public void testDecimalLogicalType() throws IOException {
    List<String> records = readRecords(DECIMAL_LOGICAL_TYPE, 100);
    assertThat(records).hasSize(1);
    String firstRecord = records.get(0);
    assertThat(firstRecord).contains("25.190000");
  }

  @Test
  @DisplayName("Assert that an invalid Avro file throws an exception")
  public void testInvalidFile() {
    File file = new File(getClass().getClassLoader().getResource(INVALID_AVRO_FILE).getFile());
    assertThrows(OutOfMemoryError.class, () -> new AvroFileReader(file));
  }

  private List<String> readRecords(String fileName, int numRecords) throws IOException {
    File file = new File(getClass().getClassLoader().getResource(fileName).getFile());
    Reader avroReader = new AvroFileReader(file);
    return avroReader.getRecords(numRecords);
  }
}
