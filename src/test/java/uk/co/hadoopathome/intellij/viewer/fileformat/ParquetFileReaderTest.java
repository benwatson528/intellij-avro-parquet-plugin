package uk.co.hadoopathome.intellij.viewer.fileformat;

import static org.assertj.core.api.Assertions.assertThat;

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
  private static final String INVALID_PARQUET_FILE = "parquet/int96_column.parquet";
  private static final String LOGICAL_DATE_PARQUET_FILE = "parquet/logical_date.parquet";

  @Test
  @DisplayName("Assert that a schema can be extracted from a Parquet file")
  public void testGetSchema() throws IOException {
    File file = new File(getClass().getClassLoader().getResource(NESTED_PARQUET_FILE).getFile());
    Reader parquetReader = new ParquetFileReader(file);
    String schema = parquetReader.getSchema();
    assertThat(schema).contains("\"type\" : [ \"int\", \"null\" ]");
  }

  @Test
  @DisplayName("Assert that one record can be extracted from a Parquet file")
  public void testGetRecords() throws IOException {
    List<String> records = readRecords(NESTED_PARQUET_FILE, 10);
    assertThat(records).hasSize(6);
    String firstRecord = records.get(0);
    assertThat(firstRecord)
        .contains(
            "\"nested2_union\": {\"nested2_string\": \"yobzdadkgk\", \"nested2_int\":"
                + " 1026040670}");
  }

  @Test
  @DisplayName("Assert that all records can be extracted from a Parquet file")
  public void testGetAllRecords() throws IOException {
    List<String> records = readRecords(NESTED_PARQUET_FILE, 99999);
    assertThat(records).hasSize(6);
    String firstRecord = records.get(0);
    assertThat(firstRecord)
        .contains(
            "\"nested2_union\": {\"nested2_string\": \"yobzdadkgk\", \"nested2_int\":"
                + " 1026040670}");
  }

  @Test
  @DisplayName("Assert that a Parquet file with complex nesting is correctly parsed")
  public void testList() throws IOException {
    List<String> records = readRecords(LIST_PARQUET_FILE, 10);
    assertThat(records).hasSize(1);
    String firstRecord = records.get(0);
    assertThat(firstRecord).contains("[{\"element\": 42}, {\"element\": 47}, {\"element\": 139}]");
  }

  @Test
  @DisplayName("Assert that a Parquet file with an INT96 column can still be displayed")
  public void testInvalidFile() throws IOException {
    List<String> records = readRecords(INVALID_PARQUET_FILE, 10);
    assertThat(records).hasSize(10);
    String firstRecord = records.get(0);
    assertThat(firstRecord).contains("\"first_name\": \"Amanda\", \"last_name\": \"Jordan\"");
  }

  @Test
  @DisplayName("Assert that a Parquet file with a LogicalType date column can still be displayed")
  public void testDateLogicalType() throws IOException {
    List<String> records = readRecords(LOGICAL_DATE_PARQUET_FILE, 10);
    assertThat(records).hasSize(5);
    String firstRecord = records.get(0);
    assertThat(firstRecord)
        .contains("{\"received_at\": 1970-01-19T12:02:37.304Z, \"name___string\": \"Tressa\"");
  }

  private List<String> readRecords(String fileName, int numRecords) throws IOException {
    File file = new File(getClass().getClassLoader().getResource(fileName).getFile());
    Reader parquetReader = new ParquetFileReader(file);
    return parquetReader.getRecords(numRecords);
  }
}
