package uk.co.hadoopathome.intellij.viewer.fileformat;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.List;
import org.junit.Test;

public class ParquetFileReaderTest {

  // Converted from
  // https://github.com/apache/incubator-gobblin/blob/master/gobblin-core/src/test/resources/converter/pickfields_nested_with_union.avro
  private static final String NESTED_PARQUET_FILE = "parquet/nested.parquet";
  private static final String LIST_PARQUET_FILE = "parquet/list.parquet";
  // https://github.com/Teradata/kylo/blob/master/samples/sample-data/parquet/userdata1.parquet
  private static final String INVALID_PARQUET_FILE = "parquet/invalid.parquet";

  @Test
  public void testGetSchema() throws IOException {
    File file = new File(getClass().getClassLoader().getResource(NESTED_PARQUET_FILE).getFile());
    Reader parquetReader = new ParquetFileReader(file);
    String schema = parquetReader.getSchema();
    assertTrue(schema.contains("\"type\" : [ \"int\", \"null\" ]"));
  }

  @Test
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
  public void testList() throws IOException {
    File file = new File(getClass().getClassLoader().getResource(LIST_PARQUET_FILE).getFile());
    Reader parquetReader = new ParquetFileReader(file);
    List<String> records = parquetReader.getRecords(10);
    assertEquals(1, records.size());
    String firstRecord = records.get(0);
    assertTrue(firstRecord.contains("[{\"element\": 42}, {\"element\": 47}, {\"element\": 139}]"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidFile() throws IOException {
    File file = new File(getClass().getClassLoader().getResource(INVALID_PARQUET_FILE).getFile());
    Reader parquetReader = new ParquetFileReader(file);
    parquetReader.getRecords(10);
  }
}
