package com.xtase.jni80;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

import java.awt.BorderLayout;

public class XtsJ80Joypad implements XtsJ80Device {

    public XtsJ80Joypad(XtsJ80System system) {

    }

    @Override
    public void reset() {
    }

    @Override
    public void setup() {
        leftPane = new JPanel();

        JPanel stickPane = new JPanel();
        stickPane.setLayout(new BorderLayout());

        JButton upBtn = new JButton("^");
        JButton downBtn = new JButton("v");
        JButton leftBtn = new JButton("<");
        JButton rightBtn = new JButton(">");

        stickPane.add(upBtn, BorderLayout.NORTH);
        stickPane.add(downBtn, BorderLayout.SOUTH);
        stickPane.add(leftBtn, BorderLayout.WEST);
        stickPane.add(rightBtn, BorderLayout.EAST);

        leftPane.setLayout(new BorderLayout());
        leftPane.add(stickPane, BorderLayout.CENTER);

        rightPane = new JPanel();

        JPanel btnPane = new JPanel();
        btnPane.setLayout(new BorderLayout());

        JButton b2Btn = new JButton("2");
        JButton b1Btn = new JButton("1");
        JButton menuBtn = new JButton("m");

        btnPane.add(menuBtn, BorderLayout.NORTH);
        btnPane.add(b2Btn, BorderLayout.CENTER);
        btnPane.add(b1Btn, BorderLayout.WEST);

        rightPane.setLayout(new BorderLayout());
        rightPane.add(btnPane, BorderLayout.CENTER);
    }

    JPanel leftPane;
    JPanel rightPane;

    public JComponent getLeftPanel() {
        return leftPane;
    }

    public JComponent getRightPanel() {
        return rightPane;
    }

}