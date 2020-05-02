package com.xtase.jni80;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

import java.awt.BorderLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class XtsJ80Joypad implements XtsJ80Device {

    public XtsJ80Joypad(XtsJ80System system) {

    }

    @Override
    public void reset() {
    }

    JButton upBtn = new JButton("^");
    JButton downBtn = new JButton("v");
    JButton leftBtn = new JButton("<");
    JButton rightBtn = new JButton(">");
    JButton b2Btn = new JButton("2");
    JButton b1Btn = new JButton("1");
    JButton menuBtn = new JButton("m");

    boolean upFlag = false;
    boolean downFlag = false;
    boolean leftFlag = false;
    boolean rightFlag = false;
    boolean b2Flag = false;
    boolean b1Flag = false;
    boolean menuFlag = false;

    @Override
    public void setup() {
        BtnListener btnListener = new BtnListener();

        leftPane = new JPanel();

        JPanel stickPane = new JPanel();
        stickPane.setLayout(new BorderLayout());

        stickPane.add(upBtn, BorderLayout.NORTH);
        stickPane.add(downBtn, BorderLayout.SOUTH);
        stickPane.add(leftBtn, BorderLayout.WEST);
        stickPane.add(rightBtn, BorderLayout.EAST);

        leftPane.setLayout(new BorderLayout());
        leftPane.add(stickPane, BorderLayout.CENTER);

        rightPane = new JPanel();

        JPanel btnPane = new JPanel();
        btnPane.setLayout(new BorderLayout());

        menuBtn.addMouseListener(btnListener);
        b1Btn.addMouseListener(btnListener);
        b2Btn.addMouseListener(btnListener);
        upBtn.addMouseListener(btnListener);
        downBtn.addMouseListener(btnListener);
        leftBtn.addMouseListener(btnListener);
        rightBtn.addMouseListener(btnListener);

        btnPane.add(menuBtn, BorderLayout.NORTH);
        btnPane.add(b2Btn, BorderLayout.CENTER);
        btnPane.add(b1Btn, BorderLayout.WEST);

        rightPane.setLayout(new BorderLayout());
        rightPane.add(btnPane, BorderLayout.CENTER);
    }

    protected class BtnListener implements MouseListener {

        @Override
        public void mouseClicked(MouseEvent e) {
        }

        @Override
        public void mouseEntered(MouseEvent e) {
        }

        @Override
        public void mouseExited(MouseEvent e) {
            if (e.getSource() == upBtn) {
                upFlag = false;
            }
            if (e.getSource() == downBtn) {
                downFlag = false;
            }
            if (e.getSource() == leftBtn) {
                leftFlag = false;
            }
            if (e.getSource() == rightBtn) {
                rightFlag = false;
            }

            if (e.getSource() == b1Btn) {
                b1Flag = false;
            }
            if (e.getSource() == b2Btn) {
                b2Flag = false;
            }
            if (e.getSource() == menuBtn) {
                menuFlag = false;
            }
        }

        @Override
        public void mousePressed(MouseEvent e) {
            if (e.getSource() == upBtn) {
                upFlag = true;
            }
            if (e.getSource() == downBtn) {
                downFlag = true;
            }
            if (e.getSource() == leftBtn) {
                leftFlag = true;
            }
            if (e.getSource() == rightBtn) {
                rightFlag = true;
            }

            if (e.getSource() == b1Btn) {
                b1Flag = true;
            }
            if (e.getSource() == b2Btn) {
                b2Flag = true;
            }
            if (e.getSource() == menuBtn) {
                menuFlag = true;
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (e.getSource() == upBtn) {
                upFlag = false;
            }
            if (e.getSource() == downBtn) {
                downFlag = false;
            }
            if (e.getSource() == leftBtn) {
                leftFlag = false;
            }
            if (e.getSource() == rightBtn) {
                rightFlag = false;
            }

            if (e.getSource() == b1Btn) {
                b1Flag = false;
            }
            if (e.getSource() == b2Btn) {
                b2Flag = false;
            }
            if (e.getSource() == menuBtn) {
                menuFlag = false;
            }
        }

    }

    JPanel leftPane;
    JPanel rightPane;

    public JComponent getLeftPanel() {
        return leftPane;
    }

    public JComponent getRightPanel() {
        return rightPane;
    }

    public boolean isDirUp() {
        return upFlag;
    }

    public boolean isDirDown() {
        return downFlag;
    }

    public boolean isDirLeft() {
        return leftFlag;
    }

    public boolean isDirRight() {
        return rightFlag;
    }

    public boolean isBtnB1() {
        return b1Flag;
    }

    public boolean isBtnB2() {
        return b2Flag;
    }

    public boolean isBtnMenu() {
        return menuFlag;
    }

}