package uk.co.hadoopathome.intellij.viewer.fileformat;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.List;
import org.junit.Test;

public class ParquetReaderTest {

  // https://github.com/Teradata/kylo/blob/master/samples/sample-data/parquet/userdata1.parquet
  private static final String PARQUET_FILE = "parquet/userdata1.parquet";

  @Test
  public void testGetSchema() throws IOException {
    File file = new File(getClass().getClassLoader().getResource(PARQUET_FILE).getFile());
    Reader parquetReader = new ParquetReader(file);
    String schema = parquetReader.getSchema();
    assertTrue(schema.contains("optional double salary"));
  }

  @Test
  public void testGetRecords() throws IOException {
    File file = new File(getClass().getClassLoader().getResource(PARQUET_FILE).getFile());
    Reader parquetReader = new ParquetReader(file);
    List<String> records = parquetReader.getRecords(10);
    assertEquals(10, records.size());
    String firstRecord = records.get(0);
    assertTrue(firstRecord.contains("\"ip_address\":\"1.197.201.2\""));
  }
}
