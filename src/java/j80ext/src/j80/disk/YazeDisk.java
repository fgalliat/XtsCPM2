package j80.disk;

import java.io.*;

/**
 * $Id: YazeDisk.java 331 2010-09-14 10:30:13Z mviara $
 * 
 * Disk driver compatible wuth YAZE CP/M disk
 *
 * $Log: YazeDisk.java,v $
 * Revision 1.3  2004/11/22 16:50:34  mviara
 * Some cosmetic change.
 *
 * Revision 1.2  2004/06/20 16:27:04  mviara
 * Some minor change.
 *
 */
public class YazeDisk extends ImageDisk
{
	byte header[] = new byte[128];
	private j80.cpm.DPB dpb = null;
	
	public YazeDisk(String name,j80.cpm.DPB dpb)
	{
		super(name);
		this.dpb = dpb;
	}

	protected int getOffset(int track,int sector)
	{
		int offset = super.getOffset(track,sector) + SECSIZE;
		//System.out.println("Read "+track+" "+sector +" = "+offset);
		return offset;
	}

	public int getSectorTrack()
	{
		return dpb.sectorTrack;
	}

	public String toString()
	{
		return "YAZE "+super.toString();
	}
}