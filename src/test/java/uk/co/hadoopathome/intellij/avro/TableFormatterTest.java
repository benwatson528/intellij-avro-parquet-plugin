package uk.co.hadoopathome.intellij.avro;

import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public class TableFormatterTest {
    @Test
    public void testFormatTable() {
        List<String> jsonRecords = Arrays.asList(
                "{\"name\" : \"Joe Cole\", \"subject\" :  \"Science\"}",
                "{\"name\" : \"Susan Jones\", \"subject\" :  \"Chemistry\"}"
        );

        TableFormatter tableFormatter = new TableFormatter(jsonRecords);
        Set<String> outputColumns = new HashSet<>(Arrays.asList(tableFormatter.getColumns()));
        Set<String> expectedColumns = new HashSet<>(Arrays.asList("name", "subject"));
        assertEquals(expectedColumns, outputColumns);

        String[][] rows = tableFormatter.getRows();
        Set<String> outputFirstRow = new HashSet<>(Arrays.asList(rows[0]));
        Set<String> expectedFirstRow = new HashSet<>(Arrays.asList("Joe Cole", "Science"));
        assertEquals(expectedFirstRow, outputFirstRow);
    }
}