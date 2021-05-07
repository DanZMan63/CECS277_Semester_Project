package FileManager;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragSource;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import javax.swing.plaf.metal.MetalIconFactory;
import javax.swing.tree.DefaultMutableTreeNode;


/**
 *
 * @author Dr. Hoffman
 */

public class FilePanel extends JPanel {
    JList list = new JList();
    DefaultListModel model = new DefaultListModel();
    JScrollPane scroll = new JScrollPane();
    boolean details = true;
    JPopupMenu pop = new JPopupMenu();
    File currentDirectory;

    public FilePanel() {
        this.setDropTarget(new MyDropTarget());
        list.setDragEnabled(true);
        list.setDropMode(DropMode.INSERT);
        Map<Object, Icon> icons = new HashMap<Object, Icon>();
        icons.put("file", MetalIconFactory.getTreeLeafIcon());
        icons.put("folder", MetalIconFactory.getTreeFolderIcon());
        list.setCellRenderer(new IconListRenderer(icons));
        list.addMouseListener(new PopMouseListener());

        scroll.setViewportView(this.list);
        list.setModel(model);
        scroll.setPreferredSize(new Dimension(650, 550));
        add(scroll);

        JMenuItem rename = new JMenuItem("Rename");
        JMenuItem copy = new JMenuItem("Copy");
        JMenuItem delete = new JMenuItem("Delete");
        rename.addActionListener(new RenameActionListener());
        copy.addActionListener(new CopyActionListener());
        delete.addActionListener(new DeleteActionListener());
        pop.add(rename);
        pop.add(copy);
        pop.add(delete);
    }

    public void addJList(JList jlist, File directory) {
        currentDirectory = directory;
        this.removeAll();
        list = jlist;
        if (list.getModel().getSize() > 0) {
            scroll.setViewportView(this.list);
            scroll.setPreferredSize(new Dimension(650, 550));
            add(scroll);
        } else {
            add(jlist);
        }
        list.addMouseListener(new PopMouseListener());
        list.setDragEnabled(true);
        list.setDropMode(DropMode.INSERT);

        Map<Object, Icon> icons = new HashMap<Object, Icon>();
        icons.put("file", MetalIconFactory.getTreeLeafIcon());
        icons.put("folder", MetalIconFactory.getTreeFolderIcon());
        list.setCellRenderer(new IconListRenderer(icons));

        revalidate();
        repaint();
    }

    public void setDetails(boolean details) {
        this.details = details;
    }
    /*************************************************************************
     * class MyDropTarget handles the dropping of files onto its owner
     * (whatever JList it is assigned to). As written, it can process two
     * types: strings and files (String, File). The String type is necessary
     * to process internal source drops from another FilePanel object. The
     * File type is necessary to process drops from external sources such
     * as Windows Explorer or IOS.
     *
     * Note: no code is provided that actually copies files to the target
     * directory. Also, you may need to adjust this code if your list model
     * is not the default model. JList assumes a toString operation is
     * defined for whatever class is used.
     */
    class MyDropTarget extends DropTarget {
        /**************************************************************************
         *
         * @param evt the event that caused this drop operation to be invoked
         */
        public void drop(DropTargetDropEvent evt){
            try {
                //types of events accepted
                evt.acceptDrop(DnDConstants.ACTION_COPY);
                //storage to hold the drop data for processing
                List result = new ArrayList();
                //what is being dropped? First, Strings are processed
                if(evt.getTransferable().isDataFlavorSupported(DataFlavor.stringFlavor)){
                    String temp = (String)evt.getTransferable().getTransferData(DataFlavor.stringFlavor);
                    //String events are concatenated if more than one list item
                    //selected in the source. The strings are separated by
                    //newline characters. Use split to break the string into
                    //individual file names and store in String[]
                    String[] next = temp.split("\\n");
                    //add the strings to the listmodel
                    for(int i=0; i<next.length;i++) {
                        System.out.println("dnd: " + next[i].toString());
                        model.addElement(next[i]);

                        File fromFile = new File(next[i].toString());
                        String toFile = currentDirectory + "\\" + fromFile.getName();
                        Dialog.copyFile(next[i].toString(), toFile);
                    }
                }
                else{ //then if not String, Files are assumed
                    result =(List)evt.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                    //process the input
                    for(Object o : result){
                        System.out.println("dnd: " + o.toString());
                        model.addElement(o);

                        File fromFile = new File(o.toString());
                        String toFile = currentDirectory + "\\" + fromFile.getName();
                        Dialog.copyFile(o.toString(), toFile);
                    }
                }
                //Refresh
                File[] fileList = currentDirectory.listFiles();
                JList jList = null;
                if (fileList != null) {
                    jList = new JList(fileList);
                } else {
                    jList = new JList();
                }
                addJList(jList,currentDirectory);
            }
            catch(Exception ex){
                ex.printStackTrace();
            }
        }

    }

