package j80;

/**
 * $Id: OutPort.java 330 2010-09-14 10:29:28Z mviara $
 * 
 * Interface to trap one Output port.
 *
 * $Log: OutPort.java,v $
 * Revision 1.2  2004/06/20 16:27:29  mviara
 * Some minor change.
 *
 */
public interface OutPort
{
	public void outb(int port,int value,int states) throws Exception;
}
