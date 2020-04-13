package j80.vdu;

import java.util.HashMap;
import java.util.Vector;
import java.awt.event.KeyEvent;

/**
 * $Id: AbstractCRT.java 330 2010-09-14 10:29:28Z mviara $
 * 
 * Sample implementation of j80.CRT<p>
 *
 * This version put the CRT copy in one array and assume that one
 * thread update the phisical device at regular interval. The constants
 *  REFRESH_TIME is the default update interval. The function key
 *  definition use the standard defined in the filed of the class
 *  java.awt.event.KeyEvent
 *
 * @author $Id: AbstractCRT.java 330 2010-09-14 10:29:28Z mviara $
 * @version $Id: AbstractCRT.java 330 2010-09-14 10:29:28Z mviara $
 *
 * $Log: AbstractCRT.java,v $
 * Revision 1.6  2008/05/15 17:07:17  mviara
 * Added preliminary support for Z80Pack. Fixed bug in idle loop detection.
 *
 * Revision 1.5  2008/05/14 16:53:38  mviara
 * Added support to terminate the simulation pressing the key F10.
 *
 * Revision 1.4  2007/06/21 10:23:32  mviara
 * Added support for variable number of lines in CRT.
 *
 * Revision 1.3  2004/06/20 16:24:33  mviara
 * Some minor change.
 *
 * Revision 1.2  2004/06/16 15:24:20  mviara
 * First CVS Revision.
 *
 */
abstract public class AbstractCRT implements j80.CRT
{
	public final byte SPACE = (byte)' ';
	protected boolean statusLine = false;
	private boolean terminate = false;

	/**
	 * Default refresh time in ms
	 */
	protected final int REFRESH_TIME = 20;

	/**
	 * Hash map where the key is the function key and the value is the
	 * ascii string associated with the function.
	 *
	 */
	private HashMap keysMap = new HashMap();

	/**
	 * Ring buffer with the readed key
	 */
	private Vector	keys = new Vector();

	/**
	 * Current attribute
	 */
	protected byte att = NORMAL;

	/**
	 * Current cursor location
	 */
	protected int row = 0 ,col = 0;

	/**
	 * Array with one flag for every for, if the row N is changed
	 * changed[N] must be set to true.
	 */
	protected boolean changed[];

	/**
	 * Flag true when any value in changed[] is set to true.
	 */
	protected boolean screenChanged = false;

	/**
	 * Screen size
	 */
	protected int NROW,NCOL;
	protected int FONTSIZE = 0;

	/**
	 * Array with screen image
	 */
	protected byte videoChar[];

	/**
	 * Array with screen attribute image
	 */
	protected byte videoAtt[];

	/**
	 * Flag set true when the cursor is enabled
	 */
	protected boolean cursor = true;

	public void setCursor(boolean mode)
	{
		this.cursor = mode;
	}

	public boolean getCursor()
	{
		return cursor;
	}

	public void addKey(String s)
	{
		for (int i = 0 ; i < s.length() ; i++)
			addKey(s.charAt(i));
	}

	/**
	 * Add a new key to the console buffer
	 *
	 * @param key - Ascii code of the key 0-255
	 */
	public void addKey(int key)
	{
		if (key > 255)
			key = 27;
		
		synchronized (keys)
		{
			if (key >=0 && key < 256)
				keys.add(new Integer(key));
		}
	}


	public boolean consoleStatus()
	{
		return keys.size() == 0 ? false : true;
	}

	public int consoleInput()
	{
		int result = 0;

		while (keys.size() == 0)
			;
		
		synchronized (keys)
		{
			Integer ii = (Integer)keys.elementAt(0);
			result = ii.intValue();
			keys.remove(0);
		}

		return result;
	}

	public String getDefinedKey(int key)
	{
		return (String)keysMap.get(new Integer(key));
	}
	
	public void defineKey(int code,int c)
	{
		defineKey(code,(char)c);
	}

