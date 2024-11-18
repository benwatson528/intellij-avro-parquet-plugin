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
package uk.co.hadoopathome.intellij.viewer;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.JBColor;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import java.awt.CardLayout;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ItemEvent;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.SwingWorker;
import org.apache.commons.lang3.StringUtils;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.fife.ui.rtextarea.RTextScrollPane;
import uk.co.hadoopathome.intellij.viewer.fileformat.AvroFileReader;
import uk.co.hadoopathome.intellij.viewer.fileformat.ParquetFileReader;
import uk.co.hadoopathome.intellij.viewer.fileformat.Reader;
import uk.co.hadoopathome.intellij.viewer.table.JTableHandler;

public class FileViewerToolWindow implements ToolWindowFactory {
  private static final Logger LOGGER = Logger.getInstance(FileViewerToolWindow.class);
  private static final String ALL = "All";
  private static final String STARTUP_MESSAGE = "Drag and drop a valid .avro or .parquet file here";
  private final JTableHandler tableHandler;
  private JPanel toolWindowContent;
  private JTabbedPane tabbedPane;
  private JPanel schemaPanel;
  private RSyntaxTextArea schemaTextPane;
  private JPanel dataPanel;
  private RTextScrollPane schemaScrollPane;
  private JTable dataTable;
  private JRadioButton tableRadioButton;
  private JRadioButton rawRadioButton;
  private JLabel numRecordsLabel;
  private JSeparator separator;
  private JScrollPane dataTableScroll;
  private JPanel dataCardLayout;
  private RSyntaxTextArea dataRawTextArea;
  private RTextScrollPane dataRawScroll;
  private JComboBox numRecordsComboBox;
  private JPanel fileInfoPanel;
  private JLabel fileInfoLabel;
  private JScrollPane fieldInfoScrollPane;
  private File currentFile;

  /** Creates the IntelliJ Tool Window. */
  public FileViewerToolWindow() {
    this.dataTable.setDropTarget(createDropTarget());
    this.dataTableScroll.setDropTarget(createDropTarget());
    this.dataRawTextArea.setDropTarget(createDropTarget());
    this.schemaTextPane.setDropTarget(createDropTarget());
    this.tableHandler = new JTableHandler(this.dataTable);
    this.fieldInfoScrollPane.setBorder(BorderFactory.createEmptyBorder());
    createDataPaneRadioButtonListeners();
    createComboBoxListener();
  }

  /**
   * Creates the {@link DropTarget} instance to detect when files are dragged into a tab.
   *
   * @return the instantiated {@link DropTarget}
   */
  private DropTarget createDropTarget() {
    return new DropTarget() {
      public synchronized void drop(DropTargetDropEvent evt) {
        try {
          tabbedPane.setSelectedIndex(0);
          evt.acceptDrop(DnDConstants.ACTION_COPY);
          File file =
              ((List<File>) evt.getTransferable().getTransferData(DataFlavor.javaFileListFlavor))
                  .get(0);
          String path = file.getPath();
          schemaTextPane.setText(String.format("Processing file %s", path));
          LOGGER.info(String.format("Received file %s", path));
          populatePanes(file, convertComboBoxValueToInt(numRecordsComboBox.getSelectedItem()));
          tabbedPane.setEnabled(true);
        } catch (UnsupportedFlavorException | IOException e) {
          JOptionPane.showMessageDialog(null, "Unable to read file");
        }
      }
    };
  }

  @Override
  public void createToolWindowContent(Project project, ToolWindow toolWindow) {
    ContentFactory contentFactory = ContentFactory.getInstance();
    Content content = contentFactory.createContent(this.toolWindowContent, "", false);
    toolWindow.getContentManager().addContent(content);
  }

  /**
   * The default IntelliJ GUI creator doesn't show line numbers or folding icon for RSyntaxTextArea
   * panels.
   */
  private void createUIComponents() {
    this.schemaTextPane = new RSyntaxTextArea();
    this.schemaTextPane.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JSON);
    this.schemaTextPane.setCodeFoldingEnabled(true);
    this.schemaScrollPane = new RTextScrollPane(this.schemaTextPane);
    this.schemaScrollPane.setFoldIndicatorEnabled(true);
    this.schemaScrollPane.setLineNumbersEnabled(true);
    this.schemaTextPane.setEditable(false);
    this.schemaTextPane.setText(STARTUP_MESSAGE);
    setTheme(this.schemaTextPane);

