package uk.co.hadoopathome.intellij.viewer.table;

import com.github.wnameless.json.flattener.JsonFlattener;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.intellij.openapi.diagnostic.Logger;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/** Takes the JSON records and places them into the format expected by JTable. */
class TableFormatter {
  private static final Logger LOGGER = Logger.getInstance(TableFormatter.class);
  private final List<JsonObject> flattenedRecords;
  private final String[] columns;

  TableFormatter(List<String> rawRecords) {
    this.flattenedRecords = new ArrayList<>();
    for (String rawRecord : rawRecords) {
      String flatten = JsonFlattener.flatten(rawRecord);
      JsonObject jsonObject = JsonParser.parseString(flatten).getAsJsonObject();
      this.flattenedRecords.add(jsonObject);
    }
    this.columns = constructAllColumns();
  }

  /**
   * Get all of the populated rows for the table, in the format expected by JTable.
   *
   * @return the rows for the table
   */
  String[][] getRows() {
    String[][] rows = new String[this.flattenedRecords.size()][this.columns.length];
    for (int i = 0; i < this.flattenedRecords.size(); i++) {
      JsonObject flattenedRecord = this.flattenedRecords.get(i);
      String[] values = new String[this.columns.length];
      for (int j = 0; j < this.columns.length; j++) {
        String column = this.columns[j];
        if (flattenedRecord.has(column)) {
          JsonElement value = flattenedRecord.get(column);
          if (value != null
              && !value.isJsonNull()
              && !(value.isJsonObject() && value.getAsJsonObject().size() == 0)
              && !(value.isJsonArray() && value.getAsJsonArray().size() == 0)) {
            values[j] = value.getAsString();
          } else {
            if (value != null) {
              LOGGER.warn(
                  String.format(
                      "Unable to display cell for column: %s with value: %s", column, value));
            }
          }
        }
      }
      rows[i] = values;
    }
    return rows;
  }

  String[] getColumns() {
    return this.columns;
  }

  /**
   * Get every (flattened) column from every record to be displayed. This is required for the table
   * view.
   *
   * @return a Set of all possible columns
   */
  private String[] constructAllColumns() {
    Set<String> totalKeys = new TreeSet<>();
    for (JsonObject flattenedRecord : this.flattenedRecords) {
      totalKeys.addAll(flattenedRecord.keySet());
    }
    LOGGER.info(String.format("Found %d unique columns", totalKeys.size()));
    return totalKeys.toArray(new String[0]);
  }
}
