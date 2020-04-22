import java.awt.EventQueue;
import java.io.BufferedReader;
import java.io.FileReader;

import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import java.awt.BorderLayout;
import java.awt.Color;

public class Test {

    public static void main(String[] args) {
        new Test();
    }

    protected JEditorPane editor;

    public void updateHighlights() {
        Document document = editor.getDocument();
        try {

            editor.getHighlighter().removeAllHighlights();

            String find = "integer";
            for (int index = 0; index + find.length() <= document.getLength(); index++) {
                String match = document.getText(index, find.length());
                if (find.equalsIgnoreCase(match)) {
                    javax.swing.text.DefaultHighlighter.DefaultHighlightPainter highlightPainter =
                            new javax.swing.text.DefaultHighlighter.DefaultHighlightPainter(Color.YELLOW);
                    editor.getHighlighter().addHighlight(index, index + find.length(),
                            highlightPainter);
                }
            }
        } catch (BadLocationException ex) {
            ex.printStackTrace();
        }
    }



    public Test() {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
                }

                editor = new JEditorPane();

                try {
                    String text = "";
                    BufferedReader reader = new BufferedReader(new FileReader("../jni80/distro/C/0/JUKE.PAS"));
                    String line;
                    while( (line = reader.readLine()) != null ) {
                        text += line+"\r";
                    }
                    reader.close();
                    if ( text.length() > 1 ) {
                        text = text.substring(0, text.length()-1);
                    }
                    editor.setText(text);
                } catch (Exception exp) {
                    exp.printStackTrace();
                    editor.setText("test for Method, Method. And again Method");
                }

                JFrame frame = new JFrame();
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setLayout(new BorderLayout());
                frame.add(new JScrollPane(editor));
                frame.setSize(400, 400);
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);

                updateHighlights();

            }
        });
    }
}