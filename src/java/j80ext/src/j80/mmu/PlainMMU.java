package j80.mmu;

import j80.*;

/**
 * $Id: PlainMMU.java 330 2010-09-14 10:29:28Z mviara $
 * 
 * Memory manager for j80 machine <p>
 *
 * This memory manager allow the access to all 64K memory address
 * space of the Z80, in the most simple way one array of 64KB.
 *
 * $Log: PlainMMU.java,v $
 * Revision 1.4  2004/11/22 16:50:34  mviara
 * Some cosmetic change.
 *
 * Revision 1.3  2004/06/20 16:26:26  mviara
 * Some minor change.
 *
 * Revision 1.2  2004/06/16 15:24:20  mviara
 * First CVS Revision.
 *
 *
 */
public class PlainMMU implements MMU
{
	// Memory address space
	private int memory[] = new int[0x10000];

	/**
	 * Read one byte
	 */
	public int peekb(int add)
	{
		return memory[add & 0xffff] & 0xff;
	}

	/**
	 * Write one byte
	 */
	public void pokeb(int add,int value)
	{
		memory[add & 0xffff] = value & 0xff;
	}


	/**
	 * Disconnect the CPU
	 */
	public void disconnectCPU(J80 cpu)
	{
	}

	/**
	 * Connect the CPU
	 */
	public void connectCPU(J80 cpu)
	{
	}

	public void resetCPU(J80 cpu)
	{
	}

	public String toString()
	{
		return "MMU : 64K Ram installed $Revision: 330 $";
	}
	

}


