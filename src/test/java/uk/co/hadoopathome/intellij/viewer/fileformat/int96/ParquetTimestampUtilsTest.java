package uk.co.hadoopathome.intellij.viewer.fileformat.int96;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ParquetTimestampUtilsTest {

  @Test
  @DisplayName("Assert that INT96 to timestamp conversion works correctly")
  public void testInt96Convert() {
    String json =
        "{\"registration_dttm\": [0, 42, -23, 108, -14, 25, 0, 0, 78, 127, 37, 0], \"last_name\": \"Jordan\", "
            + "\"last_updated\": [0, 94, 48, -48, -31, 55, 0, 0, 78, 127, 37, 0], \"gender\": \"Female\", "
            + "\"created_at\": [0, -14, -70, -51, -5, 1, 0, 0, 78, 127, 37, 0]}";
    String updatedJson = ParquetTimestampUtils.convertInt96(json);
    assertThat(updatedJson)
        .contains(
            "{\"registration_dttm\": \"2016-02-03T07:55:29Z[UTC]\", \"last_name\": \"Jordan\", "
                + "\"last_updated\": \"2016-02-03T17:04:03Z[UTC]\", \"gender\": \"Female\", "
                + "\"created_at\": \"2016-02-03T00:36:21Z[UTC]\"}");
  }
}
