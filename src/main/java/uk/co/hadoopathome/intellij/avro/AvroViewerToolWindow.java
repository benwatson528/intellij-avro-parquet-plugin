package uk.co.hadoopathome.intellij.avro;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.table.JBTable;
import org.apache.commons.lang3.StringUtils;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class AvroViewerToolWindow implements ToolWindowFactory {
    private static final Logger LOGGER = Logger.getInstance(AvroViewerToolWindow.class);
    private static final int NUM_RECORDS = 100;
    private JPanel toolWindowContent;
    private JTabbedPane tabbedPane;
    private JPanel schemaPanel;
    private RSyntaxTextArea schemaTextPane;
    private JPanel dataPanel;
//    private JTextPane dataTextPane;
    private RTextScrollPane schemaScrollPane;
    private JTable dataTable;
    private JRadioButton tableRadioButton;
    private JRadioButton rawRadioButton;
    private JTextField numRecordsValue;
    private JLabel numRecordsLabel;
    private JButton updateButton;
    private JSeparator separator;
    private JScrollPane dataTableScroll;

    public AvroViewerToolWindow() {
        this.schemaTextPane.setEditable(false);
        //setPanelAlignment(this.schemaTextPane, StyleConstants.ALIGN_CENTER);
        this.schemaTextPane.setText("Drag and drop a .avro or .avsc file here");
        this.tabbedPane.setEnabled(false);
        this.dataTable.setDropTarget(createDropTarget());
        this.schemaTextPane.setDropTarget(createDropTarget());

       // this.rawRadioButton.addActionListener(e -> (dataViewPanel.);
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
                    if (!(path.endsWith(".avro") || path.endsWith(".avsc"))) {
                        JOptionPane.showMessageDialog(null, "File must end .avro or .avsc");
                        return;
                    }
                    schemaTextPane.setText(String.format("Processing file %s", path));
                    LOGGER.info(String.format("Received file %s", path));
                    populatePanes(file);
                    tabbedPane.setEnabled(true);
                } catch (UnsupportedFlavorException | IOException e) {
                    JOptionPane.showMessageDialog(null, "Unable to read file");
                }
            }
        };
    }

    /**
     * Reads the Avro file and populates the panes with the schema and a sample of the data. Uses a {@link SwingWorker}
     * to avoid freezing IntelliJ for big files.
     *
     * @param file the Avro file to be read
     */
    private void populatePanes(File file) {
        SwingWorker swingWorker = new SwingWorker() {
            @Override
            protected Boolean doInBackground() throws Exception {
                schemaTextPane.setText("Processing file " + file.getPath());
                AvroReader avroReader = new AvroReader(file);
               // setPanelAlignment(schemaTextPane, StyleConstants.ALIGN_LEFT);
//                String formatted = StringUtils.join(avroReader.getRecords(NUM_RECORDS), "\n");
                List<String> records = avroReader.getRecords(NUM_RECORDS);
                TableFormatter tableFormatter = new TableFormatter(records);
                String[] columns = tableFormatter.getColumns();
                String[][] rows = tableFormatter.getRows();
                TableModel tableModel = new DefaultTableModel(rows, columns);
                dataTable.setModel(tableModel);
                schemaTextPane.setText(avroReader.getSchema());
                return true;
            }
        };
        swingWorker.execute();
    }

    @Override
    public void createToolWindowContent(Project project, ToolWindow toolWindow) {
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(this.toolWindowContent, "", false);
        toolWindow.getContentManager().addContent(content);
    }

    /**
     * Changes the alignment of text within a pane.
     *
     * @param textPane  the pane for which alignment should be changed
     * @param alignment the desired alignment, as a {@link StyleConstants} value
     */
    private void setPanelAlignment(JTextPane textPane, int alignment) {
        StyledDocument doc = textPane.getStyledDocument();
        SimpleAttributeSet position = new SimpleAttributeSet();
        StyleConstants.setAlignment(position, alignment);
        doc.setParagraphAttributes(0, doc.getLength(), position, false);
    }

    /**
     * The default IntelliJ GUI creator doesn't show line numbers or folding icon.
     */
    private void createUIComponents() {
        this.schemaTextPane = new RSyntaxTextArea();
        this.schemaTextPane.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JSON);
        this.schemaTextPane.setCodeFoldingEnabled(true);
        this.schemaScrollPane = new RTextScrollPane(this.schemaTextPane);
    }
}
