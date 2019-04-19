package uk.co.hadoopathome.intellij.avro;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

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
    private JPanel toolWindowContent;
    private JTabbedPane tabbedPane;
    private JPanel schemaPanel;
    private JTextPane schemaTextPane;
    private JPanel dataPanel;
    private JTextPane dataTextPane;
    private AvroFormatter avroFormatter = new AvroFormatter();

    public AvroViewerToolWindow() {
        this.schemaTextPane.setEditable(false);
        this.dataTextPane.setEditable(false);
        changePaneAlignment(this.dataTextPane, StyleConstants.ALIGN_CENTER);
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
                    String formatted = avroFormatter.format(file);
                    changePaneAlignment(dataTextPane, StyleConstants.ALIGN_LEFT);
                    dataTextPane.setText(formatted);
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
     * Changes the alignment of text within a pane.
     *
     * @param textPane  the pane for which alignment should be changed
     * @param alignment the desired alignment, as a {@link StyleConstants} value
     */
    private void changePaneAlignment(JTextPane textPane, int alignment) {
        StyledDocument doc = textPane.getStyledDocument();
        SimpleAttributeSet position = new SimpleAttributeSet();
        StyleConstants.setAlignment(position, alignment);
        doc.setParagraphAttributes(0, doc.getLength(), position, false);
    }
}
