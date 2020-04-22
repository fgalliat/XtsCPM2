import javax.swing.JFrame;
import javax.swing.JPanel;

import java.awt.Rectangle;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class Ide80Frame {

    public Ide80Frame() {

        JFrame win = new JFrame("IDE80");

        BufferedJavaPane editor = new BufferedJavaPane( new Rectangle(0, 0, 800, 600) );

        JPanel mainPan = new JPanel();
        mainPan.add(editor);
        win.setContentPane(mainPan);

        win.pack();

        win.addWindowListener(  new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
            public void windowOpened(WindowEvent e) {
                // necessary trick w/ linux & Java GUI
                // Cf KeyListener
                editor.requestFocus();
            } 
        } );

        win.setVisible(true);


    }



    public static void main(String[] args) {
        new Ide80Frame();
    }



}