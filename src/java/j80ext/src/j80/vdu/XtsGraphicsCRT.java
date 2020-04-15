package j80.vdu;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;


/**
 * $Id: XtsGraphicsCRT.java 2020-04-15 08:30:00Z fgalliat(Xtase) $
 * 
 * Swing j80.CRT + YATxx Board implementation<p>
 *
 * $Log: XtsGraphicsCRT.java,v $
 * Revision 1.1  2020/04/15 08:30:00  fgalliat
 * Start re-write.
 *
 */
public class XtsGraphicsCRT extends AbstractCRT implements KeyListener,ActionListener
{
	private Color foreGround[] = new Color[3];
	private Color backGround[] = new Color[3];
	private Image image = null;
	private FontMetrics fm;
	private Dimension size,sizeChar;
	private JCRT jcrt;
	private char line[];
	
	JLabel ledComp = new JLabel();

	public class RGBLed {
		public void rgb(int r, int g, int b) {
			ledComp.setBackground(new Color(r,g,b));
		}
	}

	RGBLed led = new RGBLed();

	public RGBLed getLed() {
		return led;
	}
	
	class JCRT extends JComponent
	{
		JCRT()
		{
			setFont(new Font("Monospaced",Font.PLAIN,11));
			//setOpaque(true);
			enableEvents(AWTEvent.KEY_EVENT_MASK);
			enableEvents(AWTEvent.KEY_EVENT_MASK | AWTEvent.INPUT_METHOD_EVENT_MASK);
			addKeyListener(XtsGraphicsCRT.this);

		}
		
		public Dimension getPreferredSize()
		{
			if (size == null)
			{
				Graphics g = getGraphics();
				g.setFont(getFont());
				fm = g.getFontMetrics();

				int width = 0;
				int height = fm.getHeight();
				// for (int i = 0 ; i < 256 ; i++)
				// 	if (fm.charWidth(i) > width)
				// 		width = fm.charWidth(i);

				// the largest letter in monospaced
				// cause UTF codes may generate 2xsize width
				width = fm.charWidth('M');

				sizeChar = new Dimension(width,height);

				size = new Dimension(NCOL*width,(NROW+(statusLine ? 1 : 0))*height);
			}

			return size;
		}


		public Dimension getMinimumSize()
		{
			return getPreferredSize();
		}

		public Dimension getMaximumSize()
		{
			return getPreferredSize();
		}


		public void paint(Graphics g)
		{
			if (image == null)
			{
				image = createImage(size.width, size.height);
				markLine();

			}
			synchronized (changed)
			{
				draw(image.getGraphics());
			}

			g.drawImage(image, 0, 0, this);

		}


		private void draw(Graphics g)
		{
			Insets insets = getInsets();
			g.translate(insets.left,insets.top);
			g.setFont(getFont());

			for (int r = 0 ; r < NROW ; r++)
			{
				if (changed[r])
				{
				//System.out.println("Paint line "+r);
					changed[r] = false;
					drawLine(g,r);
				}
			}

			if (statusLine && changed[NROW])
			{
				changed[NROW] = false;
				drawLine(g,NROW);
			}
		}
		
		public boolean hasFocus()
		{
			return true;
		}

		public boolean isFocusable()
		{
			return true;
		}

	}
	
	public XtsGraphicsCRT()
	{
		NROW = 25;
		NCOL = 80;
		statusLine = true;

		jcrt = new JCRT();
		/*defineColor(NORMAL,	new Color(255,255,255),new Color(0,0,128));
		defineColor(REVERSE,new Color(0,0,128),new Color(0,128,0));
		defineColor(HI,		new Color(255,255,0),new Color(0,0,128));*/

		defineColor(NORMAL,	new Color(80,160,0),new Color(0,0,0));
		defineColor(REVERSE,new Color(0,0,0),new Color(0,160,0));
		defineColor(HI,		new Color(0,255,0),new Color(0,0,0));

		defineKey(KeyEvent.VK_ENTER,(char)13);

		defineKey(KeyEvent.VK_ESCAPE,(char)27);
	}




	public void drawLine(Graphics g,int att,int r,int c,int count)
	{
		int x = c * sizeChar.width ;
		int y = sizeChar.height - fm.getDescent() + r * sizeChar.height -1;

		for (int i = 0 ; i < count ; i++)
			line[i] =(char)( videoChar[r*NCOL+c+i] & 0xff);
		
		g.setColor(getBackground(att));
		g.fillRect(x,(sizeChar.height * r),(count) *sizeChar.width,sizeChar.height);
		g.setColor(getForeground(att));

		g.drawChars(line,0 , count, x , y);

	}

	public void drawLine(Graphics g,int r)
	{
		int pos = r * NCOL;

		/*
		g.setColor(getBackground());
		g.fillRect(0,(sizeChar.height * r)+1,size.width,sizeChar.height);
		g.setColor(getForeground());
		*/
		int att = videoAtt[pos];
		int count = 0;
		int c = 0;

		while (c < NCOL)
		{
			if (videoAtt[pos] == att)
				count++;
			else
			{
				drawLine(g,att,r,c - count ,count);
				count = 1;
				att = videoAtt[pos];
			}
			c++;
			pos++;
		}

		drawLine(g,att,r,c - count ,count);

		
		if (r == row && getCursor() == true)
		{
			//System.out.print("LOC "+row+" "+col);
			g.fillRect(sizeChar.width * col,sizeChar.height * (row+1)-3,sizeChar.width,2);
		}

	}

	public Color getForeground(int att)
	{
		return foreGround[att];
	}

	public Color getBackground(int att)
	{
		return backGround[att];
	}

	
	public void defineColor(int att,Color f,Color b)
	{
		foreGround[att] = f;
		backGround[att] = b;
	}

		
	public void init()
	{
		super.init();
		
		if (FONTSIZE != 0)
			jcrt.setFont(new Font("Monospaced",Font.PLAIN,FONTSIZE));

		line = new char[NCOL];
		
		JFrame f = new JFrame("Xtase mod#"+j80.J80.version);

		// ------ Xts RGB LED ------------
		// JLabel led = new JLabel();
		ledComp.setOpaque(true);
		ledComp.setSize(32,32);
		ledComp.setText("M");
		ledComp.setBackground(Color.BLACK);
		
		JPanel ledPanel = new JPanel();
		ledPanel.add(ledComp);
		// --------------------------------

		JPanel p = new JPanel();
		p.add(jcrt);
		// default is HFlowLayout
		p.add(ledPanel);

		f.setContentPane(p);
		f.pack();
		f.setVisible(true);

		jcrt.requestFocus();
		
		javax.swing.Timer timer = new javax.swing.Timer(REFRESH_TIME,this);

		timer.setRepeats(true);
		timer.start();


	}


	public void keyPressed(KeyEvent e)
	{
		String s = getDefinedKey(e.getKeyCode());
		if (s != null)
		{
			addKey(s);
			e.consume();
		}
		else
		{
			if (e.getKeyCode() == KeyEvent.VK_F10)
				terminate();

			if (e.getKeyChar() != e.CHAR_UNDEFINED)
			{
				addKey(e.getKeyChar());
				e.consume();
			}
		}

	}

	public void keyReleased(KeyEvent e)
	{
	}


	public void keyTyped(KeyEvent e)
	{
	}

	public boolean isFocusTraversable() 
	{
		return true;
	}


	public void actionPerformed(ActionEvent e)
	{
		if (screenChanged)
		{
		
			synchronized (changed)
			{
				screenChanged = false;
				jcrt.repaint();

			}
		}
	}

}
