
import java.awt.GridLayout;
import java.io.BufferedReader;
import java.io.FileReader;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class IDE80 extends JFrame {

    public static void main(String[] args) {
        IDE80 xmlEditor = new IDE80();
        xmlEditor.setVisible(true);
    }

    public IDE80() {

        super("Xtase Turbo Pascal Editor (IDE80)");
        setSize(800, 600);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout());

        PascalTextPane textPane = new PascalTextPane();

        JScrollPane scroller = new JScrollPane(textPane);
        panel.add(scroller);

        add(panel);

        try {
            String text = "";
            BufferedReader reader = new BufferedReader(new FileReader("../jni80/distro/C/0/JUKE.PAS"));
            String line;
            while( (line = reader.readLine()) != null ) {
                // text += line+"\r";
                text += line+"\n"; // BEWARE @ SAVE !!!!!
            }
            reader.close();
            if ( text.length() > 1 ) {
                text = text.substring(0, text.length()-1);
            }

            // System.out.println(text.substring(0, 250));


            textPane.setText(text);
        } catch (Exception exp) {
            exp.printStackTrace();
            textPane.setText("test for Method, Method. And again Method");
        }

    }
}
