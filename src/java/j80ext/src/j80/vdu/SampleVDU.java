package j80.vdu;

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.JComponent;
import java.util.Vector;
import java.util.HashMap;

import j80.*;

/**
 * $Id: SampleVDU.java 330 2010-09-14 10:29:28Z mviara $
 * 
 * 
 * Sample implementation of VDU class implements only the basic
 * function for one DUMP terminal.<p>
 *
 * Port used  : <p>
 *
 * CONSTA - Input register 0 when no keyboard char is available<p>
 * CONDAT - Input register, return the first character in the keyboard
 * buffer, if no char is availbale wait for one<p>
 * CONDAT - Output register output the character on the terminal<p>
 * TERMINATE - Output register when any data is written ther emulation
 * will be terminate.
 *
 * $Log: SampleVDU.java,v $
 * Revision 1.8  2008/05/22 22:13:43  mviara
 *
 * Removed debug comment.
 *
 * Revision 1.7  2008/05/15 17:07:17  mviara
 * Added preliminary support for Z80Pack. Fixed bug in idle loop detection.
 *
 * Revision 1.6  2008/05/15 12:33:37  mviara
 *
 * Fixed bug in reset CRT.
 *
 * Revision 1.5  2008/05/14 16:53:38  mviara
 * Added support to terminate the simulation pressing the key F10.
 *
 * Revision 1.4  2004/06/20 16:24:33  mviara
 * Some minor change.
 *
 */
public class SampleVDU implements VDU,Peripheral,OutPort,InPort
{
	private PrintStream ps = null;
	
	/**
	 * Console status register
	 */
	static private final int CONSTA		= 0;

	/**
	 * Console input/output register
	 */
	static private final int CONDAT		= 1;

	/**
	 * Terminale emulation register
	 */
	static private final int TERMINATE	= 255;
	
	private long numOutput = 0L;
	private long idleCounter = 0;
	private boolean screenChanged = false;
	protected CRT crt;
	private int row,col;
	private boolean insertMode;
	private int savedRow,savedCol;
	protected J80 cpu;
	

	public SampleVDU()
	{
		savedCol = savedRow = 0;
		insertMode = false;
	}

	void setInsertMode(boolean insertMode)
	{
		this.insertMode = insertMode;
	}
	
	public void saveCursor()
	{
		savedCol = crt.getCol();
		savedRow = crt.getRow();
		
	}

	public void restoreCursor()
	{
		crt.setCursor(savedRow,savedCol);
	}
	
	public void capture(String name)
	{
		try
		{
			ps = new PrintStream(new FileOutputStream(name));
		}
		catch (Exception e)
		{
			ps = null;
		}
	}
	
	public void clearEol()
	{
		int pos = crt.getRow() * crt.getNumCol() + crt.getCol();

		for (int i = crt.getCol() ; i < crt.getNumCol() ;i++)
		{
			crt.setChar(pos,' ');
			pos++;
		}

	}

	public void insertChar()
	{
		int pos		= crt.getRow()*crt.getNumCol() + crt.getCol();
		crt.scrollDown(pos,crt.getNumRow()*crt.getNumCol() - pos,1);
	}

	public void insertChar(char c)
	{
		int pos		= crt.getRow()*crt.getNumCol() + crt.getCol();
		insertChar();
		crt.setChar(pos,c);
	}

	public void insertLine()
	{
		int row		= crt.getRow();
		int ncol	= crt.getNumCol();
		int nrow	= crt.getNumRow();
		
		crt.scrollDown(row*ncol,(nrow - row) * ncol,ncol);
	}

	public void deleteChar()
	{
		int pos		= crt.getRow()*crt.getNumCol() + crt.getCol();
		crt.scrollUp(pos,crt.getNumRow()*crt.getNumCol() - pos,1);
					 
	}
	
	public void deleteLine()
	{
		int row		= crt.getRow();
		int ncol	= crt.getNumCol();
		int nrow	= crt.getNumRow();

		crt.scrollUp(row*ncol,(nrow - row) * ncol , ncol);
	}

