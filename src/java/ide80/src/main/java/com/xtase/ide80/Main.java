package com.xtase.ide80;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Graphics;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Method;

public class Main extends JFrame {

    /**
     * assumes that class has a noArg constructor<br/>
     * assumes that method is not overloaded ...<br/>
     * and requires one String arg
     */
    protected static Object invokeMethodOnClass(String className, String methodName, String value) throws Exception {
        Class clazz = Class.forName(className);

        // needs a no-params constructor
        Object instance = clazz.newInstance();

        Method foundMeth = null;
        Method[] meths = clazz.getDeclaredMethods();
        for (Method meth : meths) {
            if (meth.getName().equals(methodName)) {
                foundMeth = meth;
                break;
            }
        }

        return foundMeth.invoke(instance, new Object[] { value });
    }

    public static void main(String[] args) {
        String path = "c:bmp.pas";
        if (args != null && args.length > 0) {
            path = args[0];
        }
        new Main(path);
    }

    // ============================
    protected JTextArea console = null;

    protected void status(Object o) {
        console.append(""+o+"\n");
    }

    public Main(String cpmPath) {
        super("IDE80 ("+ cpmPath +")");

        // ===========================
        // Dark Laf
        try {
            UIManager.setLookAndFeel(new com.formdev.flatlaf.FlatDarkLaf());
        } catch (Exception ex) {
            status("Failed to initialize LaF");
        }
        // ===========================

        console = new JTextArea("");
        console.setFont(new Font("Monospaced", Font.PLAIN, 12));

        PascalTextPane textPane = new PascalTextPane();
        JScrollPane scroller = new JScrollPane(textPane);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scroller, console) {
            private boolean hasProportionalLocation = false;
            private double proportionalLocation = 0.5;
            private boolean isPainted = false;

            public void setDividerLocation(double proportionalLocation) {
                if (!isPainted) {
                    hasProportionalLocation = true;
                    this.proportionalLocation = proportionalLocation;
                } else {
                    super.setDividerLocation(proportionalLocation);
                }
            }

            public void paint(Graphics g) {
                super.paint(g);
                if (!isPainted) {
                    if (hasProportionalLocation) {
                        super.setDividerLocation(proportionalLocation);
                    }
                    isPainted = true;
                }
            }
        };
        splitPane.setOneTouchExpandable(true);
        splitPane.setDividerLocation(0.75);

        JPanel mainPane = new JPanel();
        mainPane.setLayout(new BorderLayout());
        mainPane.add(splitPane, BorderLayout.CENTER);
        setContentPane(mainPane);

        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);

        File fileToEdit = null;

        // new com.xtase.jni80.XtsJ80FileSystem().resolveCPMPath("c:test.pas");

        try {
            File file = (File) invokeMethodOnClass("com.xtase.jni80.XtsJ80FileSystem", "resolveCPMPath", cpmPath);
            status("CPM File : " + file.getPath());
            fileToEdit = file;
        } catch (Exception ex) {
            status("Could not get CPM path [" + ex.toString() + "]");
        }

        // try {
        // File file = (File) invokeMethodOnClass("XtsJ80FileSystem", "resolveCPMPath",
        // fileToCompile);
        // System.out.println("CPM File : " + file.getPath());
        // fileToEdit = file;

        // invokeMethodOnClass("JavaPascalCompiler", "compile", fileToCompile);

        // System.out.println("JavaPascalCompiler is available ;-) ");
        // } catch (ClassNotFoundException ex) {
        // System.out.println("JavaPascalCompiler is not available :-( ");
        // } catch (InstantiationException | IllegalAccessException ex) {
        // System.out.println("JavaPascalCompiler is not instanciable :-( ");
        // } catch (InvocationTargetException ex) {
        // System.out.println("JavaPascalCompiler.compile() is not invokable :-( ");
        // } catch (Exception ex) {
        // System.out.println("JavaPascalCompiler.compile() failed ");
        // }

        try {
            String text = "";

            String path = null;
            if (fileToEdit != null && fileToEdit.exists()) {
                // path = "./C/0/JUKE.PAS";
                path = fileToEdit.getPath();
            } else {
                path = "../jni80/distro/C/0/JUKE.PAS";
                status("Could not open file, select a default one");
            }

            BufferedReader reader = new BufferedReader(new FileReader(path));
            String line;
            while ((line = reader.readLine()) != null) {
                // text += line+"\r";
                text += line + "\n"; // BEWARE @ SAVE !!!!!
            }
            reader.close();
            if (text.length() > 1) {
                text = text.substring(0, text.length() - 1);
            }

            // System.out.println(text.substring(0, 250));

            textPane.setText(text);
        } catch (Exception exp) {
            exp.printStackTrace();
            textPane.setText("File failed to open");
        }

    }

}