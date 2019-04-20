package uk.co.hadoopathome.intellij.avro;

import com.github.wnameless.json.flattener.JsonFlattener;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

class TableFormatter {
    private final List<String> rawRecords;
    private String[] columns;

    TableFormatter(List<String> rawRecords) {
        this.rawRecords = rawRecords;
        this.columns = findColumns();
    }

    String[][] getRows() {
        String[][] rows = new String[this.rawRecords.size()][this.columns.length];
        int i = 0;
        for (String rawRecord : rawRecords) {
            List<String> values = new ArrayList<>();
            String flattened = JsonFlattener.flatten(rawRecord);
            JSONObject jsonObject = new JSONObject(flattened);
            for (String column : columns) {
                values.add(jsonObject.get(column).toString());
            }
            rows[i] = values.toArray(new String[0]);
            i++;
        }
        return rows;
    }

    String[] getColumns() {
        return columns;
    }

    private String[] findColumns() {
        String firstRecord = this.rawRecords.get(0);
        String flattened = JsonFlattener.flatten(firstRecord);
        JSONObject jsonObject = new JSONObject(flattened);
        Set<String> keys = jsonObject.keySet();
        String[] columns = keys.toArray(new String[0]);
        this.columns = columns;
        return columns;
    }
}
