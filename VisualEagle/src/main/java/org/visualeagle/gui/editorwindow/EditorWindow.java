package org.visualeagle.gui.editorwindow;

import java.awt.BorderLayout;
import java.awt.Color;
import java.io.IOException;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import org.apache.commons.lang3.StringUtils;
import org.visualeagle.gui.mainwindow.ActionManager;
import org.visualeagle.project.vnodes.AbstractVNode;
import org.visualeagle.utils.ChunkedTextCollector;
import org.visualeagle.utils.Lookup;

/**
 * @author Dmitry
 */
public class EditorWindow extends JInternalFrame {

    private JTabbedPane jtabbedPane;
    private EditorFactory editorFactory;
    private ChunkedTextCollector titleTextCollector;

    public EditorWindow(String title, boolean resizable, boolean closable, boolean maximizable, boolean iconifiable) {
        super(title, resizable, closable, maximizable, iconifiable);
        editorFactory = new EditorFactory();
        initGui();
        initEvents();
    }

    private void initGui() {
        setLayout(new BorderLayout(0, 0));
        jtabbedPane = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
        add(jtabbedPane);
        jtabbedPane.setOpaque(true);
        jtabbedPane.setBackground(new Color(240, 240, 240));
        titleTextCollector = new ChunkedTextCollector((String text) -> {
            setTitle(text);
        });

        titleTextCollector.setSeparator("    ");
        titleTextCollector.setTitles("title", "Editor", "fileName", "");
        registerTabSwitchTitleChanger();
    }

    private void registerTabSwitchTitleChanger() {
        jtabbedPane.addChangeListener((ChangeEvent e) -> {
            AbstractEditor currentEditor = getCurrentEditor();
            String name;
            if (currentEditor != null) {
                name = currentEditor.getFile().getName();
            } else {
                name = "";
            }
            titleTextCollector.setTitle("fileName", name);
        });
    }

    private void initEvents() {
        ActionManager actionManager = Lookup.get().get(ActionManager.class);
        actionManager.registerAction("save_file", this::save);
        actionManager.registerAction("save_all", this::saveAll);
        actionManager.registerAction("undo", this::undo);
        actionManager.registerAction("redo", this::redo);
        actionManager.registerAction("select_all", this::selectAll);
        actionManager.registerAction("cut", this::cut);
        actionManager.registerAction("copy", this::copy);
        actionManager.registerAction("paste", this::paste);
    }

    private void undo() {
        AbstractEditor editor = getCurrentEditor();
        if (editor != null) {
            editor.undo();
        }
    }

    private void redo() {
        AbstractEditor editor = getCurrentEditor();
        if (editor != null) {
            editor.redo();
        }
    }
    
    private void selectAll() {
        AbstractEditor editor = getCurrentEditor();
        if (editor != null) {
            editor.selectAll();
        }
    }

    private void copy() {
        AbstractEditor editor = getCurrentEditor();
        if (editor != null) {
            editor.copy();
        }
    }

    private void cut() {
        AbstractEditor editor = getCurrentEditor();
        if (editor != null) {
            editor.cut();
        }
    }

    private void paste() {
        AbstractEditor editor = getCurrentEditor();
        if (editor != null) {
            editor.paste();
        }
    }

    private void save() throws IOException {
        AbstractEditor editor = getCurrentEditor();
        if (editor != null) {
            editor.save();
        }
    }

    public void saveAll() throws IOException {
        for (int i = 0; i < jtabbedPane.getTabCount(); i++) {
            AbstractEditor editor = (AbstractEditor) jtabbedPane.getComponentAt(i);
            if (editor != null) {
                editor.save();
            }
        }
    }

    public boolean closeAllEditorWindows() throws IOException {
        for (int i = 0; i < jtabbedPane.getTabCount(); i++) {
            AbstractEditor editor = (AbstractEditor) jtabbedPane.getComponentAt(i);
            if (editor != null) {
                if (editor.isModified()) {
                    AskToSaveResult result = askToSaveEditor(editor);
                    if (null == result) {
                        editor.clearModified();
                    } else {
                        switch (result) {
                            case CANCEL:
                                return false;
                            case YES:
                                editor.save();
                                break;
                            default:
                                editor.clearModified();
                                break;
                        }
                    }

                }

                closeTab(editor);
                i--;
            }
        }
        return true;
    }

    private AbstractEditor searchInCurrentlyEditing(AbstractVNode vnode) {
        for (int i = 0; i < jtabbedPane.getTabCount(); i++) {
            AbstractEditor abstractEditor = (AbstractEditor) jtabbedPane.getComponentAt(i);
            if (vnode.equals(abstractEditor.getFile())) {
                return abstractEditor;
            }
        }

        return null;
    }

    private void selectEditingFile(AbstractEditor abstractEditor) {
        jtabbedPane.setSelectedComponent(abstractEditor);
    }

    public void closeTab(AbstractEditor abstractEditor) throws IOException {
        if (abstractEditor.isModified()) {
            AskToSaveResult result = askToSaveEditor(abstractEditor);
            if (result == AskToSaveResult.CANCEL) {
                return;
            }

            if (result == AskToSaveResult.YES) {
                saveTab(abstractEditor);
            }
        }

        abstractEditor.destroy();
        int indexOfTab = jtabbedPane.indexOfComponent(abstractEditor);
        try {
            jtabbedPane.remove(indexOfTab);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private AskToSaveResult askToSaveEditor(AbstractEditor abstractEditor) throws IOException {
        String message = "File " + StringUtils.abbreviateMiddle(abstractEditor.getFile().getName(), "...", 30) + " is modified. Save?";
        int result = JOptionPane.showOptionDialog(this, message, "Question", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);
        if (result == JOptionPane.CANCEL_OPTION) {
            return AskToSaveResult.CANCEL;
        }

        if (result == JOptionPane.YES_OPTION) {
            return AskToSaveResult.YES;
        }
        return AskToSaveResult.NO;
    }

    public AbstractEditor getCurrentEditor() {
        int index = jtabbedPane.getSelectedIndex();
        if (index == -1) {
            return null;
        }
        return (AbstractEditor) jtabbedPane.getComponentAt(index);
    }

    public void saveTab(AbstractEditor editor) throws IOException {
        editor.save();
    }

    public void editFile(AbstractVNode vnode) throws IOException {
        AbstractEditor editor = searchInCurrentlyEditing(vnode);
        if (editor != null) {
            selectEditingFile(editor);
        } else {
            TabComponent tabComponent = new TabComponent(vnode.getName());
            final AbstractEditor newEditor = editorFactory.createEditor(vnode, tabComponent);
            if (newEditor == null) {
                JOptionPane.showMessageDialog(EditorWindow.this, "Editor for file with extension [" + vnode.getExtension() + "] is not implemented", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            jtabbedPane.addTab(vnode.getName(), newEditor);
            jtabbedPane.setTabComponentAt(jtabbedPane.getTabCount() - 1, tabComponent);
            selectEditingFile(newEditor);
            newEditor.setFocus();
            tabComponent.addCloseEvent(ActionListener -> {
                try {
                    closeTab(newEditor);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            });
        }
    }

    private static enum AskToSaveResult {
        YES, NO, CANCEL
    }
}
