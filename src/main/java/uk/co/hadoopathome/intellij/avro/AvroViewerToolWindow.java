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
    private JTextPane textPane;

    private AvroFormatter avroFormatter = new AvroFormatter();

    public AvroViewerToolWindow() {
        changePaneAlignment(this.textPane, StyleConstants.ALIGN_CENTER);

        this.textPane.setText("Drag and drop a .avro or .avsc file here");
        this.textPane.setDropTarget(new DropTarget() {
            public void drop(DropTargetDropEvent evt) {
                try {
                    evt.acceptDrop(DnDConstants.ACTION_COPY);
                    File file = ((List<File>) evt.getTransferable().getTransferData(DataFlavor.javaFileListFlavor)).get(0);
                    String formatted = avroFormatter.format(file);
                    changePaneAlignment(textPane, StyleConstants.ALIGN_LEFT);
                    textPane.setText(formatted);
                } catch (UnsupportedFlavorException | IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(this.toolWindowContent, "", false);
        toolWindow.getContentManager().addContent(content);
    }

    private void changePaneAlignment(JTextPane textPane, int alignment) {
        StyledDocument doc = textPane.getStyledDocument();
        SimpleAttributeSet position = new SimpleAttributeSet();
        StyleConstants.setAlignment(position, alignment);
        doc.setParagraphAttributes(0, doc.getLength(), position, false);
    }
}
