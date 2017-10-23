package org.visualeagle.gui.projectnavigation;

import org.visualeagle.gui.editorwindow.EditorWindow;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import org.visualeagle.project.projectloaders.ProjectStructure;
import org.visualeagle.project.vnodes.AbstractVNode;
import org.visualeagle.project.vnodes.VirtualFolderVNode;
import org.visualeagle.utils.ImageManager;
import org.visualeagle.utils.Lookup;

/**
 * @author Dmitry
 */
public class ProjectNavigationWindow extends JInternalFrame {

    private JTree jtree;
    private DefaultMutableTreeNode root;

    public ProjectNavigationWindow(String title, boolean resizable, boolean closable, boolean maximizable, boolean iconifiable) {
        super(title, resizable, closable, maximizable, iconifiable);
        initGui();
    }

    private void initGui() {
        JScrollPane scrollPane = new JScrollPane();
        root = new DefaultMutableTreeNode();
        root.setAllowsChildren(true);
        jtree = new JTree(root);
        jtree.setRootVisible(false);
        jtree.setShowsRootHandles(true);
        jtree.setCellRenderer(createCellRenderer());
        jtree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        jtree.addTreeWillExpandListener(new TreeWillExpandListener() {
            @Override
            public void treeWillExpand(TreeExpansionEvent event) throws ExpandVetoException {
                onTreeWillExpand(event);
            }

            @Override
            public void treeWillCollapse(TreeExpansionEvent event) throws ExpandVetoException {
                onTreeWillCollapse(event);
            }
        });

        jtree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() >= 2) {
                    try {
                        onTreeItemSelected();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

        scrollPane.getViewport().add(jtree);
        setLayout(new BorderLayout(0, 0));
        add(scrollPane);
    }

    private void onTreeItemSelected() throws IOException {
        TreePath selectionPath = jtree.getSelectionPath();
        if (selectionPath != null) {
            DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) selectionPath.getLastPathComponent();
            AbstractVNode vnode = (AbstractVNode) treeNode.getUserObject();
            if (!vnode.isLeaf()) {
                return;
            }

            EditorWindow editorWindow = Lookup.get().get(EditorWindow.class);
            editorWindow.editFile(vnode);
        }
    }

    private void sortVNodes(List<AbstractVNode> files) {

        files.sort(new Comparator<AbstractVNode>() {
            @Override
            public int compare(AbstractVNode o1, AbstractVNode o2) {
                if (o1.isLeaf() == o2.isLeaf()) {
                    return o1.getName().compareToIgnoreCase(o2.getName());
                }
                if (!o1.isLeaf()) {
                    return -1;
                }
                return 1;
            }
        });

    }

    private void onTreeWillExpand(TreeExpansionEvent event) {
        DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) event.getPath().getLastPathComponent();
        AbstractVNode vnode = (AbstractVNode) treeNode.getUserObject();
        treeNode.removeAllChildren();
        if (!vnode.isLeaf()) {
            List<AbstractVNode> children = vnode.getChildren();
            if (!(vnode instanceof VirtualFolderVNode)) {
                sortVNodes(children);
            }
            for (AbstractVNode child : children) {

                DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(child);
                childNode.setAllowsChildren(!child.isLeaf());
                if (!child.isLeaf()) {
                    childNode.add(createDummyTreeNode());
                } else {
                    String extension = child.getExtension();
                    if (extension.equals("h") || extension.equals("hpp")) {
                        child.setIcon(ImageManager.get().getIcon("fileH"));
                    }else
                    if (extension.equals("c")) {
                        child.setIcon(ImageManager.get().getIcon("fileC"));
                    }else
                    if (extension.equals("cpp")) {
                        child.setIcon(ImageManager.get().getIcon("fileCpp"));
                    }
                }

                treeNode.add(childNode);
            }
        }
    }

    private void onTreeWillCollapse(TreeExpansionEvent event) {

    }

    private DefaultMutableTreeNode createDummyTreeNode() {
        return new DefaultMutableTreeNode("DUMMY_NODE");
    }

    public void closeCurrentProject() {
        root.removeAllChildren();
    }

    public void loadProject(ProjectStructure projectStructure) {
        DefaultMutableTreeNode projectRootNode = new DefaultMutableTreeNode(projectStructure.getRootProjectVFile());
        projectRootNode.setUserObject(projectStructure.getRootProjectVFile());
        root = new DefaultMutableTreeNode();
        root.setAllowsChildren(true);
        root.add(projectRootNode);
        projectRootNode.add(createDummyTreeNode());
        DefaultTreeModel treeModel = (DefaultTreeModel) jtree.getModel();
        treeModel.setRoot(root);
    }

    private DefaultTreeCellRenderer createCellRenderer() {
        return new DefaultTreeCellRenderer() {
            @Override
            public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                JLabel component = (JLabel) super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus); //To change body of generated methods, choose Tools | Templates.
                DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) value;
                if (!(treeNode.getUserObject() instanceof AbstractVNode)) {
                    return component;
                }

                AbstractVNode vnode = (AbstractVNode) treeNode.getUserObject();
                if (vnode == null) {
                    return component;
                }

                component.setIcon(vnode.getIcon());
                component.setText(vnode.getName());
                return component;
            }
        };
    }
}
