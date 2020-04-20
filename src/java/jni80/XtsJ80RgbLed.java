/**
 * 
 * RGB Led device
 * 
 * Xtase-fgalliat @Apr2020
 */

import javax.swing.*;
import java.awt.*;

public class XtsJ80RgbLed extends JLabel implements XtsJ80Device {

    @Override
    public void reset() {
        off();
    }

    @Override
    public void setup() {
        off();
    }

    public XtsJ80RgbLed(XtsJ80System system) {
        super("#");
        setOpaque(true);
        setPreferredSize(new Dimension(32, 32));
        setBackground(Color.BLACK);
    }

    public void off() {
        setBackground(Color.BLACK);
    }

    public void rgb(int r, int g, int b) {
        setBackground(new Color(r, g, b));
        // need repaint ?
    }

}