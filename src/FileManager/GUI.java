package FileManager;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;

public class GUI extends JFrame{
	JPanel panel, topPanel, toolbar;
	JMenuBar menu, status;
	JDesktopPane desktop;
	File[] paths;
	JComboBox combo;
	FileFrame ff;
	boolean details = true;
	
	public GUI() {
		panel = new JPanel();
		topPanel = new JPanel();
		menu = new JMenuBar();
		toolbar = new JPanel();
		desktop = new JDesktopPane();
		status = new JMenuBar();
	}

	public void go() {
		this.setTitle("CECS 277 File Manger");
		panel.setLayout(new BorderLayout());
		topPanel.setLayout(new BorderLayout());

		buildMenu();
		buildToolBar();
		buildStatusBar();
		topPanel.add(desktop, BorderLayout.CENTER);
		ff = new FileFrame(paths[0]);
		desktop.add(ff);

		panel.add(topPanel, BorderLayout.CENTER);
		this.add(panel);
		this.setSize(1300, 800);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setVisible(true);

	}

	public void revalidateAndPaint() {
		this.revalidate();
		this.repaint();
	}
	private void buildToolBar() {
		paths = File.listRoots();
		combo = new JComboBox(paths);
		JButton simple = new JButton("Simple");
		JButton details = new JButton("Details");

		combo.addItemListener(new ComboActionListener());
		simple.addActionListener(new SDActionListener());
		details.addActionListener(new SDActionListener());

		toolbar.add(combo);
		toolbar.add(simple);
		toolbar.add(details);

		topPanel.add(toolbar, BorderLayout.NORTH);
	}

	private void buildMenu() {
		JMenu file, help, window, tree;
		// Sets up menu titles
		file = new JMenu("File");
		help = new JMenu("Help");
		window = new JMenu("Window");
		tree = new JMenu("Tree");

		// Creates file items and listeners and adds to file
		JMenuItem rename = new JMenuItem("Rename");
		JMenuItem copy = new JMenuItem("Copy");
		JMenuItem delete = new JMenuItem("Delete");
		JMenuItem run = new JMenuItem("Run");
		JMenuItem exit = new JMenuItem("Exit");
		rename.addActionListener(new MultiActionListener());
		copy.addActionListener(new MultiActionListener());
		delete.addActionListener(new DeleteActionListener());
		run.addActionListener(new RunActionListener());
		exit.addActionListener(new ExitActionListener());
		file.add(rename);
		file.add(copy);
		file.add(delete);
		file.add(run);
		file.add(exit);

		// Creates tree items and listeners and adds to tree
		JMenuItem expand = new JMenuItem("Expand Branch");
		JMenuItem collapse = new JMenuItem("Collapse Branch");
		expand.addActionListener(new ExpandActionListener());
		collapse.addActionListener(new CollapseActionListener());
		tree.add(expand);
		tree.add(collapse);

		// Creates window items and listeners and adds to window
		JMenuItem newW = new JMenuItem("New");
		JMenuItem cascade = new JMenuItem("Cascade");
		newW.addActionListener(new NewActionListener());
		cascade.addActionListener(new CascadeActionListener());
		window.add(newW);
		window.add(cascade);

		// Creates help items and listeners and adds to help
		JMenuItem helpH = new JMenuItem("Help");
		JMenuItem about = new JMenuItem("About");
		helpH.addActionListener(new HelpActionListener());
		about.addActionListener(new AboutActionListener());
		help.add(helpH);
		help.add(about);

		menu.add(file);
		menu.add(tree);
		menu.add(window);
		menu.add(help);
		panel.add(menu, BorderLayout.NORTH);
	}

	public void buildStatusBar() {
		status.removeAll();
		File f = (File) combo.getSelectedItem();
		JLabel size = new JLabel("Current Drive: " + f
				+ "     Free Space: " + f.getFreeSpace()/(1024*1024*1024)
				+ "GB     Used Space: " + (f.getTotalSpace() - f.getFreeSpace())/(1024*1024*1024)
				+ "GB     Total Space: " + f.getTotalSpace()/(1024*1024*1024) + "GB");
		status.add(size);
		topPanel.add(status, BorderLayout.SOUTH);
	}


