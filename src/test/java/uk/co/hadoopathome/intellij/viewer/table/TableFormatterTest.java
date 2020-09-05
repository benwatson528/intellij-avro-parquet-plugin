package uk.co.hadoopathome.intellij.viewer.table;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TableFormatterTest {
  @Test
  @DisplayName("Assert that JSON records can be correctly extracted and formatted")
  public void testFormatTable() {
    List<String> jsonRecords =
        Arrays.asList(
            "{\"name\" : \"Joe Cole\", \"subject\" : \"Science\"}",
            "{\"name\" : \"Susan Jones\", \"subject\" : \"Chemistry\"}");

    TableFormatter tableFormatter = new TableFormatter(jsonRecords);
    Set<String> outputColumns = new HashSet<>(Arrays.asList(tableFormatter.getColumns()));
    Set<String> expectedColumns = new HashSet<>(Arrays.asList("name", "subject"));
    assertThat(expectedColumns).isEqualTo(outputColumns);

    String[][] rows = tableFormatter.getRows();
    Set<String> outputFirstRow = new HashSet<>(Arrays.asList(rows[0]));
    Set<String> expectedFirstRow = new HashSet<>(Arrays.asList("Joe Cole", "Science"));
    assertThat(expectedFirstRow).isEqualTo(outputFirstRow);
  }

  @Test
  @DisplayName("Assert that invalid JSON records are not formatted and an empty table is produced")
  public void testFormatTableInvalidJson() {
    List<String> jsonRecords =
        Arrays.asList("{\"name\" : \"Joe Cole\", \"subject\" : \"Science\"}", "{invalid}");

    TableFormatter tableFormatter = new TableFormatter(jsonRecords);
    Set<String> outputColumns = new HashSet<>(Arrays.asList(tableFormatter.getColumns()));
    assertThat(outputColumns).isEmpty();

    String[][] rows = tableFormatter.getRows();
    assertThat(rows).isEmpty();
  }
}
