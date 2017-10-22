package org.visualeagle.gui.components.editorwindow;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import org.apache.commons.lang3.StringUtils;
import org.visualeagle.gui.ActionManager;
import org.visualeagle.project.vnodes.AbstractVNode;
import org.visualeagle.utils.Lookup;

/**
 * @author Dmitry
 */
public class EditorWindow extends JInternalFrame {

    private JTabbedPane jtabbedPane;
    private EditorFactory editorFactory;

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
    }

    private void initEvents() {
        ActionManager actionManager = Lookup.get().get(ActionManager.class);
        actionManager.registerAction("save_file", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    save();
                } catch (IOException ex) {
                    ex.printStackTrace();
                    throw new RuntimeException("Error while thread", ex);
                }
            }
        });
        actionManager.registerAction("save_all", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    saveAll();
                } catch (IOException ex) {
                    ex.printStackTrace();
                    throw new RuntimeException("Error while thread", ex);
                }
            }
        });
    }

    private void save() throws IOException {
        AbstractEditor editor = getCurrentEditor();
        if (editor != null) {
            editor.save();
        }
    }

    private void saveAll() throws IOException {
        for (int i = 0; i < jtabbedPane.getTabCount(); i++) {
            AbstractEditor editor = (AbstractEditor) jtabbedPane.getComponentAt(i);
            if (editor != null) {
                editor.save();
            }
        }
    }

    public boolean closeAllEditorWindows() {
        System.out.println("Closing is not implemented yet");
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
            String message = "File " + StringUtils.abbreviateMiddle(abstractEditor.getFile().getName(), "...", 30) + " is modified. Save?";
            int result = JOptionPane.showOptionDialog(this, message, "Question", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);
            if (result == JOptionPane.CANCEL_OPTION) {
                return;
            }

            if (result == JOptionPane.YES_OPTION) {
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
}
