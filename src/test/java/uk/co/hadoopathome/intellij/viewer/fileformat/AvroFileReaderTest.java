/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.co.hadoopathome.intellij.viewer.fileformat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

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
    AvroFileReader avroFileReader = readRecords(TWITTER_AVRO_FILE);
    String schema = avroFileReader.getSchema();
    assertThat(schema).contains("A basic schema for storing Twitter messages");
  }

  @Test
  @DisplayName("Assert that one record can be extracted from an Avro file")
  public void testGetRecords() throws IOException {
    AvroFileReader avroFileReader = readRecords(TWITTER_AVRO_FILE);
    int totalRecords = avroFileReader.getNumRecords();
    assertThat(totalRecords).isEqualTo(2);
    List<String> records = avroFileReader.getRecords(1);
    assertThat(records).hasSize(1);
    String firstRecord = records.get(0);
    assertThat(firstRecord).contains("Nerf paper");
  }

  @Test
  @DisplayName("Assert that all records can be extracted from an Avro file")
  public void testGetAllRecords() throws IOException {
    AvroFileReader avroFileReader = readRecords(TWITTER_AVRO_FILE);
    int totalRecords = avroFileReader.getNumRecords();
    assertThat(totalRecords).isEqualTo(2);
    List<String> records = avroFileReader.getRecords(100);
    assertThat(records).hasSize(2);
    String firstRecord = records.get(0);
    assertThat(firstRecord).contains("Nerf paper");
  }

  @Test
  @DisplayName("Assert that an Avro file with complex nesting is correctly parsed")
  public void testComplexNesting() throws IOException {
    AvroFileReader avroFileReader = readRecords(COMPLEX_AVRO_FILE);
    int totalRecords = avroFileReader.getNumRecords();
    assertThat(totalRecords).isEqualTo(6);
    List<String> records = avroFileReader.getRecords(100);
    assertThat(records).hasSize(6);
    String firstRecord = records.get(0);
    assertThat(firstRecord).contains("btnzlrfptk");
  }

  @Test
  @DisplayName("Assert that an Avro file with a decimal LogicalType is correctly parsed")
  public void testDecimalLogicalType() {
    try {
      AvroFileReader avroFileReader = readRecords(DECIMAL_LOGICAL_TYPE);
      int totalRecords = avroFileReader.getNumRecords();
      assertThat(totalRecords).isEqualTo(1);
      List<String> records = avroFileReader.getRecords(100);
      assertThat(records).hasSize(1);
      String firstRecord = records.get(0);
      assertThat(firstRecord).contains("25.190000");
    } catch (IOException e) {
      fail();
    }
  }

  @Test
  @DisplayName("Assert that an invalid Avro file throws an exception")
  public void testInvalidFile() {
    File file = new File(getClass().getClassLoader().getResource(INVALID_AVRO_FILE).getFile());
    assertThrows(OutOfMemoryError.class, () -> new AvroFileReader(file));
  }

  private AvroFileReader readRecords(String fileName) throws IOException {
    File file = new File(getClass().getClassLoader().getResource(fileName).getFile());
    return new AvroFileReader(file);
  }
}
