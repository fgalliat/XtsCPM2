package com.xtase.ide80;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.DefaultCaret;

import com.xtase.jni80.JavaPascalCompiler;
import com.xtase.jni80.JavaRunCPM_GFX;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.awt.event.MouseListener;

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

    protected File getCpmFile(String cpmPath) {
        try {
            File file = (File) invokeMethodOnClass("com.xtase.jni80.XtsJ80FileSystem", "resolveCPMPath", cpmPath);
            return file;
        } catch (Exception ex) {
            status(ex.toString());
            return null;
        }
    }

    public static void main(String[] args) {
        String path = "c:bmp.pas";
        if (args != null && args.length > 0) {
            path = args[0];
        }
        new Main(path);
    }

    protected void updateTitle() {
        String cpmPath = getCurCpmPath();
        Main.this.setTitle("IDE80 (" + cpmPath + ")" + (getCurrentEditor().isChanged() ? " *" : ""));

        if (cpmPath.endsWith(".PAS")) {
            File pasFile = getCpmFile(cpmPath);
            if (pasFile != null && pasFile.exists()) {
                compileBtn.setEnabled(true);
                compileDiskBtn.setEnabled(true);
            } else {
                compileBtn.setEnabled(false);
                compileDiskBtn.setEnabled(false);
            }

            File comFile = getCpmFile(cpmPath.replaceAll(Pattern.quote(".PAS"), Matcher.quoteReplacement(".COM")));
            if (comFile != null && comFile.exists()) {
                runBtn.setEnabled(true);
            } else {
                runBtn.setEnabled(false);
            }
        } else {
            compileBtn.setEnabled(false);
            compileDiskBtn.setEnabled(false);
            runBtn.setEnabled(false);
        }

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
        final String _cpmPath = cpmPath.replaceAll(Pattern.quote(".PAS"), Matcher.quoteReplacement(""));

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

    protected void halt() {
        this.setVisible(false);
        System.exit(0);
    }

    // =========================================================
    // =========================================================

    public String getCurCpmPath() {
        return getCurrentEditor().getCpmPath();
    }

    public String getCurCpmDrive() {
        return getCurCpmPath().charAt(0) + ":";
    }

    public JComponent makeAnEditor(String cpmPath) {
        cpmPath = cpmPath.toUpperCase();

        CodeEditor editor = new CodeEditor(this);
        editor.load(cpmPath);

        return editor;
    }

    JTabbedPane tabbedPane = null;

    public CodeEditor getCurrentEditor() {
        CodeEditor editor = (CodeEditor) tabbedPane.getSelectedComponent();
        return editor;
    }

    public void closeCurrentEditor() {
        if (getCurrentEditor().isChanged()) {
            if (!confirm("Current editor is modified\nExit anyway ?")) {
                return;
            }
        }
        if (tabbedPane.getComponentCount() <= 1) {
            halt();
        }
        tabbedPane.removeTabAt(tabbedPane.getSelectedIndex());
    }

    public void openFile(String cpmPath) {
        cpmPath = cpmPath.toUpperCase();
        tabbedPane.addTab(cpmPath.substring(2), null, makeAnEditor(cpmPath), cpmPath);
        tabbedPane.setSelectedIndex(tabbedPane.getTabCount() - 1);
    }

    public String openDisk() {
        String curDrive = getCurCpmDrive();

        final JDialog d = new JDialog(Main.this, "Disk " + curDrive, true);
        d.setLayout(new BorderLayout());
        JButton b = new JButton("OK");
        b.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                d.setVisible(false);
            }
        });

        File disk = getCpmFile(curDrive);
        if (disk == null || !disk.exists()) {
            status("Drive not found !");
            return null;
        }

        File[] content = disk.listFiles();
        DefaultListModel model = new DefaultListModel();
        for (File f : content) {
            if (f.isFile() 
                 && !f.getName().endsWith(".COM") 
                 && !f.getName().endsWith(".class")
                 && !f.getName().endsWith("~")) {
                model.addElement(f.getName());
            }
        }
        JList list = new JList(model);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setLayoutOrientation(JList.VERTICAL);
        list.setVisibleRowCount(8);
        list.setSelectedIndex(0);
        list.addMouseListener(new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() >= 2) {
                    d.setVisible(false);
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }

            @Override
            public void mousePressed(MouseEvent e) {
            }

            @Override
            public void mouseReleased(MouseEvent e) {
            }

        });

        d.add(new JLabel("Select a file"), BorderLayout.NORTH);
        d.add(new JScrollPane(list), BorderLayout.CENTER);
        d.add(b, BorderLayout.SOUTH);
        d.setSize(400, 600);
        d.setVisible(true);

        return (String) model.get(list.getSelectedIndex());
    }

    // ============
    protected String promptValue(String message) {
        String name = JOptionPane.showInputDialog(this, message);
        return name;
    }

    protected boolean confirm(String message) {
        int v = JOptionPane.showConfirmDialog(this, message);
        return v == JOptionPane.YES_OPTION;
    }
    // ============

    JButton runBtn = null;
    JButton compileBtn = null;
    JButton compileDiskBtn = null;

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
        runBtn = new JButton("Run");
        compileBtn = new JButton("Compile");
        compileDiskBtn = new JButton("Compile on Disk");
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

        // ==== test 1st CPM disk ====
        File disk = getCpmFile("A:");
        if (disk == null || !disk.exists()) {
            status("A: Drive not found !");
        }
        // ===========================
        JPanel btnPan = new JPanel();
        btnPan.setLayout(new FlowLayout(FlowLayout.LEFT));

        JButton openBtn = new JButton("Open");
        openBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // String file = promptValue("Filename to open");
                String file = openDisk();

                if (file == null) {
                    return;
                }

                if (file.charAt(1) != ':') {
                    file = getCurCpmDrive() + file;
                }
                openFile(file);
            }
        });
        openBtn.setMnemonic(KeyEvent.VK_O); // Alt + O
        btnPan.add(openBtn);

        JButton saveBtn = new JButton("Save");
        saveBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                getCurrentEditor().save();
            }
        });
        saveBtn.setMnemonic(KeyEvent.VK_S); // Alt + S
        btnPan.add(saveBtn);

        compileBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (getCurrentEditor().isChanged()) {
                    if (!confirm("Current editor is modified\nCompile anyway ?")) {
                        return;
                    }
                }
                compileCpmTp3(getCurCpmPath(), true);
            }
        });
        btnPan.add(compileBtn);

        compileDiskBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (getCurrentEditor().isChanged()) {
                    if (!confirm("Current editor is modified\nCompile anyway ?")) {
                        return;
                    }
                }
                compileCpmTp3(getCurCpmPath(), false);
            }
        });
        compileDiskBtn.setMnemonic(KeyEvent.VK_C); // Alt + C
        btnPan.add(compileDiskBtn);

        runBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (getCurrentEditor().isChanged()) {
                    if (!confirm("Current editor is modified\nRun anyway ?")) {
                        return;
                    }
                }
                runCpmTp3(getCurCpmPath());
            }
        });
        runBtn.setMnemonic(KeyEvent.VK_U); // Alt + U
        btnPan.add(runBtn);

        JButton closeBtn = new JButton("Close");
        closeBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                closeCurrentEditor();
            }
        });
        closeBtn.setMnemonic(KeyEvent.VK_X); // Alt + X
        btnPan.add(closeBtn);

        compileBtn.setEnabled(false);
        compileDiskBtn.setEnabled(false);
        runBtn.setEnabled(false);

        JScrollPane conScroller = new JScrollPane(console);

        tabbedPane = new JTabbedPane();
        tabbedPane.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                // CodeEditor editor = (CodeEditor) tabbedPane.getSelectedComponent();
                // String cpmPath = editor.getCpmPath();
                updateTitle();
            }
        });

        openFile(cpmPath);

        JPanel editorPane = new JPanel();
        editorPane.setLayout(new BorderLayout());
        editorPane.add(tabbedPane, BorderLayout.CENTER);
        // ===========================

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

        updateTitle();
    }

}