package uk.co.hadoopathome.intellij.avro;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;

import javax.swing.*;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
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
    private JTextPane schemaTextPane;
    private JPanel dataPanel;
    private JTextPane dataTextPane;

    public AvroViewerToolWindow() {
        this.schemaTextPane.setEditable(false);
        this.dataTextPane.setEditable(false);
        setPanelAlignment(this.dataTextPane, StyleConstants.ALIGN_CENTER);
        this.dataTextPane.setText("Drag and drop a .avro or .avsc file here");
        this.tabbedPane.setEnabled(true);
        this.dataTextPane.setDropTarget(createDropTarget());
        this.schemaTextPane.setDropTarget(createDropTarget());
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
                    evt.acceptDrop(DnDConstants.ACTION_COPY);
                    File file = ((List<File>) evt.getTransferable().getTransferData(DataFlavor.javaFileListFlavor)).get(0);
                    LOGGER.info(String.format("Received file %s", file.getAbsolutePath()));
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
                //  JTextPane selectedPane = (JTextPane) tabbedPane.getSelectedComponent();
                AvroReader avroReader = new AvroReader(file);
                setPanelAlignment(dataTextPane, StyleConstants.ALIGN_LEFT);
                dataTextPane.setText(avroReader.getRecords(NUM_RECORDS).toString());
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
}
