package org.visualeagle.gui.components.logwindow;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JInternalFrame;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.RTextScrollPane;

/**
 * @author Dmitry
 */
public class LogWindow extends JInternalFrame implements GuiLogPrinter {

    private int maxLineCount = 5000;
    private int batchSizeToRemoveAfterLimit = 500;

    boolean autoscrollEnabled = false;
    private RSyntaxTextArea logTextArea;
    private RTextScrollPane scroll;
    private JMenuItem autoScrollMenuItem;

    public LogWindow(String title, boolean resizable, boolean closable, boolean maximizable, boolean iconifiable) {
        super(title, resizable, closable, maximizable, iconifiable);
        initGui();
    }

    private void initGui() {
        setLayout(new BorderLayout(0, 0));
        logTextArea = new RSyntaxTextArea();
        logTextArea.setHighlightCurrentLine(false);
        logTextArea.setLineWrap(true);
        logTextArea.setAnimateBracketMatching(false);
        logTextArea.setAntiAliasingEnabled(true);
        logTextArea.setAutoIndentEnabled(false);
        logTextArea.setBracketMatchingEnabled(false);
        logTextArea.setClearWhitespaceLinesEnabled(false);
        logTextArea.setCloseCurlyBraces(false);
        logTextArea.setCloseMarkupTags(false);
        logTextArea.setCodeFoldingEnabled(false);
        logTextArea.setFractionalFontMetricsEnabled(false);
        logTextArea.setHyperlinksEnabled(false);
        logTextArea.setHighlightSecondaryLanguages(false);
        logTextArea.setMarkOccurrences(false);
        logTextArea.setPaintTabLines(false);
        logTextArea.setUseFocusableTips(false);
        logTextArea.setUseSelectedTextColor(false);
        logTextArea.setEditable(false);
        logTextArea.setLineWrap(true);
        logTextArea.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    enableAutoscroll(false);
                }
            }
        });

        DefaultCaret caret = (DefaultCaret) logTextArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
        boolean showLineNumbers = true;
        scroll = new RTextScrollPane(logTextArea, showLineNumbers);

        add(scroll);
        createPopupMenu();
        enableAutoscroll(true);
        //for testing add lines
        //Timer timer = new Timer(500, (ActionEvent e) -> {
        //    println("" + (int) (Math.random() * 50000) + " line " + RandomStringUtils.randomAlphanumeric((int) (Math.random() * 600)));
        //});
        //timer.setRepeats(true);
        //timer.start();
    }

    public void disableLineNumbers() {
        scroll.setLineNumbersEnabled(false);
    }

    private void createPopupMenu() {
        JPopupMenu popupMenu = new JPopupMenu();

        JMenuItem copyItem = new JMenuItem("Copy");
        copyItem.addActionListener((ActionEvent e) -> {
            logTextArea.copy();
        });

        popupMenu.add(copyItem);

        JCheckBoxMenuItem wordWrapItem = new JCheckBoxMenuItem("Word Wrap");
        wordWrapItem.setSelected(true);
        wordWrapItem.addActionListener((ActionEvent e) -> {
            boolean wordWrap = wordWrapItem.isSelected();
            logTextArea.setLineWrap(wordWrap);
        });

        popupMenu.add(wordWrapItem);
        popupMenu.addSeparator();
        JMenuItem clearItem = new JMenuItem("Clear");
        clearItem.addActionListener((ActionEvent e) -> {
            logTextArea.discardAllEdits();
            logTextArea.setText("");
            logTextArea.discardAllEdits();
        });

        popupMenu.add(clearItem);

        popupMenu.addSeparator();
        autoScrollMenuItem = new JCheckBoxMenuItem("Autoscroll");
        autoScrollMenuItem.setSelected(true);
        autoScrollMenuItem.addActionListener((ActionEvent e) -> {
            enableAutoscroll(autoScrollMenuItem.isSelected());
        });

        popupMenu.add(autoScrollMenuItem);

        logTextArea.setPopupMenu(popupMenu);
    }

    private void scrollToBottom() {
        logTextArea.setCaretPosition(logTextArea.getDocument().getLength());
    }

    private void enableAutoscroll(boolean autoscroll) {
        autoscrollEnabled = autoscroll;
        autoScrollMenuItem.setSelected(autoscroll);
    }

    private void clearFirstLines(int count) {
        try {
            int lineToRemoveCount = Math.min(logTextArea.getLineCount() - 1, count);
            int end = logTextArea.getLineEndOffset(lineToRemoveCount);
            logTextArea.replaceRange("", 0, end);
        } catch (BadLocationException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void print(String str) {
        logTextArea.append(str);
        if (logTextArea.getLineCount() > maxLineCount) {
            clearFirstLines(batchSizeToRemoveAfterLimit);
            disableLineNumbers();
        }
        if (autoscrollEnabled) {
            scrollToBottom();
        }
    }

    @Override
    public void println(String str) {
        print(str + "\n");
    }

}
