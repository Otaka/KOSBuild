package org.visualeagle.gui.small.directorychooser;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import org.visualeagle.utils.ImageManager;
import org.visualeagle.utils.Utils;

/**
 * @author Dmitry
 */
public class DirectoryChooser {

    private Icon fileIcon;
    private Icon folderIcon;
    private File currentDir = new File(".").getAbsoluteFile();
    private String title = "Choose folder...";
    private boolean showHidden = false;
    private OnFileFound onFileFoundEvent;
    private CheckAllowToSelectCallback checkAllowToSelectCallback;
    private File selectedDir;
    private JDialog dialog;
    private boolean userAccepted = false;
    private JTree fileTree;

    public DirectoryChooser() {
        checkAllowToSelectCallback = createDefaultCheckAllowToSelectCallback();
        fileIcon = ImageManager.get().getIcon("file");
        folderIcon = ImageManager.get().getIcon("folder");
    }

    public boolean isShowHidden() {
        return showHidden;
    }

    public void setShowHidden(boolean showHidden) {
        this.showHidden = showHidden;
    }

    public void setCurrentDir(File currentDir) {
        this.currentDir = currentDir;
    }

    public final void setOnFileFoundEvent(OnFileFound onFileFoundEvent) {
        this.onFileFoundEvent = onFileFoundEvent;
    }

    public final void setCheckAllowToSelectCallback(CheckAllowToSelectCallback checkAllowToSelectCallback) {
        this.checkAllowToSelectCallback = checkAllowToSelectCallback;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public File chooseFolder(Frame frame) {
        if (onFileFoundEvent == null) {
            throw new IllegalStateException("Cannot execute DirectoryChooser because you have not provided OnFileFoundEvent");
        }

        selectedDir = null;
        userAccepted = false;

        dialog = new JDialog(frame);
        dialog.setContentPane(createContentPanel());
        dialog.pack();
        dialog.setTitle(title);
        dialog.setModal(true);
        dialog.setLocationRelativeTo(frame);
        dialog.setVisible(true);

        if (userAccepted == false) {
            return null;
        }

        return selectedDir;
    }

    private DefaultMutableTreeNode createDummyNode() {
        return new DefaultMutableTreeNode();
    }

    private CheckAllowToSelectCallback createDefaultCheckAllowToSelectCallback() {
        return (TreeElement treeElement) -> true; //default check that always returns true
    }

    private JPanel createContentPanel() {
        JPanel panel = new JPanel();
        DefaultMutableTreeNode top = new DefaultMutableTreeNode(new TreeElement(null, "top", new File("."), true));
        for (File root : File.listRoots()) {
            TreeElement treeElement = new TreeElement(null, root.getAbsolutePath(), root, true);
            DefaultMutableTreeNode mutableTreeNode = new DefaultMutableTreeNode(treeElement, true);
            if (root.isDirectory()) {
                mutableTreeNode.add(createDummyNode());
            }

            top.add(mutableTreeNode);
        }

        fileTree = new JTree(top);
        fileTree.setCellRenderer(createCellRenderer());
        fileTree.setRootVisible(false);
        fileTree.setShowsRootHandles(true);
        fileTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        fileTree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    if (selectedDir != null) {
                        userAccepted = true;
                        dialog.setVisible(false);
                    }
                }
            }
        });
        fileTree.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    if (selectedDir != null) {
                        userAccepted = true;
                        dialog.setVisible(false);
                    }
                }
            }
        });
        fileTree.addTreeSelectionListener((TreeSelectionEvent e) -> {
            TreePath treePath = e.getPath();
            DefaultMutableTreeNode mutableTreeNode = (DefaultMutableTreeNode) treePath.getLastPathComponent();
            TreeElement treeElement = (TreeElement) mutableTreeNode.getUserObject();
            if (checkAllowToSelectCallback.onSelect(treeElement)) {
                selectedDir = treeElement.getFile();
            } else {
                selectedDir = null;
            }
        });
        fileTree.addTreeWillExpandListener(new TreeWillExpandListener() {
            @Override
            public void treeWillExpand(TreeExpansionEvent event) throws ExpandVetoException {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) event.getPath().getLastPathComponent();
                node.removeAllChildren();
                TreeElement treeElement = (TreeElement) node.getUserObject();
                File[] children = treeElement.getFile().listFiles();
                if (children == null) {
                    return;
                }
                Utils.sortFiles(children);
                for (File f : children) {
                    if (!isShowHidden() && f.isHidden()) {
                        continue;
                    }
                    TreeElement newTreeElement = onFileFoundEvent.onFileFound(f);
                    if (newTreeElement != null) {
                        DefaultMutableTreeNode newTreeNode = new DefaultMutableTreeNode(newTreeElement);
                        if (newTreeElement.shouldHaveChildren) {
                            newTreeNode.add(createDummyNode());
                        }

                        node.add(newTreeNode);
                    }
                }
            }

            @Override
            public void treeWillCollapse(TreeExpansionEvent event) throws ExpandVetoException {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) event.getPath().getLastPathComponent();
                node.removeAllChildren();
                node.add(createDummyNode());
            }
        });
        panel.setLayout(new BorderLayout(0, 0));
        JScrollPane scrollPane = new JScrollPane(fileTree);
        panel.add(scrollPane);
        panel.setPreferredSize(new Dimension(500, 600));
        return panel;
    }

    private DefaultTreeCellRenderer createCellRenderer() {
        return new DefaultTreeCellRenderer() {
            @Override
            public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                JLabel component = (JLabel) super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus); //To change body of generated methods, choose Tools | Templates.
                DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) value;
                TreeElement treeElement = (TreeElement) treeNode.getUserObject();
                if (treeElement == null) {
                    return component;
                }
                if (treeElement.getIcon() != null) {
                    component.setIcon(treeElement.getIcon());
                } else if (!treeElement.isShouldHaveChildren()) {
                    component.setIcon(fileIcon);
                } else {
                    component.setIcon(folderIcon);
                }
                component.setText(treeElement.getText());
                return component;
            }
        };
    }
}
