package j80.mmu;

import j80.*;

/**
 * $Id: BankMMU.java 330 2010-09-14 10:29:28Z mviara $
 * 
 * Banked memory manager<p>
 *
 * Allocate 1024 KB of memory in 16 bank of 16 x 4KB the hardware
 * interface implements the following register : <p>
 *
 * PAGELOW - Log byte of page mumber.<p>
 * PAGEHI - Hi byte of page number.<p>
 * PAGEFRAME - Page frame in the current bank.<p>
 *
 * $Log: BankMMU.java,v $
 * Revision 1.3  2004/06/20 16:26:26  mviara
 * Some minor change.
 *
 */
public class BankMMU implements MMU , OutPort
{
	public static final int PAGESIZE	= 4096;
	public static final int NUMPAGE		= 16;
	public static final int NUMBANK		= 16;
	public static final int TOTALPAGE	= 256;
	public static final int PAGELOW		= 20;
	public static final int PAGEHI		= 21;
	public static final int PAGEFRAME	= 22;

	private int banks[][] = new int[16][16];
	private int memory[] = new int[PAGESIZE * TOTALPAGE];
	private int currentBank = 0;
	private int page = 0;

	public BankMMU()
	{
		for (int b = 0 ; b < NUMBANK ; b++)
			for (int p = 0 ; p < NUMPAGE ; p++)
				banks[b][p] = p;
	}

	public void setBank(int bank)
	{
		currentBank = bank;
	}

	public int getBank()
	{
		return currentBank;
	}

	public void mapBank(int b,int p,int map)
	{
		banks[b][p] = map;
	}

	/**
	 * Map one virtual memory address to the phisical memory.
	 *
	 * @param add - Virtual address
	 *
	 * @return The phisical address.
	 */
	private int virtualMemory(int add)
	{
		int page = (add >> 12) & 0x0f;
		int offset = (add & 0xfff);

		return  banks[currentBank][page] * PAGESIZE + offset;

	}

	public void pokeb(int add,int value)
	{
		memory[virtualMemory(add)] = (value & 0xff);
	}

	public int peekb(int add)
	{
		return memory[virtualMemory(add)] & 0xff;
	}



	public void outb( int port, int bite, int tstates )
	{
		switch (port)
		{
			case	PAGELOW:
					page = (page & 0xff00) | bite;
					break;
			case	PAGEHI:
					page = (page & 0x00ff) | (bite << 8);
					break;
			case	PAGEFRAME:
					banks[currentBank][bite] = page;
					//System.out.print("Bank # "+currentBank+" frame # "+bite+" = "+Integer.toHexString(page*PAGESIZE)+" ");
					break;
		}
	}

	public void disconnectCPU(J80 cpu)
	{
	}
	
	public void connectCPU(J80 cpu)
	{
		cpu.addOutPort(PAGEHI,this);
		cpu.addOutPort(PAGELOW,this);
		cpu.addOutPort(PAGEFRAME,this);
	}

	public void resetCPU(J80 cpu)
	{
	}

	public String toString()
	{
		return "MMU : 1024KB Installed in 16 bank of 4KB x 16 $Revision: 330 $";
	}

}