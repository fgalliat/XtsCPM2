/**
 * $Id: FDC.java 330 2010-09-14 10:29:28Z mviara $
 * $Name:  $
 *
 * FDC Porting for Z80Pack from Udo Munk
 * 
 */
package j80.z80pack;

public class FDC extends j80.FDC
{
	public FDC()
	{
		super();

		/* change defaul register address */
		DRIVE		= 10;
		TRACKLOW	= 11;
		SECTORLOW	= 12;
		CMD		= 13;
		STATUS		= 14;
		DMALOW		= 15;
		DMAHI		= 16;
		SECTORHI	= 17;
		TRACKHI		= 18;
	}
}

   