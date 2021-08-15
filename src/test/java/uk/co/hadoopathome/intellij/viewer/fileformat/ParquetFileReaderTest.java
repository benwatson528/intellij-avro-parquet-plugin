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
  private static final String LOGICAL_DECIMAL_PARQUET_FILE = "parquet/logical_decimal.parquet";

  @Test
  @DisplayName("Assert that a schema can be extracted from a Parquet file")
  public void testGetSchema() throws IOException {
    ParquetFileReader parquetFileReader = readRecords(NESTED_PARQUET_FILE);
    String schema = parquetFileReader.getSchema();
    assertThat(schema).contains("\"type\" : [ \"int\", \"null\" ]");
  }

  @Test
  @DisplayName("Assert that one record can be extracted from a Parquet file")
  public void testGetRecords() throws IOException {
    ParquetFileReader parquetFileReader = readRecords(NESTED_PARQUET_FILE);
    int totalRecords = parquetFileReader.getNumRecords();
    assertThat(totalRecords).isEqualTo(6);
    List<String> records = parquetFileReader.getRecords(10);
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
    ParquetFileReader parquetFileReader = readRecords(NESTED_PARQUET_FILE);
    int totalRecords = parquetFileReader.getNumRecords();
    assertThat(totalRecords).isEqualTo(6);
    List<String> records = parquetFileReader.getRecords(10);
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
    ParquetFileReader parquetFileReader = readRecords(LIST_PARQUET_FILE);
    int totalRecords = parquetFileReader.getNumRecords();
    assertThat(totalRecords).isEqualTo(1);
    List<String> records = parquetFileReader.getRecords(10);
    assertThat(records).hasSize(1);
    String firstRecord = records.get(0);
    assertThat(firstRecord).contains("[{\"element\": 42}, {\"element\": 47}, {\"element\": 139}]");
  }

  @Test
  @DisplayName("Assert that a Parquet file with an INT96 column can still be displayed")
  public void testInvalidFile() throws IOException {
    ParquetFileReader parquetFileReader = readRecords(INVALID_PARQUET_FILE);
    int totalRecords = parquetFileReader.getNumRecords();
    assertThat(totalRecords).isEqualTo(1000);
    List<String> records = parquetFileReader.getRecords(10);
    assertThat(records).hasSize(10);
    String firstRecord = records.get(0);
    assertThat(firstRecord).contains("\"first_name\": \"Amanda\", \"last_name\": \"Jordan\"");
  }

  @Test
  @DisplayName("Assert that a Parquet file with a LogicalType date column can still be displayed")
  public void testDateLogicalType() throws IOException {
    ParquetFileReader parquetFileReader = readRecords(LOGICAL_DATE_PARQUET_FILE);
    int totalRecords = parquetFileReader.getNumRecords();
    assertThat(totalRecords).isEqualTo(5);
    List<String> records = parquetFileReader.getRecords(10);
    assertThat(records).hasSize(5);
    String firstRecord = records.get(0);
    assertThat(firstRecord)
        .contains("{\"received_at\": \"1970-01-19T12:02:37.304Z\", \"name___string\": \"Tressa\"");
  }

  @Test
  @DisplayName(
      "Assert that a Parquet file with a LogicalType decimal column can still be displayed")
  public void testDecimalLogicalType() throws IOException {
    ParquetFileReader parquetFileReader = readRecords(LOGICAL_DECIMAL_PARQUET_FILE);
    int totalRecords = parquetFileReader.getNumRecords();
    assertThat(totalRecords).isEqualTo(3);
    List<String> records = parquetFileReader.getRecords(10);
    assertThat(records).hasSize(3);
    String firstRecord = records.get(0);
    assertThat(firstRecord).contains("{\"name\": \"ben\", \"score\": 1.15}");
  }

  private ParquetFileReader readRecords(String fileName) {
    File file = new File(getClass().getClassLoader().getResource(fileName).getFile());
    return new ParquetFileReader(file);
  }
}
