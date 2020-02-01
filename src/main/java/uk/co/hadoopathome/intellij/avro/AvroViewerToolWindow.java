package uk.co.hadoopathome.intellij.avro;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.util.ui.UIUtil;
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
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.SwingWorker;
import org.apache.commons.lang.StringUtils;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.fife.ui.rtextarea.RTextScrollPane;
import uk.co.hadoopathome.intellij.avro.fileformat.AvroReader;
import uk.co.hadoopathome.intellij.avro.table.JTableHandler;

public class AvroViewerToolWindow implements ToolWindowFactory {
  private static final Logger LOGGER = Logger.getInstance(AvroViewerToolWindow.class);
  private static final String ALL = "All";
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
  private File currentFile;

  /** Creates the IntelliJ Tool Window. */
  public AvroViewerToolWindow() {
    this.dataTable.setDropTarget(createDropTarget());
    this.dataTableScroll.setDropTarget(createDropTarget());
    this.dataRawTextArea.setDropTarget(createDropTarget());
    this.schemaTextPane.setDropTarget(createDropTarget());
    this.tableHandler = new JTableHandler(this.dataTable);
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
      public void drop(DropTargetDropEvent evt) {
        try {
          tabbedPane.setSelectedIndex(0);
          evt.acceptDrop(DnDConstants.ACTION_COPY);
          File file =
              ((List<File>) evt.getTransferable().getTransferData(DataFlavor.javaFileListFlavor))
                  .get(0);
          String path = file.getPath();
          if (!path.endsWith(".avro")) {
            JOptionPane.showMessageDialog(null, "Must be a .avro file");
            return;
          }
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
    ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
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
    this.schemaTextPane.setText("Drag and drop a .avro file here");
    setTheme(this.schemaTextPane);

    this.dataRawTextArea = new RSyntaxTextArea();
    this.dataRawTextArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JSON);
    this.dataRawTextArea.setCodeFoldingEnabled(false);
    this.dataRawScroll = new RTextScrollPane(this.dataRawTextArea);
    setTheme(this.dataRawTextArea);
  }

  /**
   * Detects if the IntelliJ Darcula theme is being used, and updates the JSON colours accordingly.
   * Any other theme (even dark ones) will appear with a white background.
   *
   * @param syntaxTextArea the text area whose colours should be updated
   */
  private void setTheme(RSyntaxTextArea syntaxTextArea) {
    if (UIUtil.isUnderDarcula()) {
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
          protected Boolean doInBackground() throws Exception {
            schemaTextPane.setText("Processing file " + file.getPath());
            AvroReader avroReader = new AvroReader(file);
            List<String> records = avroReader.getRecords(numRecords);
            tableHandler.updateTable(records);
            dataRawTextArea.setText(StringUtils.join(records, "\n"));
            schemaTextPane.setText(avroReader.getSchema());
            fileInfoLabel.setText(
                "Displaying " + records.size() + " records from " + file.getPath());
            return true;
          }
        };
    swingWorker.execute();
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
