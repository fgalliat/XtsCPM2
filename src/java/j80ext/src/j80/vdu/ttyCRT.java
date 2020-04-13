package j80.vdu;

import java.awt.event.KeyEvent;

/**
 * $Id: ttyCRT.java 330 2010-09-14 10:29:28Z mviara $
 * 
 * Console implementation of j80.CRT<p>
 *
 * Require a JNI library "ttyCRT" this version support only WIN32.
 *
 * $Log: ttyCRT.java,v $
 * Revision 1.6  2008/05/15 17:07:17  mviara
 * Added preliminary support for Z80Pack. Fixed bug in idle loop detection.
 *
 * Revision 1.5  2008/05/15 12:33:37  mviara
 *
 * Fixed bug in reset CRT.
 *
 * Revision 1.4  2008/05/15 12:21:47  mviara
 *
 * Porting under Linux
 *
 * Revision 1.3  2008/05/14 16:53:38  mviara
 * Added support to terminate the simulation pressing the key F10.
 *
 * Revision 1.2  2004/06/20 16:24:33  mviara
 * Some minor change.
 *
 */
public class ttyCRT extends AbstractCRT implements Runnable

{
	public final int UP = 0x4800;
	public final int DOWN = 0x5000;
	public final int LEFT = 0x4b00;
	public final int RIGHT = 0x4d00;
	public final int KEY_F10 = 0x4400;
	
	private native void	ttyPutchar(byte chars[],int len,int att);
	private native void	ttySetCursorPosition(int row,int col);
	private native void	ttySetCursor(boolean mode);
	private native boolean	ttyKbhit();
	private native int	ttyGetch();
	private native int	ttyGetNumRow();
	private native int	ttyGetNumCol();
	private native boolean	ttyInit();
	private native void	ttyReset();
	
	private byte		line[];
	
	
	public ttyCRT()
	{
		System.loadLibrary("ttyCRT");
		ttyInit();
		NROW = ttyGetNumRow();
		NCOL = ttyGetNumCol();
	}

	/**
	 * D'ont allow row number change.
	 */
	public void setNumRow(int row)
	{
		if (row < ttyGetNumRow())
		{
			NROW = row;
			statusLine = true;
		}
	}

	/**
	 * D'ont allow col number change.
	 */
	public void setNumCol(int col)
	{
	}
	


	public void init()
	{
		super.init();
		line = new byte[NCOL];
		
		Thread t = new Thread(this);
		t.setPriority(t.MAX_PRIORITY);
		t.start();
		
	}

	public void reset()
	{
		super.reset();
		ttyReset();
	}
	
	public void run()
	{
		for (;;)
		{
			try
			{
				Thread.sleep(REFRESH_TIME);
			}
			catch (Exception e)
			{
				
			}
			
			if (ttyKbhit())
			{

				int c = ttyGetch();
				if (c == KEY_F10)
				{
					terminate();
				}
				
				switch (c)
				{
					case	UP:
							c = KeyEvent.VK_UP;
							break;
					case	DOWN:
							c = KeyEvent.VK_DOWN;
							break;
					case	LEFT:
							c = KeyEvent.VK_LEFT;
							break;
					case	RIGHT:
							c = KeyEvent.VK_RIGHT;
							break;
				}

				String s = getDefinedKey(c);
				
				if (s != null)
				{
					addKey(s);
				}
				else
				{
					addKey(c);
				}

			}

			if (isTerminate())
				continue;

			if (!screenChanged)
				continue;
			
			synchronized (changed)
			{
				screenChanged = false;

				ttySetCursor(false);

				for (int r = 0 ; r < (NROW + (statusLine ? 1 : 0)); r++)
				{

					if (changed[r])
					{
						changed[r] = false;
						int count = 0;
						int pos = r * NCOL;
						byte att = videoAtt[pos];

						ttySetCursorPosition(r,0);

						for (int c = 0 ; c < NCOL ;c++,pos++)
						{
							//System.out.println("pos "+pos+" count "+count+" r "+r+" c "+c+" NROW "+NROW+" MNCOL "+NCOL);

							if (att != videoAtt[pos])
							{
								ttyPutchar(line,count,att);
								att = videoAtt[pos];
								count = 0;
							}

							line[count++] = videoChar[pos];
						}

						ttyPutchar(line,count,att);
					}
				}

				if (getCursor() == true)
				{
					ttySetCursorPosition(row,col);
					ttySetCursor(true);
				}

			}
		}
		
	}
	
}
