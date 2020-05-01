package com.xtase.ide80;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.DefaultCaret;

import com.xtase.ide80.components.PascalTextPane;
import com.xtase.jni80.JavaPascalCompiler;
import com.xtase.jni80.JavaRunCPM_GFX;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.ActionEvent;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    protected OutputStream consoleOut = new OutputStream() {
        @Override
        public void write(int b) throws IOException {
            if (b < 0 || b > 255) {
                return;
            }
            if (b == '\r') {
                b = (int) '\n';
            }
            console.append("" + (char) b);
        }
    };
    protected PrintStream consoleStream = new PrintStream(consoleOut);

    public void compileCpmTp3(String cpmPath, boolean inMemOnly) {
        try {
            // invokeMethodOnClass("com.xtase.jni80.JavaPascalCompiler", "compile",
            // cpmPath);
            // boolean inMemOnly = true;

            if (inMemOnly) {
                System.out.println("Will compile in Memory only");
            } else {
                System.out.println("Will compile on Disk");
            }
            new JavaPascalCompiler().compile(cpmPath, inMemOnly);
        } catch (Exception ex) {
            status(ex.toString());
        }
    }

    public void runCpmTp3(String cpmPath) {
        // c:bmp.pas => "c:bmp" (not c:bmp.com)
        final String _cpmPath = getCurCpmPath().replaceAll(Pattern.quote(".PAS"), Matcher.quoteReplacement(""));

        try {
            System.out.println("will exec : " + _cpmPath);
            new Thread() {
                public void run() {
                    try {

                        if (!(_cpmPath.charAt(1) == ':')) {
                            throw new IOException("Invalid CPM path (" + _cpmPath + ")");
                        }

                        // 1st : enter on proper drive
                        // 2nd : launch executable
                        String autorun = _cpmPath.charAt(0) + ":" + "\r" + _cpmPath.substring(2) + "\r";

                        new JavaRunCPM_GFX().runCPM(autorun, false);
                    } catch (Exception ex) {
                        // status(ex.toString());
                        System.out.println(ex.toString());
                    }
                }
            }.start();
        } catch (Exception ex) {
            System.out.println(ex.toString());
        }
    }

    // ============================
    protected JTextArea console = null;
    // protected String curCpmPath = null;

    protected void status(Object o) {
        try {
            console.append("" + o + "\n");
        } catch (Exception ex) {
            System.out.println("(DBG) " + o);
        }
    }

    // =========================================================
    // =========================================================

    public String getCurCpmPath() {
        return "c:bmp.pas";
    }

    public JComponent makeAnEditor(String cpmPath) {
        cpmPath = cpmPath.toUpperCase();

        CodeEditor editor = new CodeEditor(this);
        editor.load(cpmPath);

        return editor;
    }

    JTabbedPane tabbedPane = null;

    public void openFile(String cpmPath) {
        cpmPath = cpmPath.toUpperCase();
        tabbedPane.addTab(cpmPath.substring(2), null, makeAnEditor(cpmPath), cpmPath);
        tabbedPane.setSelectedIndex( tabbedPane.getTabCount()-1 );
    }

    protected String promptValue(String message) {
        String name = JOptionPane.showInputDialog(this, message);
        return name;
    }

    public Main(final String cpmPath) {
        super("IDE80 (" + cpmPath.toUpperCase() + ")");

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
        // === autoscroll ====
        DefaultCaret caret = (DefaultCaret) console.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        // ===================

        // ==== System Output ========
        System.setOut(consoleStream);
        // ==== System Output ========

        JScrollPane conScroller = new JScrollPane(console);

        tabbedPane = new JTabbedPane();
        tabbedPane.addChangeListener( new ChangeListener(){
            @Override
            public void stateChanged(ChangeEvent e) {
                // int idx = tabbedPane.getSelectedIndex();
                CodeEditor editor = (CodeEditor)tabbedPane.getSelectedComponent();
                // System.out.println("now selected tab#"+idx);
                Main.this.setTitle("IDE80 ("+ editor.getCpmPath() +")");
            }
        } );
        openFile(cpmPath);

        JPanel editorPane = new JPanel();
        editorPane.setLayout(new BorderLayout());
        editorPane.add(tabbedPane, BorderLayout.CENTER);

        JPanel btnPan = new JPanel();
        btnPan.setLayout(new FlowLayout(FlowLayout.LEFT));
        
        JButton openBtn = new JButton("Open");
        openBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String file = promptValue("Filename to open");
                if ( file.charAt(1) != ':' ) {
                    file = cpmPath.charAt(0)+":"+file;
                }
                openFile(file);
            }
        });
        openBtn.setMnemonic( KeyEvent.VK_O ); // Alt + O
        btnPan.add(openBtn);

        JButton compileBtn = new JButton("Compile");
        compileBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                compileCpmTp3(getCurCpmPath(), true);
            }
        });
        btnPan.add(compileBtn);

        JButton compileDiskBtn = new JButton("Compile on Disk");
        compileDiskBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                compileCpmTp3(getCurCpmPath(), false);
            }
        });
        btnPan.add(compileDiskBtn);

        JButton runBtn = new JButton("Run"); // todo : compile on disk / then add autorun
        runBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                runCpmTp3(getCurCpmPath());
            }
        });
        btnPan.add(runBtn);

        editorPane.add(btnPan, BorderLayout.NORTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, editorPane, conScroller) {
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

    }

}