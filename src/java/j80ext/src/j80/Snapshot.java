package j80;

/**
 * $Id: Snapshot.java 330 2010-09-14 10:29:28Z mviara $
 * 
 * Snapshot interface for memory shapshot in the emulator.
 *
 * $Log: Snapshot.java,v $
 * Revision 1.2  2004/06/20 16:27:29  mviara
 * Some minor change.
 *
 */
public interface Snapshot extends Peripheral
{
	public void loadSnapshot(J80 cpu,String filename) throws Exception;
}

