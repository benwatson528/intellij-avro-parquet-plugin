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
import org.apache.commons.lang3.ArrayUtils;

/** Takes the JSON records and places them into the format expected by JTable. */
class TableFormatter {
  private static final Logger LOGGER = Logger.getInstance(TableFormatter.class);
  private final List<JsonObject> flattenedRecords;
  private final String[] columns;

  /**
   * Converts the raw records into flattened JSON and extracts all columns from the records.
   * LogicalTypes may not always be correctly formatted in JSON, and so JSON parsing exceptions are
   * caught so that they can be correctly handled.
   *
   * @param rawRecords the raw Avro or Parquet records in nested JSON format
   */
  TableFormatter(List<String> rawRecords) {
    this.flattenedRecords = new ArrayList<>();
    for (String rawRecord : rawRecords) {
      JsonObject jsonObject;
      try {
        String flatten = JsonFlattener.flatten(rawRecord);
        jsonObject = JsonParser.parseString(flatten).getAsJsonObject();
      } catch (Exception e) {
        LOGGER.warn(
            String.format(
                "Unable to parse record into JSON; no formatted records will be available for this"
                    + " file: %s",
                rawRecord));
        this.columns = ArrayUtils.EMPTY_STRING_ARRAY;
        return;
      }
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
   * view. This results in columns being stored in alphabetical order rather than their input order.
   *
   * <p>Columns can't simply be extracted from the schema as we don't know which columns are present
   * in the flattened subset of records being displayed until we have flattened them.
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