	/**
	 * clear the current line
	 */
	void clearLine()
	{
		int pos = crt.getRow();
		for (int c = 0 ; c <= crt.getNumCol () ; c++,pos++)
		{
			crt.setChar(pos,' ');
		}

	}
	
	/**
	 * Clear from the begin of the line to the cursor
	 */
	public void clearFromLine()
	{
		int pos = crt.getRow() * crt.getNumCol();
		for (int c = 0 ; c <= crt.getCol () ; c++,pos++)
		{
			crt.setChar(pos,' ');
		}
			
	}
	
				
	/**
	 * Clear the display from the begin to the cursor
	 */
	public void clearToCursor()
	{
		clearFromLine();
		for (int r = 0 ; r < crt.getRow() ; r++)
		{
			int pos = r * crt.getNumCol();
			for (int c = 0 ; c < crt.getNumCol() ; c++)
			{
				crt.setChar(pos,' ');
				pos++;
			}
		}
		
	}
	
	public void clearEos()
	{
		clearEol();
		for (int r = crt.getRow() + 1 ; r < crt.getNumRow() ; r++)
		{
			int pos = r * crt.getNumCol();
			for (int c = 0 ; c < crt.getNumCol() ; c++)
			{
				crt.setChar(pos,' ');
				pos++;
			}
		}
	}
	

	
	private int constatus()
	{
		boolean result = crt.consoleStatus();
		

		
		if (result == false)
		{
			long o = cpu.getNumOutput();
			
			if (o == numOutput)
			{
				if (++idleCounter > 1000000)
				{
					cpu.sleep();
					idleCounter = 0;
				}
				
			}
			else
			{
				numOutput = o;
				idleCounter = 0;
			}
			
		}
		else
			idleCounter = 0;

 		return result == true ? 0xff : 0;

	}
	

	public int inb(int port,int hi)
	{
		int result = 0xff;
		
		switch (port)
		{
			case	CONSTA:
					result = constatus();
					break;
					
			case	CONDAT:
					
					while (constatus() == 0)
					{
						try
						{
							cpu.sleep();
						}
						catch (Exception ex)
						{
						}
					}
					result = crt.consoleInput();
			
		}

		return result;
	}
	
	public void outb(int port,int value,int states)
	{
		switch (port)
		{
			case	CONDAT:
					putchar((char)value);
					break;
			case	TERMINATE:
					J80.terminate();
					break;
		}
	}

	public int getNumCol()
	{
		return crt.getNumCol();
	}
	
	public void setNumRow(int row)
	{
		this.row = row;
	}

	public void setNumCol(int col)
	{
		this.col = col;
	}
	
	public void setCRT(CRT crt)
	{
		this.crt = crt;
	}

	public void resetCPU(J80 cpu)
	{
	}

	public void disconnectCPU(J80 cpu)
	{
		if (ps != null)
		{
			ps.close();
			ps = null;
		}
		crt.terminate();
		crt.reset();
	}
	

	/**
	 * Connect periperal to the virtual Z80 cpu
	 */
	public void connectCPU(J80 cpu)
	{
		this.cpu = cpu;
		cpu.addOutPort(CONDAT,this);
		cpu.addInPort(CONSTA,this);
		cpu.addInPort(CONDAT,this);
		cpu.addOutPort(TERMINATE,this);

		crt.defineKey(KeyEvent.VK_UP,5);
		crt.defineKey(KeyEvent.VK_DOWN,24);
		crt.defineKey(KeyEvent.VK_LEFT,19);
		crt.defineKey(KeyEvent.VK_RIGHT,4);

		crt.setNumRow(row);
		crt.setNumCol(col);
		crt.init();
		crt.printStatus(0,j80.J80.version);
		cls();
		home();

	}
	

	public void cls()
	{
		for (int i = 0 ; i < crt.getScreenSize() ; i++)
		{
			crt.setChar(i,' ');
		}
		
	}

