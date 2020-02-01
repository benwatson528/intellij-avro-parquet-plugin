package uk.co.hadoopathome.intellij.viewer.fileformat;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.List;
import org.junit.Test;

public class AvroReaderTest {
  private static final String TWITTER_AVRO_FILE = "avro/twitter.avro";
  // https://github.com/apache/incubator-gobblin/blob/master/gobblin-core/src/test/resources/converter/pickfields_nested_with_union.avro
  private static final String COMPLEX_AVRO_FILE = "avro/pickfields_nested_with_union.avro";
  private static final String INVALID_AVRO_FILE = "avro/invalid.avro";

  @Test
  public void testGetSchema() throws IOException {
    File file = new File(getClass().getClassLoader().getResource(TWITTER_AVRO_FILE).getFile());
    Reader avroReader = new AvroReader(file);
    String schema = avroReader.getSchema();
    assertTrue(schema.contains("A basic schema for storing Twitter messages"));
  }

  @Test
  public void testGetRecords() throws IOException {
    File file = new File(getClass().getClassLoader().getResource(TWITTER_AVRO_FILE).getFile());
    Reader avroReader = new AvroReader(file);
    List<String> records = avroReader.getRecords(100);
    assertEquals(2, records.size());
    String firstRecord = records.get(0);
    assertTrue(firstRecord.contains("Nerf paper"));
  }

  @Test
  public void testComplexNesting() throws IOException {
    File file = new File(getClass().getClassLoader().getResource(COMPLEX_AVRO_FILE).getFile());
    Reader avroReader = new AvroReader(file);
    List<String> records = avroReader.getRecords(100);
    assertEquals(6, records.size());
    String firstRecord = records.get(0);
    assertTrue(firstRecord.contains("btnzlrfptk"));
  }

  @Test(expected = OutOfMemoryError.class)
  public void testInvalidFile() throws IOException {
    File file = new File(getClass().getClassLoader().getResource(INVALID_AVRO_FILE).getFile());
    new AvroReader(file);
  }
}
