package uk.co.hadoopathome.intellij.avro;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.apache.commons.lang.StringUtils;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
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

public class AvroViewerToolWindow implements ToolWindowFactory {
    private static final Logger LOGGER = Logger.getInstance(AvroViewerToolWindow.class);
    private static final String ALL = "All";
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

    public AvroViewerToolWindow() {
        this.dataTable.setDropTarget(createDropTarget());
        this.schemaTextPane.setDropTarget(createDropTarget());
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
                    File file = ((List<File>) evt.getTransferable().getTransferData(DataFlavor.javaFileListFlavor)).get(0);
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
     * The default IntelliJ GUI creator doesn't show line numbers or folding icon.
     */
    private void createUIComponents() {
        this.schemaTextPane = new RSyntaxTextArea();
        this.schemaTextPane.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JSON);
        this.schemaTextPane.setCodeFoldingEnabled(true);
        this.schemaScrollPane = new RTextScrollPane(this.schemaTextPane);
        this.schemaTextPane.setEditable(false);
        this.schemaTextPane.setText("Drag and drop a .avro file here");

        this.dataRawTextArea = new RSyntaxTextArea();
        this.dataRawTextArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JSON);
        this.dataRawTextArea.setCodeFoldingEnabled(false);
        this.dataRawScroll = new RTextScrollPane(this.dataRawTextArea);
    }

    /**
     * Reads the Avro file and populates the panes with the schema and a sample of the data. Uses a {@link SwingWorker}
     * to avoid freezing IntelliJ for big files.
     *
     * @param file the Avro file to be read
     */
    private void populatePanes(File file, int numRecords) {
        this.currentFile = file;
        SwingWorker swingWorker = new SwingWorker() {
            @Override
            protected Boolean doInBackground() throws Exception {
                schemaTextPane.setText("Processing file " + file.getPath());
                AvroReader avroReader = new AvroReader(file);
                List<String> records = avroReader.getRecords(numRecords);
                TableFormatter tableFormatter = new TableFormatter(records);
                String[] columns = tableFormatter.getColumns();
                String[][] rows = tableFormatter.getRows();
                TableModel tableModel = new DefaultTableModel(rows, columns);
                dataTable.setModel(tableModel);
                dataRawTextArea.setText(StringUtils.join(records, "\n"));
                schemaTextPane.setText(avroReader.getSchema());
                fileInfoLabel.setText("Displaying " + records.size() + " records from " + file.getPath());
                return true;
            }
        };
        swingWorker.execute();
    }

    private void createDataPaneRadioButtonListeners() {
        this.rawRadioButton.addActionListener(e -> {
            CardLayout cardLayout = (CardLayout) dataCardLayout.getLayout();
            cardLayout.show(dataCardLayout, "dataRawCard");
        });

        this.tableRadioButton.addActionListener(e -> {
            CardLayout cardLayout = (CardLayout) dataCardLayout.getLayout();
            cardLayout.show(dataCardLayout, "dataTableCard");
        });
    }

    private void createComboBoxListener() {
        for (Object item : Arrays.asList(10, 50, 100, 500, 1000, ALL)) {
            this.numRecordsComboBox.addItem(item);
        }
        this.numRecordsComboBox.setSelectedItem(1000);

        this.numRecordsComboBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                if (currentFile == null) {
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