	public void defineKey(int code,char c)
	{
		char a[] = new char[1];
		a[0] = c;
		defineKey(code,new String(a));
	}

	public void defineKey(int code,String s)
	{
		keysMap.put(new Integer(code),s);
	}


	public void setChar(int pos,char c)
	{
		videoChar[pos] = (byte)c;
		videoAtt[pos] = att;

		markLine(pos/NCOL);
	}

	public void scrollDown(int from,int size,int n)
	{
		int i;

		//System.arraycopy(videoChar,from+n,videoChar,from,size - n);
		//System.arraycopy(videoAtt,from+n,videoAtt,from,size-n);

		for (i = 0 ; i < size - n ; i++)
		{
			videoChar[from+size-i-1] = videoChar[from+size-i-1-n];
			videoAtt[from+size-i-1]  = videoAtt[from+size-i-1-n];
		}

		for (i = 0 ; i < n ; i++)
		{
			videoChar[from+i] = SPACE;
			videoAtt[from+i] = att;
		}

		markLine();
	}


	public void scrollUp(int from,int size,int n)
	{
		int i;

		//System.arraycopy(videoChar,from+n,videoChar,from,size - n);
		//System.arraycopy(videoAtt,from+n,videoAtt,from,size-n);
		
		for (i = 0 ; i < size - n ; i++)
		{
			videoChar[from+i] = videoChar[from+i+n];
			videoAtt[from+i] = videoAtt[from+i+n];
		}

		for (i = 0 ; i < n ; i++)
		{
			videoChar[from+size-1-i] = SPACE;
			videoAtt[from+size-1-i] = att;
		}

		markLine();
	}



	public void setAtt(byte att)
	{
		this.att = att;
	}

	public int getAtt()
	{
		return att;
	}

	public int getNumRow()
	{
		return NROW;
	}

	public int getNumCol()
	{
		return NCOL;
	}

	public void setFontSize(int fontSize)
	{
		this.FONTSIZE = fontSize;
	}
	
	public void setNumRow(int row)
	{
		this.NROW = row;
	}

	public void setNumCol(int col)
	{
		this.NCOL = col;
	}

	public int setNumRow()
	{
		return NROW;
	}

	public int getScreenSize()
	{
		return NROW * NCOL;
	}

	public void setRow(int r)
	{
		setCursor(r,col);
	}

	public void setCol(int c)
	{
		setCursor(row,c);
	}

	public int getRow()
	{
		return row;
	}

	public int getCol()
	{
		return col;
	}
	
	protected void markLine(int start)
	{
		markLine(start,1);
	}

	protected void markLine(int start,int n)
	{
		synchronized (changed)
		{
			for (int i = 0 ; i < n ; i++)
				changed[start+i] = true;
			screenChanged = true;
		}
	}

	protected void markLine()
	{
		markLine(0,NROW);
	}

	public void reset()
	{
	}
	
	public void init()
	{
		videoChar	= new byte[NCOL * (NROW+1)];
		videoAtt	= new byte[NCOL * (NROW+1)];
		changed		= new boolean[NROW+1];
		
		/*
		 * write empty status line
		 */
		for (int i = 0 ; i < NCOL ;i ++)
		{
			videoChar[NROW*NCOL+i] = (byte)SPACE;
			videoAtt[NROW*NCOL+i]  = REVERSE;
			markLine(NROW);
		}
		
		markLine();
	}
	

	public void setCursor(int r,int c)
	{
		int oldRow = row;
		row = r;
		col = c;
		markLine(oldRow);
		markLine(row);
	}


	public void printStatus(int pos,String s)
	{
		pos = NROW * NCOL + pos;

		for (int i = 0 ; i < s.length() ; i++)
		{
			if (pos < videoChar.length)
			{
				videoChar[pos] = (byte)s.charAt(i);
			}
			pos++;
		}
		markLine(NROW);
	}

	public void terminate()
	{
		terminate = true;
	}

	public boolean isTerminate()
	{
		return terminate;
	}
}