	private class ExitActionListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			System.exit(0);
		}
	}


	private class MultiActionListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			String title = "";
			if (e.getActionCommand().equals("Rename")){
				title = "Renaming";
			} else if (e.getActionCommand().equals("Copy")) {
				title = "Copying";
			}
			Dialog dlg = new Dialog(title);
			FileFrame ff = (FileFrame) desktop.getSelectedFrame();
			File fromFile = (File) ff.fileP.list.getSelectedValue();
			dlg.setFromField(fromFile.getName());
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) ff.dirPanel.dirTree.getLastSelectedPathComponent();
			FileNode currentDirectoryNode = (FileNode) node.getUserObject();
			File currentDirectory = (File) currentDirectoryNode.getFile();
			dlg.setCurrentDirectory(currentDirectory.getPath());
			dlg.setVisible(true);

			int selected = ff.dirPanel.dirTree.getMinSelectionRow();
			ff.dirPanel.dirTree.setSelectionRow(selected +1);
			ff.dirPanel.dirTree.setSelectionRow(selected);
		}
	}

	private class RunActionListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			executeFile((File) ff.fileP.list.getSelectedValue());
		}
	}

	private class DeleteActionListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			int result = JOptionPane.showConfirmDialog(ff.dirPanel.dirTree,
					"Delete " + ff.fileP.list.getSelectedValue(), "Deleting",
					JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE);
			if(result == JOptionPane.YES_OPTION){
				//label.setText("You selected: Yes");
				File f = (File) ff.fileP.list.getSelectedValue();
				f.delete();
			}
			int selected = ff.dirPanel.dirTree.getMinSelectionRow();
			ff.dirPanel.dirTree.setSelectionRow(selected +1);
			ff.dirPanel.dirTree.setSelectionRow(selected);
		}
	}

	public void executeFile(File file) {
		Desktop desktop = Desktop.getDesktop();
		try {
			desktop.open(file);
		} catch (IOException ex) {
			System.out.println(ex);
		}
	}

	private class ComboActionListener implements ItemListener {
		@Override
		public void itemStateChanged(ItemEvent e) {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				File f = (File) combo.getSelectedItem();
				buildStatusBar();
				topPanel.revalidate();
				topPanel.repaint();

				FileFrame ff2 = new FileFrame(f);

				desktop.add(ff2);
				ff2.moveToFront();
				revalidateAndPaint();
			}
		}
	}

	private class SDActionListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getActionCommand().equals("Simple")) {
				System.out.println("Simple");
				ff.fileP.details = false;
				ff.fileP.revalidate();
				ff.fileP.repaint();
			} else if (e.getActionCommand().equals("Details")) {
				System.out.println("Details");
				ff.fileP.details = true;
				ff.fileP.revalidate();
				ff.fileP.repaint();
			}
		}
	}

	private class ExpandActionListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			ff.dirPanel.dirTree.expandRow(ff.dirPanel.dirTree.getMaxSelectionRow());
		}
	}

	private class CollapseActionListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			ff.dirPanel.dirTree.collapseRow(ff.dirPanel.dirTree.getMaxSelectionRow());
		}
	}

	private class NewActionListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			File f = (File) combo.getSelectedItem();
			FileFrame ff2 = new FileFrame(f);

			desktop.add(ff2);
			ff2.moveToFront();
			revalidateAndPaint();
		}
	}

	private class CascadeActionListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			int x = 0; int y = 0;
			for (JInternalFrame jframe : desktop.getAllFrames()) {
				jframe.setLocation(x,y);
				x+=30;
				y+=30;
				jframe.moveToFront();
			}
		}
	}

	private class HelpActionListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			NoHelp dlg = new NoHelp();
			dlg.setVisible(true);
		}
	}

	private class AboutActionListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			AboutBox dlg = new AboutBox();
			dlg.setVisible(true);
		}
	}
}
