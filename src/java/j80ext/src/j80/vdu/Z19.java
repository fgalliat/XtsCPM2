package j80.vdu;

/**
 * $Id: Z19.java 330 2010-09-14 10:29:28Z mviara $
 * 
 * Zenith Z19 implemenfation of VDU
 *
 * @see <a href = "http://vt100.net/heath/z19-om/z19-om.pdf">Reference
 * manual</a>
 *
 * 
 * $Log: Z19.java,v $
 * Revision 1.2  2004/06/20 16:24:33  mviara
 * Some minor change.
 *
 * Revision 1.1  2004/06/18 13:10:36  mviara
 * Added support per Zenith Z19 terminal.
 *
 *
 */
public class Z19 extends SampleVDU
{
	int state;
	int cursorRow;
	
	public Z19()
	{
		setNumRow(25);
		setNumCol(80);
		state = 0;
	}


	private void escape(char c)
	{
		switch (c)
		{
					// Normal mode
			case	'q':
					crt.setAtt(crt.NORMAL);
					break;
					
					// Reverse mode
			case	'p':
					crt.setAtt(crt.REVERSE);
					break;
					
					// Overwrite mode
			case	'O':
					setInsertMode(false);
					break;
					
					// Insert mode
			case	'@':
					setInsertMode(true);
					break;
					
					// Delete char
			case	'N':
					deleteChar();
					break;
					
			case	'M':
					deleteLine();
					setCol(0);
					break;
					
			case	'L':
					insertLine();
					setCol(0);
					break;
					
					// Clear to end of line
			case	'K':
					clearEol();
					break;
					
			case	'J':
					clearEos();
					break;
					
					// Erase the current line
			case	'l':
					clearLine();
					break;

					// Clear from beginning to cursor
			case	'o':
					clearToCursor();
					break;
					
			case	'E':
					cls();
			case	'H':
					home();
					break;
			case	'C':
					right();
					break;
			case	'D':
					left();
					break;
			case	'B':
					down();
					break;
			case	'A':
					up();
					break;
			case	'I':
					if (crt.getRow() == 0)
					{
						crt.scrollUp(0,crt.getScreenSize(),crt.getNumCol());

					}
					else
						up();
					break;
			case	'n':
					crt.addKey(27);
					crt.addKey('Y');
					crt.addKey(crt.getRow()+32);
					crt.addKey(crt.getCol()+32);
					break;

					// Save cursor position
			case	'j':
					saveCursor();
					break;
					// Restore cursor position
			case	'k':
					restoreCursor();
					break;

					//  Set cursor position
			case	'Y':
					state = 2;
					break;
		}
		if (state == 1)
			state = 0;
	}
	
	public void putchar(char c)
	{
		switch (state)
		{
			case	0:
					switch (c)
					{
						case	27:
								state =1;
								break;
						default:
								super.putchar(c);
								break;
					}
					break;
			case	1:
					escape(c);
					break;
			case	2:
					cursorRow = c - 32;
					state++;
					break;
			case	3:
					crt.setCursor(cursorRow,c - 32);
					state = 0;
					break;
		}
	}
	
	public String toString()
	{
		return "VDU : Zenith Z19 "+crt.getNumRow()+"x"+crt.getNumCol()+" $Revision: 330 $";
	}

}
