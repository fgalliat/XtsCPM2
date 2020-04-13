package j80;

/**
 * $Id: VDU.java 330 2010-09-14 10:29:28Z mviara $
 * 
 * Video Display Unit peripheral, this class is the locial terminal of
 * the J80 system.
 *
 * $Log: VDU.java,v $
 * Revision 1.3  2008/05/14 16:53:38  mviara
 * Added support to terminate the simulation pressing the key F10.
 *
 * Revision 1.2  2004/06/20 16:27:29  mviara
 * Some minor change.
 *
 */
public interface VDU extends Peripheral
{
	public void showIdle(boolean mode);

	public void showUtilization(int cpu);

	public void println(String s);
	
	public void print(String s);
	/**
	 * Set the phisical devices
	 */
	public void setCRT(CRT crt);

	/**
	 * Set the number of requested row
	 */
	public void setNumRow(int row);

	/**
	 * Set the number of requested col
	 */
	public void setNumCol(int col);

	/**
	 * Return the number of screew col
	 */
	public int getNumCol();

	public boolean isTerminate();
}
