/**
 * $Id: Network.java 330 2010-09-14 10:29:28Z mviara $
 * $Name:  $
 *
 * NETWORK Porting for Z80Pack from Udo Munk
 * Not YET Available
 * 
 */
package j80.z80pack;

import java.awt.*;
import java.awt.event.*;


public class Network implements j80.Peripheral ,j80.OutPort,j80.InPort
{
	private j80.J80 cpu;
	
	public void connectCPU(j80.J80 cpu)
	{
		this.cpu = cpu;

		for (int i = 40 ; i <= 51 ; i++)
		{
			cpu.addInPort(i,this);
			cpu.addOutPort(i,this);
		}
	}
	public void outb(int port,int value,int states)
	{
	}

	public int inb(int port,int hi)
	{
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
		return "Z80Pack Network interface $Revision: 330 $";
	}

	
}
