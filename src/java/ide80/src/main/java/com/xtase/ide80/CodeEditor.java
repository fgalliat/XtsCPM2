package com.xtase.ide80;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import javax.swing.JScrollPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.xtase.ide80.components.PascalTextPane;

public class CodeEditor extends JScrollPane {

    protected Main editorFrame = null;

    protected PascalTextPane editor = null;
    protected File fileToEdit = null;

    protected String cpmPath = null;

    boolean modified = false;

    public CodeEditor(Main editorFrame) {
        super(new PascalTextPane());
        editor = (PascalTextPane) getViewport().getComponent(0);
        this.editorFrame = editorFrame;

        editor.getDocument().addDocumentListener( new DocumentListener(){
        
            @Override
            public void removeUpdate(DocumentEvent e) {
                modified = true;
                editorFrame.setTitle("IDE80 ("+ cpmPath +") *");
            }
        
            @Override
            public void insertUpdate(DocumentEvent e) {
                modified = true;
                editorFrame.setTitle("IDE80 ("+ cpmPath +") *");
            }
        
            @Override
            public void changedUpdate(DocumentEvent e) {
                modified = true;
                editorFrame.setTitle("IDE80 ("+ cpmPath +") *");
            }
        } );
    }

    public PascalTextPane getEditor() {
        return editor;
    }

    public boolean isChanged() {
        return modified;
    }

    // TODO : better
    protected void setChanged(boolean changed) {
        modified = changed;
    }

    public String getCpmPath() {
        return cpmPath;
    }

    public void load(String cpmPath) {
        cpmPath = cpmPath.toUpperCase();
        this.cpmPath = cpmPath;
        try {
            File file = (File) editorFrame.invokeMethodOnClass("com.xtase.jni80.XtsJ80FileSystem", "resolveCPMPath",
                    cpmPath);
            editorFrame.status("CPM File : " + file.getPath());
            fileToEdit = file;
        } catch (Exception ex) {
            editorFrame.status("Could not get CPM path [" + ex.toString() + "]");
        }

        try {
            String text = "";

            String path = null;
            if (fileToEdit != null && fileToEdit.exists()) {
                // path = "./C/0/JUKE.PAS";
                path = fileToEdit.getPath();
            } else {
                // path = "../jni80/distro/C/0/JUKE.PAS";
                editorFrame.status("Could not open file");
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

            // // print an extract
            // System.out.println(text.substring(0, 250));

            editor.setText(text);
        } catch (Exception exp) {
            exp.printStackTrace();
            editor.setText("File failed to open");
        }
    }

}