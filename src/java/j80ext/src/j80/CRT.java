package j80;

/**
 * $Id: CRT.java 330 2010-09-14 10:29:28Z mviara $
 * 
 * Phisical CRT interface<p>
 *
 * Rappresent a phisical devices suitable for one terminale emulation.
 *
 * $Log: CRT.java,v $
 * Revision 1.5  2008/05/14 16:53:38  mviara
 * Added support to terminate the simulation pressing the key F10.
 *
 * Revision 1.4  2007/06/21 10:23:32  mviara
 * Added support for variable number of lines in CRT.
 *
 * Revision 1.3  2004/11/22 16:50:34  mviara
 * Some cosmetic change.
 *
 * Revision 1.2  2004/06/20 16:27:29  mviara
 * Some minor change.
 *
 *
 */
public interface CRT
{
	/**
	 * Normale attribute
	 */
	static public final byte NORMAL		= 0;

	/**
	 * Reverse attribute
	 */
	static public final byte REVERSE	= 1;

	/**
	 * Hi light attribute
	 */
	static public final byte HI			= 2;

	/**
	 * Underline attribute
	 */
	static public final byte UNDERLINE  = 4;

	/**
	 * Blinking attribute
	 */
	static public final byte BLINKING	= 8;
	
	public void scrollDown(int from,int size,int n);
	public void scrollUp(int from,int size,int n);
	public void setChar(int pos,char c);
	public void setAtt(byte a);
	public void setCursor(boolean mode);
	public boolean getCursor();
	public void setCursor(int r,int c);
	public void setRow(int r);
	public void setCol(int c);
	public int getRow();
	public int getCol();
	public int getNumCol();
	public int getNumRow();
	public int getScreenSize();
	public void setNumRow(int row);
	public void setNumCol(int col);
	public void setFontSize(int size);
	public void init();
	public void reset();
	public boolean consoleStatus();
	public int consoleInput();
	public void printStatus(int pos,String status);
	
	/**
	 * Add new key to the console buffer
	 */
	public void addKey(int key);

	/**
	 * Add one string to the console buffer
	 */
	public void addKey(String s);

	/**
	 * Define a new function key
	 * 
	 * @param key - Function key @see java.awt.KeyEvent
	 * @param S - String with the String definition of the key
	 */
	public void defineKey(int key,String s);
	
	/**
	 * Define a function key
	 *
	 * @param key - Function key @see java.awt.KeyEvent
	 * @param code - Ascii code of the key
	 */
	public void defineKey(int key,int code);
	
	/**
	 * Define a function key
	 *
	 * @param key - Function key @see java.awt.KeyEvent
	 * @param code - Ascii code of the key
	 */
	public void defineKey(int key,char code);


	/**
	 * Return function key definition
	 *
	 * @param key - Function key
	 *
	 * @return The string associated of null
	 */
	public String getDefinedKey(int key);

	public void terminate();
	public boolean isTerminate();
	
}