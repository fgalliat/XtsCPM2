/**
 * $Id: Timer.java 330 2010-09-14 10:29:28Z mviara $
 * $Name:  $
 *
 * Timer Porting for Z80Pack from Udo Munk
 * 
 */
package j80.z80pack;

import java.awt.*;
import java.awt.event.*;


public class Timer implements j80.Peripheral ,j80.OutPort,j80.InPort,j80.Polling
{
	private j80.J80 cpu;
	private boolean timerEnable = false;
	
	public void connectCPU(j80.J80 cpu)
	{
		this.cpu = cpu;
		cpu.addInPort(27,this);	
		cpu.addOutPort(27,this);
		cpu.addPolling(10,this);

	}
	public void outb(int port,int value,int states)
	{
		if (port == 27)
		{
			timerEnable = value != 0;
		}
	}

	/**
	 * Called every 10 ms of emulation time
	 */
	public void polling(j80.J80 cpu)
	{
		if (timerEnable)
			cpu.irq();


	}

	public int inb(int port,int hi)
	{
		if (port == 27)
			return timerEnable ? 1 : 0;
		return 0;
	}

	public void resetCPU(j80.J80 cpu)
	{
	}

	public void disconnectCPU(j80.J80 cpu)
	{
	}

	public String toString()
	{
		return "Z80Pack Timer evey 10 ms $Revision: 330 $";
	}

	
}
