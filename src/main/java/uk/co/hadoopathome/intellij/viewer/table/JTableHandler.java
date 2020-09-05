package uk.co.hadoopathome.intellij.viewer.table;

import com.intellij.openapi.diagnostic.Logger;
import java.awt.Component;
import java.util.List;
import javax.swing.DefaultCellEditor;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import org.apache.commons.lang3.ArrayUtils;

/** Manages the JTable. */
public class JTableHandler {
  private static final Logger LOGGER = Logger.getInstance(JTableHandler.class);
  private final JTable table;

  public JTableHandler(JTable table) {
    this.table = table;
  }

  /**
   * Updates the contents of the JTable.
   *
   * @param records the raw JSON records to be added to the table
   * @return true if the table was successfully updated with data, else false
   */
  public boolean updateTable(List<String> records) {
    TableFormatter tableFormatter = new TableFormatter(records);
    String[] columns = tableFormatter.getColumns();
    String[][] rows = tableFormatter.getRows();
    disableTableEditing();

    if (ArrayUtils.isEmpty(columns)) {
      LOGGER.warn("Unable to display data in table");
      return false;
    }

    TableModel tableModel = new DefaultTableModel(rows, columns);
    this.table.setModel(tableModel);
    resizeTableColumns();
    return true;
  }

  /** Ensures that cells in the table can be copied but not modified. */
  private void disableTableEditing() {
    JTextField textField = new JTextField();
    textField.setEditable(false);
    DefaultCellEditor editor = new DefaultCellEditor(textField);
    this.table.setDefaultEditor(Object.class, editor);
  }

  /**
   * Automatically resizes the table columns to fit the content. Source:
   * https://stackoverflow.com/a/17627497/729819.
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
    Component colComp =
        colRenderer.getTableCellRendererComponent(
            this.table, column.getHeaderValue(), false, false, 0, 0);
    return colComp.getPreferredSize().width;
  }
}
