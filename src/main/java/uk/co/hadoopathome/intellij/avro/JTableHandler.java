package uk.co.hadoopathome.intellij.avro;

import com.intellij.openapi.diagnostic.Logger;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;

/**
 * Manages the JTable.
 */
class JTableHandler {
    private static final Logger LOGGER = Logger.getInstance(JTableHandler.class);
    private final JTable table;

    JTableHandler(JTable table) {
        this.table = table;
    }

    /**
     * Updates the contents of the JTable.
     *
     * @param records the raw JSON records to be added to the table
     */
    void updateTable(List<String> records) {
        TableFormatter tableFormatter = new TableFormatter(records);
        String[] columns = tableFormatter.getColumns();
        String[][] rows = tableFormatter.getRows();
        disableTableEditing();

        TableModel tableModel = new DefaultTableModel(rows, columns);
        this.table.setModel(tableModel);
        resizeTableColumns();
    }

    /**
     * Ensures that cells in the table can be copied but not modified.
     */
    private void disableTableEditing() {
        JTextField textField = new JTextField();
        textField.setEditable(false);
        DefaultCellEditor editor = new DefaultCellEditor(textField);
        this.table.setDefaultEditor(Object.class, editor);
    }

    /**
     * Automatically resizes the table columns to fit the content. Source: https://stackoverflow.com/a/17627497/729819.
     */
    private void resizeTableColumns() {
        TableColumnModel columnModel = this.table.getColumnModel();
        for (int columnCounter = 0; columnCounter < this.table.getColumnCount(); columnCounter++) {
            TableColumn column = columnModel.getColumn(columnCounter);
            int colWidth = getColHeaderWidth(column);

            int maxRowWidth = 50;
            for (int row = 0; row < this.table.getRowCount(); row++) {
                TableCellRenderer cellRenderer = this.table.getCellRenderer(row, columnCounter);
                Component rowComp = this.table.prepareRenderer(cellRenderer, row, columnCounter);
                maxRowWidth = Math.max(rowComp.getPreferredSize().width, maxRowWidth);
            }
            if (maxRowWidth > colWidth && maxRowWidth > 300) {
                column.setPreferredWidth(300);
            } else {
                column.setPreferredWidth(Math.max(colWidth, maxRowWidth) + 10);
            }
        }
    }

    /**
     * Finds the width of the text header of a given column.
     *
     * @param column the specific column whose width is to be defined
     * @return the width of the column in pixels
     */
    private int getColHeaderWidth(TableColumn column) {
        TableCellRenderer colRenderer = column.getCellRenderer();
        if (colRenderer == null) {
            colRenderer = this.table.getTableHeader().getDefaultRenderer();
        }
        Component colComp = colRenderer.getTableCellRendererComponent(this.table, column.getHeaderValue(), false, false,
                0, 0);
        return colComp.getPreferredSize().width;
    }
}
