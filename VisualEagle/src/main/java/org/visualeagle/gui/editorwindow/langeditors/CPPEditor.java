package org.visualeagle.gui.editorwindow.langeditors;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import javax.swing.event.CaretEvent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.visualeagle.gui.editorwindow.AbstractEditor;
import org.visualeagle.gui.editorwindow.TabComponent;
import org.visualeagle.project.vnodes.AbstractVNode;
import org.visualeagle.utils.Lookup;

/**
 * @author Dmitry
 */
public class CPPEditor extends AbstractEditor {

    private RSyntaxTextArea textArea;
    private RTextScrollPane scrollPane;

    public CPPEditor(AbstractVNode file, TabComponent tabComponent) throws IOException {
        super(file, tabComponent);
        init();
    }

    private void init() throws IOException {
        setLayout(new BorderLayout(0, 0));
        textArea = new RSyntaxTextArea(readFileFully());
        textArea.setSyntaxEditingStyle(RSyntaxTextArea.SYNTAX_STYLE_CPLUSPLUS);
        textArea.setTabsEmulated(true);
        textArea.discardAllEdits();
        textArea.setCaretPosition(0);
        textArea.setAnimateBracketMatching(false);
        textArea.setCodeFoldingEnabled(true);
        textArea.setMarkOccurrences(true);
        textArea.setMarkOccurrencesDelay(500);
        textArea.setPaintTabLines(true);
        textArea.addCaretListener((CaretEvent e) -> {
            try {
                int position = e.getDot();
                int y1 = textArea.getLineOfOffset(position);
                int x1 = position - textArea.getLineStartOffset(y1);
                String loc = "" + x1 + ":" + y1;
                Lookup.get().put("cursorPosition", loc);
            } catch (BadLocationException ex) {
                ex.printStackTrace();
            }
        });
        textArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                setModified();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                setModified();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                setModified();
            }
        });
        scrollPane = new RTextScrollPane(textArea, true, Color.lightGray);
        scrollPane.setIconRowHeaderEnabled(true);
        if (getFile().isReadonly()) {
            textArea.setEditable(false);
        }

        add(scrollPane, BorderLayout.CENTER);
    }

    @Override

    public void setFocus() {
        textArea.requestFocusInWindow();
    }

    @Override
    public String getDataToSave() {
        try {
            return textArea.getDocument().getText(0, textArea.getDocument().getLength());
        } catch (BadLocationException ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public void undo() {
        textArea.undoLastAction();
    }

    @Override
    public void redo() {
        textArea.redoLastAction();
    }

    @Override
    public void copy() {
        textArea.copy();
    }

    @Override
    public void cut() {
        textArea.cut();
    }

    @Override
    public void paste() {
        textArea.paste();
    }

    @Override
    public void selectAll() {
        textArea.selectAll();
    }

}