	public void up()
	{
		int r = crt.getRow(), c = crt.getCol();

		if (r > 0)
			crt.setCursor(r - 1,c);

	}

	public void down()
	{
		int r = crt.getRow() + 1, c = crt.getCol();

		if (r < crt.getNumRow() - 1)
			crt.setCursor(r ,c);

	}

	
	public void left()
	{
		int r = crt.getRow(), c = crt.getCol();
		if (r == 0 && c == 0)
			return;

		if (--c < 0)
		{
			r --;
			c = crt.getNumCol() - 1;
			
		}
		
		crt.setCursor(r,c);
	}

	public void right()
	{
		int r = crt.getRow(), c = crt.getCol();

		if ((r == crt.getNumRow() - 1) && (c == crt.getNumCol() - 1))
			return;
		
		if (++c >= crt.getNumCol())
		{
			c = 0;
			if (++r >= crt.getNumRow())
				r = crt.getNumRow()- 1;
		}
		
		crt.setCursor(r,c);
	}

	void setCursor(int r,int c)
	{
		crt.setCursor(r,c);
	}

	void setRow(int r)
	{
		crt.setCursor(r,crt.getCol());
	}

	void setCol(int c)
	{
		crt.setCursor(crt.getRow(),c);
	}
	
	public void home()
	{
		crt.setCursor(0,0);
	}
	

	public void print(String s)
	{
		for (int i = 0 ; i < s.length() ; i++)
			putchar(s.charAt(i));
	}

	public void println(String s)
	{
		print(s+"\r\n");
	}
	
	public void putchar(char c)
	{
		
		switch (c)
		{
			default:

					if (c < 32 || c > 127)
						c = 32;
					if (insertMode)
						insertChar(c);
					else
						crt.setChar(crt.getRow()*crt.getNumCol()+crt.getCol(),c);
					
					if (crt.getCol()+1 >= crt.getNumCol())
					{
						if (crt.getRow()+1 >= crt.getNumRow())
						{
							crt.scrollUp(0,crt.getScreenSize(),crt.getNumCol());
							crt.setCursor(crt.getRow(),0);
						}
						else
							crt.setCursor(crt.getRow()+1,0);
					}
					else
						crt.setCursor(crt.getRow(),crt.getCol()+1);
					break;
					
			case	9:	// tab
					{
						int nc = (crt.getCol() / 8) * 8 + 8;
						if (nc >= crt.getNumCol())
							nc = crt.getNumCol() -1;
						crt.setCursor(crt.getRow(),nc);
				
					}
					break;
				
			case	8:
			case	127:
					left();
					break;
							
			case	13:
					crt.setCursor(crt.getRow(),0);
					break;
					
			case	10:
					crt.setCursor(crt.getRow(),0);
					if (crt.getRow()+1 >= crt.getNumRow())
					{
						crt.scrollUp(0,crt.getScreenSize(),crt.getNumCol());
					}
					else
						crt.setCursor(crt.getRow()+1,crt.getCol());
					break;
			case	12:
					cls();
					home();
					break;
					
			case	7:
					break;
		}

		

	}


/*	public void println(String s)
	{
		print(s+"\n");
	}
	
	public void print(String s)
	{
		for (int i = 0 ; i < s.length() ; i++)
			putchar(s.charAt(i));
	}
	*/

	public void showIdle(boolean mode)
	{
		String s = mode ? "W" : " ";
		crt.printStatus(crt.getNumCol() - 1,s);
	}

	public void showUtilization(int perc)
	{
		String s = perc+"%";

		while (s.length() < 5)
			s = " "+s;

		crt.printStatus(crt.getNumCol() - 7,s);
	}

	public boolean isTerminate()
	{
		return crt.isTerminate();
	}
	
	public String toString()
	{
		return "VDU : "+crt.getNumCol()+"x"+crt.getNumRow();
	}
}