    private class IconListRenderer extends DefaultListCellRenderer {
        private Map<Object, Icon> icons = null;

        public IconListRenderer(Map<Object, Icon> icons) {
            this.icons = icons;
        }
        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {

            File f = (File) value;
            String s = "";
            SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
            DecimalFormat dformat = new DecimalFormat("#,###");
            if (details && !f.isDirectory()) {
                s = f.getName() + "     " + formatter.format(f.lastModified()) + "     " + dformat.format(f.length());
            } else {
                s = f.getName();
            }
            value = s;
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            String iconName = "file";
            if (f.isDirectory()) {
                iconName = "folder";
            }
            Icon icon = icons.get(iconName);
            label.setIcon(icon);
            return label;
        }
    }

    private class RenameActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            Dialog dlg = new Dialog("Renaming");
            File fromFile = (File) list.getSelectedValue();
            dlg.setFromField(fromFile.getName());
            dlg.setCurrentDirectory(currentDirectory.getPath());
            dlg.setVisible(true);

            File[] fileList = currentDirectory.listFiles();
            JList jList = null;
            if (fileList != null) {
                jList = new JList(fileList);
            } else {
                jList = new JList();
            }
            addJList(jList,currentDirectory);
        }
    }

    private class CopyActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            Dialog dlg = new Dialog("Copying");
            File fromFile = (File) list.getSelectedValue();
            dlg.setFromField(fromFile.getName());
            dlg.setCurrentDirectory(currentDirectory.getPath());
            dlg.setVisible(true);

            File[] fileList = currentDirectory.listFiles();
            JList jList = null;
            if (fileList != null) {
                jList = new JList(fileList);
            } else {
                jList = new JList();
            }
            addJList(jList,currentDirectory);
        }
    }

    private class DeleteActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            int result = JOptionPane.showConfirmDialog(list,
                    "Delete " + list.getSelectedValue(), "Deleting",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
            if(result == JOptionPane.YES_OPTION){
                //label.setText("You selected: Yes");
                File f = (File) list.getSelectedValue();
                f.delete();
            }
            File[] fileList = currentDirectory.listFiles();
            JList jList = null;
            if (fileList != null) {
                jList = new JList(fileList);
            } else {
                jList = new JList();
            }
            addJList(jList,currentDirectory);
        }
    }

    private class PopMouseListener extends MouseAdapter {

        @Override
        public void mouseClicked(MouseEvent evt) {
            if (evt.getClickCount() == 2) {

                // Double-click detected
                int index = list.locationToIndex(evt.getPoint());
                list.setSelectedIndex(index);
                File f = (File) list.getSelectedValue();
                Desktop desktop = Desktop.getDesktop();
                try {
                    desktop.open(f);
                } catch (IOException ex) {
                    System.out.println(ex);
                }
            }
        }
        @Override
        public void mousePressed(MouseEvent e) {
            check(e);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            check(e);
        }

        public void check(MouseEvent e) {
            System.out.println("check");
            if (e.isPopupTrigger()) {
                list.setSelectedIndex(list.locationToIndex(e.getPoint()));
                pop.show(list, e.getX(), e.getY());
            }
        }
    }
}