    this.dataRawTextArea = new RSyntaxTextArea();
    this.dataRawTextArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JSON);
    this.dataRawTextArea.setCodeFoldingEnabled(false);
    this.dataRawScroll = new RTextScrollPane(this.dataRawTextArea);
    setTheme(this.dataRawTextArea);
  }

  /**
   * Detects if the IntelliJ Darcula theme is being used, and updates the pane's colours
   * accordingly. Any other theme (even dark ones) will appear with a white background.
   *
   * @param syntaxTextArea the text area whose colours should be updated
   */
  private void setTheme(RSyntaxTextArea syntaxTextArea) {
    if (!JBColor.isBright()) {
      try {
        Theme theme =
            Theme.load(getClass().getClassLoader().getResourceAsStream("META-INF/dark.xml"));
        theme.apply(syntaxTextArea);
      } catch (IOException e) {
        LOGGER.warn("Unable to find theme file, defaulting to light theme");
      }
    }
  }

  /**
   * Reads the Avro file and populates the panes with the schema and a sample of the data. Uses a
   * {@link SwingWorker} to avoid freezing IntelliJ for big files.
   *
   * @param file the Avro file to be read
   */
  private void populatePanes(File file, int numRecords) {
    this.currentFile = file;
    SwingWorker swingWorker =
        new SwingWorker() {
          @Override
          protected Boolean doInBackground() {
            schemaTextPane.setText(String.format("Processing file %s...", file.getPath()));
            try {
              Reader reader = detectFileType(currentFile);
              LOGGER.info(
                  String.format("Detected file %s as a %s", currentFile, reader.getClass()));
              List<String> records = reader.getRecords(numRecords);
              int totalRecords = reader.getNumRecords();
              configureDataPanes(records);
              schemaTextPane.setText(reader.getSchema());
              String recordPlural = records.size() == 1 ? "" : "s";
              fileInfoLabel.setText(
                  String.format(
                      "Displaying %d of %d record%s from %s",
                      records.size(), totalRecords, recordPlural, file.getPath()));
              return true;
            } catch (Throwable t) {
              JOptionPane.showMessageDialog(
                  new JFrame(),
                  "Unable to process file, see IDEA logs for more information.\n\nError: "
                      + t.getMessage(),
                  "Error",
                  JOptionPane.ERROR_MESSAGE);
              LOGGER.error(String.format("Unable to process file %s", file), t);
              schemaTextPane.setText(STARTUP_MESSAGE);
              return false;
            }
          }
        };
    swingWorker.execute();
  }

  /**
   * Identifies the file type of the dropped file by attempting to parse it with both readers -
   * either Avro or Parquet.
   *
   * @param currentFile the file to be parsed
   * @return the AvroFileReader or ParquetFileReader, else an exception if the file is not
   *     recognised by either
   */
  private Reader detectFileType(File currentFile) throws IOException {
    try {
      return new AvroFileReader(currentFile);
    } catch (Exception e) {
      LOGGER.debug(String.format("File %s is not an Avro file", currentFile));
    }
    try {
      return new ParquetFileReader(currentFile);
    } catch (Exception e) {
      LOGGER.debug(String.format("File %s is not a Parquet file", currentFile));
    }
    throw new IOException(
        String.format("File %s is not recognised as either Parquet or Avro", currentFile));
  }

  /**
   * Populates the raw and table data panes with records and configures the radio buttons. If
   * invalid JSON is found, the table pane is disabled and no data is loaded into it.
   *
   * @param records the records to be displayed
   */
  private void configureDataPanes(List<String> records) {
    dataRawTextArea.setText(StringUtils.join(records, "\n"));
    boolean tableUpdated = tableHandler.updateTable(records);
    if (!tableUpdated) {
      tableRadioButton.setToolTipText("Table view not available due to invalid JSON records");
      CardLayout cardLayout = (CardLayout) dataCardLayout.getLayout();
      cardLayout.show(dataCardLayout, "dataRawCard");
    } else {
      CardLayout cardLayout = (CardLayout) dataCardLayout.getLayout();
      cardLayout.show(dataCardLayout, "dataTableCard");
    }
    tableRadioButton.setEnabled(tableUpdated);
    tableRadioButton.setSelected(tableUpdated);
    rawRadioButton.setSelected(!tableUpdated);
  }

  private void createDataPaneRadioButtonListeners() {
    this.rawRadioButton.addActionListener(
        e -> {
          CardLayout cardLayout = (CardLayout) this.dataCardLayout.getLayout();
          cardLayout.show(this.dataCardLayout, "dataRawCard");
        });

    this.tableRadioButton.addActionListener(
        e -> {
          CardLayout cardLayout = (CardLayout) this.dataCardLayout.getLayout();
          cardLayout.show(this.dataCardLayout, "dataTableCard");
        });
  }

  private void createComboBoxListener() {
    for (Object item : Arrays.asList(10, 50, 100, 500, 1000, ALL)) {
      this.numRecordsComboBox.addItem(item);
    }
    this.numRecordsComboBox.setSelectedItem(1000);

    this.numRecordsComboBox.addItemListener(
        e -> {
          if (e.getStateChange() == ItemEvent.SELECTED) {
            if (this.currentFile == null) {
              return;
            }
            Object item = e.getItem();
            int numRecords = convertComboBoxValueToInt(item);
            populatePanes(this.currentFile, numRecords);
          }
        });
  }

  private int convertComboBoxValueToInt(Object comboBoxValue) {
    if (comboBoxValue.equals(ALL)) {
      return Integer.MAX_VALUE;
    } else {
      return (Integer) comboBoxValue;
    }
  }
}
