package FileManager;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.io.File;

public class DirectoryPanel extends JPanel {
    private JScrollPane scrollPane = new JScrollPane();
    JTree dirTree = new JTree();
    DefaultTreeModel treeModel;
    FilePanel filePanel;
    File rootD;

    public DirectoryPanel(FilePanel fileP, File rootDirectory){
        filePanel = fileP;
        rootD = rootDirectory;
        dirTree.addTreeSelectionListener(new DirTreeSelectionListener());
        dirTree.addTreeWillExpandListener(new DirTreeExpansionListener());
        buildTree(rootDirectory);
        scrollPane.setViewportView(this.dirTree);
        scrollPane.setPreferredSize(new Dimension(300, 550));
        add(scrollPane);
    }

    public void buildTree(File file) {
        File rootFile = file;
        FileNode rootFileNode = new FileNode(rootFile.getName(), rootFile);
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(rootFileNode);
        treeModel = new DefaultTreeModel(root);

        scanDirectory(root, 2);
        dirTree.setModel(treeModel);
        dirTree.setSelectionRow(0);
    }

    public void scanDirectory(DefaultMutableTreeNode n, int level) {
        if (level == 0) {return;}
        n.removeAllChildren();

        FileNode filennode = (FileNode) n.getUserObject();
        File root = filennode.getFile();
        DefaultMutableTreeNode dir = null;
        File[] dirs1 = root.listFiles();
        if (dirs1 != null) {
            for (int i=0; i<dirs1.length; i++) {
                if(dirs1[i].isDirectory()) {
                    //System.out.println(dirs[i].getAbsolutePath());
                    FileNode fn = new FileNode(dirs1[i].getName(), dirs1[i]);
                    dir = new DefaultMutableTreeNode(fn);
                    n.add(dir);
                    scanDirectory(dir,level-1);
                }
            }
        }

    }

    private class DirTreeSelectionListener implements TreeSelectionListener {

        @Override
        public void valueChanged(TreeSelectionEvent e) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) dirTree.getLastSelectedPathComponent();
            if (node == null) {
                return;
            }
            FileNode fn = (FileNode) node.getUserObject();
            System.out.println(fn.getFile().getAbsolutePath());
            scanDirectory(node, 2);

            File[] fileList = fn.getFile().listFiles();
            JList jList = null;
            if (fileList != null) {
                jList = new JList(fileList);
            } else {
                jList = new JList();
            }
            filePanel.addJList(jList,fn.getFile());
        }
    }

    private class DirTreeExpansionListener implements TreeWillExpandListener {

        @Override
        public void treeWillExpand(TreeExpansionEvent event) throws ExpandVetoException {
            TreePath path = event.getPath();
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
            if (node == null) {
                return;
            }
            scanDirectory(node, 2);
        }

        @Override
        public void treeWillCollapse(TreeExpansionEvent event) throws ExpandVetoException {

        }
    }
}

