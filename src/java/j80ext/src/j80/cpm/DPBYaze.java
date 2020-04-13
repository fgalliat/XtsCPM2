package j80.cpm;

import java.io.*;

/**
 * $Id: DPBYaze.java 330 2010-09-14 10:29:28Z mviara $
 * 
 * DPB user for YAZE CP/M Disks
 *
 * $Log: DPBYaze.java,v $
 * Revision 1.2  2004/06/20 16:26:45  mviara
 * CVS ----------------------------------------------------------------------
 * Some minor change.
 *
 */
public class DPBYaze extends DPBBuffer
{
	DPBYaze()
	{
	}

	public void setBuffer(String name) throws Exception
	{
		InputStream is = new FileInputStream(name);
		byte buffer[] = new byte[128];
		is.read(buffer);
		setBuffer(buffer,32);
		is.close();
			
	}

	public String toString()
	{
		return "Yaze Cp/m Disk";
	}
}	
