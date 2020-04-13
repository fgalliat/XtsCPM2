package j80.vdu;

/**
 * $Id: Hazeltine1500.java 330 2010-09-14 10:29:28Z mviara $
 * 
 * Hazeltine1500 implementation of VDU
 *
 * @see <a href="http://vt100.net/hazeltine/h1500-rm/h1500-rm.pdf">Reference
 * manual</a>
 *
 * $Log: Hazeltine1500.java,v $
 * Revision 1.4  2008/05/22 22:13:43  mviara
 *
 * Removed debug comment.
 *
 * Revision 1.3  2004/06/20 16:24:33  mviara
 * Some minor change.
 *
 */
public class Hazeltine1500 extends SampleVDU
{
	private int state = 0;
	private int setRow,setCol;

	/**
	 * Default constructor
	 */
	public Hazeltine1500()
	{
		setNumRow(24);
		setNumCol(80);
	}

	/**
	 * Processing leadin character
	 */
	private void leadin(char c)
	{
		switch (c)
		{
			case	18:
					home();
					break;
			case	12:
					if (crt.getRow() > 0)
						crt.setCursor(crt.getRow()-1,crt.getCol());
					break;
			case	11:
					if (crt.getRow() < (crt.getNumRow() - 1))
						crt.setCursor(crt.getRow()+1,crt.getCol());
					break;
			case	17:
					state++;
					return;
					//break;
			case	28:
					cls();
					home();
					break;
			case	29:
					// FIXME clear only reverse ?
					cls();	
					break;
					
			case	15:
					clearEol();
					break;
					
			case	23:
					// FIXME Clear background ?
					clearEos();	// background
					break;

			case	24:
					// FIXME Clear foregound ?
					clearEos(); 
					break;

			case	25:			
					crt.setAtt(crt.NORMAL);
					break;

			case	31:			
					crt.setAtt(crt.HI);
					break;

			case	19:
					deleteLine();
					crt.setCol(0);
					break;
			case	26:
					insertLine();
					crt.setCol(0);
					break;

		}

		state = 0;
	}

	public void putchar(char c)
	{
		switch (state)
		{
			case	0:
				switch (c)
				{
					case	126:
						state = 1;
						break;
					case	16:
						right();
						break;
					default:
						super.putchar(c);
						return;
				}
				break;
			case	1:
					leadin(c);
					break;
			case	2:
					setCol = c;
					setCol = setCol % 96;
					state++;
					break;
			case	3:
					setRow = c;
					setRow &= 0x1f;
					state = 0;
					if (setCol < 0)
						setCol = 0;
					if (setCol >= crt.getNumCol())
						setCol = crt.getNumCol() - 1;
					if (setRow < 0)
						setRow = 0;
					if (setRow >= crt.getNumRow())
						setRow = crt.getNumRow() - 1;
					crt.setCursor(setRow,setCol);
					break;


		}
		//repaint();

	}

	public String toString()
	{
		return "VDU : Hazeltine1500 "+crt.getNumRow()+"x"+crt.getNumCol()+" $Revision: 330 $";
	}
}
