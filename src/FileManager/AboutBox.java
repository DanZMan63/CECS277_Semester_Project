package FileManager;

import javax.swing.*;
import java.awt.event.*;

public class AboutBox extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;

    public AboutBox() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        this.setSize(300, 200);

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });
    }

    private void onOK() {
        // add your code here
        dispose();
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }

    public static void main(String[] args) {
        AboutBox dialog = new AboutBox();
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }
}
