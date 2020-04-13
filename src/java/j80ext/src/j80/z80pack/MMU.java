/**
 * $Id: MMU.java 330 2010-09-14 10:29:28Z mviara $
 * $Name:  $
 *
 * MMU Porting for Z80Pack from Udo Munk
 * 
 */
package j80.z80pack;

public class MMU implements j80.MMU,j80.OutPort,j80.InPort
{
	// Memory address space
	private byte plainMemory[] = new byte[0x10000];
	private byte bankedMemory[] = new byte[32*0x10000];
	private int currentBank = 0;
	private int bankSize = 49152;
	private int maxBank = 16;
	private boolean mmuInit = false;
	
	/**
	 * Read one byte
	 */
	public int peekb(int add)
	{
		add &= 0xffff;
		if (!mmuInit || add >= bankSize)
			return plainMemory[add] & 0xff;
		else
			return bankedMemory[currentBank*0x10000+add] & 0xff;
	}

	/**
	 * Write one byte
	 */
	public void pokeb(int add,int value)
	{
		add &= 0xffff;	
		if (!mmuInit || add >= bankSize)
			plainMemory[add]= (byte)value;
		else
			bankedMemory[currentBank*0x10000+add] = (byte)value;
	}


	/**
	 * Disconnect the CPU
	 */
	public void disconnectCPU(j80.J80 cpu)
	{
	}

	/**
	 * Connect the CPU
	 */
	public void connectCPU(j80.J80 cpu)
	{
		cpu.addInPort(20,this);	// Init
		cpu.addInPort(21,this);	// bank select
		cpu.addInPort(22,this);	// bank size in 256 increment
		cpu.addOutPort(20,this);
		cpu.addOutPort(21,this);
		cpu.addOutPort(22,this);
	}

	public void outb(int port,int value,int states)
	{
		if (port == 20)
		{
			if (value > 31)
				value = 31;
			maxBank = value;
			currentBank = 0;
			System.arraycopy(plainMemory,0,bankedMemory,0,0x10000);
			mmuInit = true;
		}
		else if (port == 21)
		{
			currentBank = value;
		}
		else if (port == 22)
		{
			bankSize = value << 8;
		}
	}
	
	public int inb(int port,int hi)
	{
		if (port == 20)
			return maxBank;
		else if (port == 21)
			return currentBank;
		else if (port == 22)
			return bankSize >> 8;

		return 0;
	}
	
	public void resetCPU(j80.J80 cpu)
	{
	}

	public String toString()
	{
		return "Z80Pack MMU : 64K x 32 Banks installed $Revision: 330 $";
	}

	
}
