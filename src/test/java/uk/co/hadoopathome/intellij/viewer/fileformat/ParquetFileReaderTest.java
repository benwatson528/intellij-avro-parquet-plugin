package uk.co.hadoopathome.intellij.viewer.fileformat;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ParquetFileReaderTest {

  // Converted from
  // https://github.com/apache/incubator-gobblin/blob/master/gobblin-core/src/test/resources/converter/pickfields_nested_with_union.avro
  private static final String NESTED_PARQUET_FILE = "parquet/nested.parquet";
  private static final String LIST_PARQUET_FILE = "parquet/list.parquet";
  // https://github.com/Teradata/kylo/blob/master/samples/sample-data/parquet/userdata1.parquet
  private static final String INVALID_PARQUET_FILE = "parquet/invalid.parquet";

  @Test
  @DisplayName("Assert that a schema can be extracted from a Parquet file")
  public void testGetSchema() throws IOException {
    File file = new File(getClass().getClassLoader().getResource(NESTED_PARQUET_FILE).getFile());
    Reader parquetReader = new ParquetFileReader(file);
    String schema = parquetReader.getSchema();
    assertTrue(schema.contains("\"type\" : [ \"int\", \"null\" ]"));
  }

  @Test
  @DisplayName("Assert that one record can be extracted from a Parquet file")
  public void testGetRecords() throws IOException {
    File file = new File(getClass().getClassLoader().getResource(NESTED_PARQUET_FILE).getFile());
    Reader parquetReader = new ParquetFileReader(file);
    List<String> records = parquetReader.getRecords(10);
    assertEquals(6, records.size());
    String firstRecord = records.get(0);
    assertTrue(
        firstRecord.contains(
            "\"nested2_union\": {\"nested2_string\": \"yobzdadkgk\", \"nested2_int\": 1026040670}"));
  }

  @Test
  @DisplayName("Assert that all records can be extracted from a Parquet file")
  public void testGetAllRecords() throws IOException {
    File file = new File(getClass().getClassLoader().getResource(NESTED_PARQUET_FILE).getFile());
    Reader parquetReader = new ParquetFileReader(file);
    List<String> records = parquetReader.getRecords(99999);
    assertEquals(6, records.size());
    String firstRecord = records.get(0);
    assertTrue(
        firstRecord.contains(
            "\"nested2_union\": {\"nested2_string\": \"yobzdadkgk\", \"nested2_int\": 1026040670}"));
  }

  @Test
  @DisplayName("Assert that a Parquet file with complex nesting is correctly parsed")
  public void testList() throws IOException {
    File file = new File(getClass().getClassLoader().getResource(LIST_PARQUET_FILE).getFile());
    Reader parquetReader = new ParquetFileReader(file);
    List<String> records = parquetReader.getRecords(10);
    assertEquals(1, records.size());
    String firstRecord = records.get(0);
    assertTrue(firstRecord.contains("[{\"element\": 42}, {\"element\": 47}, {\"element\": 139}]"));
  }

  @Test
  @DisplayName("Assert that an invalid Parquet file throws an exception")
  public void testInvalidFile() throws IOException {
    File file = new File(getClass().getClassLoader().getResource(INVALID_PARQUET_FILE).getFile());
    Reader parquetReader = new ParquetFileReader(file);
    assertThrows(IllegalArgumentException.class, () -> parquetReader.getRecords(10));
  }
}
