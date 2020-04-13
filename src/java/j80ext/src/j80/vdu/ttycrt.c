#include <stdio.h>

#ifdef __WIN32__
#include <windows.h>
#include <conio.h>
#endif


#ifdef __NCURSES__
#include <curses.h>
#endif

#include "j80_vdu_ttyCRT.h"

#ifdef __WIN32__
static char lineBuffer[256];
static CONSOLE_SCREEN_BUFFER_INFO csb;
static HANDLE hStdOut;
static HANDLE hStdIn;
static DWORD  oldConsoleInMode;
static DWORD  oldConsoleOutMode;
static WORD	  curColor;
static WORD	  attributes[] =
{
	FOREGROUND_GREEN							, // NORMAL
	BACKGROUND_GREEN							, // REVERSE
	FOREGROUND_GREEN|FOREGROUND_INTENSITY		, // HI
	BACKGROUND_GREEN|BACKGROUND_INTENSITY		, // REVERSE + HI
	
	
};


JNIEXPORT void JNICALL Java_j80_vdu_ttyCRT_ttyReset(JNIEnv *env, jobject o)
{
	COORD c;
	CONSOLE_CURSOR_INFO cursor;

	SetConsoleMode(hStdIn,oldConsoleInMode);
	SetConsoleMode(hStdOut,oldConsoleOutMode);

	GetConsoleCursorInfo(hStdOut,&cursor);
	cursor.bVisible = TRUE;
	SetConsoleCursorInfo(hStdOut,&cursor);


	c.X = 0;
	c.Y = csb.dwSize.Y - 2;

	SetConsoleCursorPosition(hStdOut,c);
	SetConsoleTextAttribute(hStdOut,FOREGROUND_RED|FOREGROUND_BLUE|FOREGROUND_GREEN);

}

JNIEXPORT void JNICALL Java_j80_vdu_ttyCRT_ttySetCursor(JNIEnv *env, jobject c,jboolean mode)
{
	CONSOLE_CURSOR_INFO cursor;

	GetConsoleCursorInfo(hStdOut,&cursor);
	cursor.bVisible = mode == JNI_TRUE ? TRUE : FALSE;
	SetConsoleCursorInfo(hStdOut,&cursor);
	
}


JNIEXPORT void JNICALL Java_j80_vdu_ttyCRT_ttySetCursorPosition(JNIEnv *env, jobject o,jint row,jint col)
{
	COORD c;

	c.X = col;
	c.Y = row;

	SetConsoleCursorPosition(hStdOut,c);
}


JNIEXPORT void JNICALL Java_j80_vdu_ttyCRT_ttyPutchar(JNIEnv *env, jobject c,jbyteArray arr,jint len,jint a)
{
	//SetConsoleTextAttribute(hStdOut, currentColor);
	int color = a & 3;
	DWORD written = 0;
	int i;
	jbyte * body;
	

	if (attributes[color] != curColor)
	{
		curColor = attributes[color];
		SetConsoleTextAttribute(hStdOut,curColor);

	}

	body = (*env)->GetByteArrayElements(env, arr, 0);

	for (i = 0 ; i < len ; i++)
		lineBuffer[i] = body[i];
	(*env)->ReleaseByteArrayElements(env, arr, body, 0);
	
	WriteConsole(hStdOut,lineBuffer,len,&written,NULL);

}


JNIEXPORT jboolean JNICALL Java_j80_vdu_ttyCRT_ttyInit(JNIEnv *env, jobject c)
{
	curColor = 0;
	hStdOut = GetStdHandle(STD_OUTPUT_HANDLE);
	hStdIn  = GetStdHandle(STD_INPUT_HANDLE);

	GetConsoleScreenBufferInfo(hStdOut,&csb);
	GetConsoleMode(hStdIn,&oldConsoleInMode);
	GetConsoleMode(hStdOut,&oldConsoleOutMode);
	SetConsoleMode(hStdIn,oldConsoleInMode &~ ENABLE_PROCESSED_INPUT);
	SetConsoleMode(hStdOut,oldConsoleOutMode &~ ENABLE_WRAP_AT_EOL_OUTPUT);

	return JNI_TRUE;
	
}

JNIEXPORT jint JNICALL Java_j80_vdu_ttyCRT_ttyGetNumCol(JNIEnv *env, jobject c)
{
	return csb.dwSize.X;
}

JNIEXPORT jint JNICALL Java_j80_vdu_ttyCRT_ttyGetNumRow(JNIEnv *env, jobject c)
{
	return csb.dwSize.Y;
}



JNIEXPORT jboolean JNICALL Java_j80_vdu_ttyCRT_ttyKbhit(JNIEnv *env, jobject c)
{
	return kbhit() == 0 ? JNI_FALSE : JNI_TRUE;
}

JNIEXPORT jint JNICALL Java_j80_vdu_ttyCRT_ttyGetch(JNIEnv *env, jobject c)
{
	int ch;

	ch = getch();


	if (ch == 0 || ch == 0xe0)
	{
		ch = ((getch() & 0xff) << 8);
	}

	return ch;
	
}
#else


#ifdef __NCURSES__
JNIEXPORT void JNICALL Java_j80_vdu_ttyCRT_ttyReset(JNIEnv *env, jobject o)
{
	endwin();
}

JNIEXPORT void JNICALL Java_j80_vdu_ttyCRT_ttySetCursor(JNIEnv *env, jobject c,jboolean mode)
{
}

JNIEXPORT void JNICALL Java_j80_vdu_ttyCRT_ttySetCursorPosition(JNIEnv *env, jobject o,jint row,jint col)
{

	move(row,col);
	refresh();

}

JNIEXPORT void JNICALL Java_j80_vdu_ttyCRT_ttyPutchar(JNIEnv *env, jobject c,jbyteArray arr,jint len,jint a)
{
	int i;
	jbyte * body;


	body = (*env)->GetByteArrayElements(env, arr, 0);

	for (i = 0 ; i < len ; i++)
		addch(body[i]);
	(*env)->ReleaseByteArrayElements(env, arr, body, 0);
	refresh();
}

JNIEXPORT jboolean JNICALL Java_j80_vdu_ttyCRT_ttyInit(JNIEnv *env, jobject c)
{

	initscr();
	start_color();
	raw();
	noecho();
	nonl();
	intrflush(stdscr,FALSE);
	keypad(stdscr,TRUE);

}
JNIEXPORT jint JNICALL Java_j80_vdu_ttyCRT_ttyGetNumCol(JNIEnv *env, jobject c)
{
	return COLS;
}

JNIEXPORT jint JNICALL Java_j80_vdu_ttyCRT_ttyGetNumRow(JNIEnv *env, jobject c)
{
	return LINES;
}
JNIEXPORT jboolean JNICALL Java_j80_vdu_ttyCRT_ttyKbhit(JNIEnv *env, jobject c)
{
	int ch;
	
	nodelay(stdscr,TRUE);
	ch = getch();
	nodelay(stdscr,FALSE);


	if (ch != ERR)
	{

		ungetch(ch);
		return JNI_TRUE;
	}

	return JNI_FALSE;
}

JNIEXPORT jint JNICALL Java_j80_vdu_ttyCRT_ttyGetch(JNIEnv *env, jobject c)
{


	int ch =  getch();

	switch (ch)
	{
		case	KEY_DOWN:
			ch = 0x5000;
			break;
		case	KEY_UP:
			ch = 0x4800;
			break;
		case	KEY_LEFT:
			ch = 0x4b00;
			break;
		case	KEY_RIGHT:
			ch = 0x4d00;
			break;
		default:
			if (ch > 0xFF)
				ch = 0x4400;
	}
	
	return ch;
}
#else	// __NCURSES__
JNIEXPORT void JNICALL Java_j80_vdu_ttyCRT_ttyReset(JNIEnv *env, jobject o)
{
	
}

JNIEXPORT void JNICALL Java_j80_vdu_ttyCRT_ttySetCursor(JNIEnv *env, jobject c,jboolean mode)
{
}

JNIEXPORT void JNICALL Java_j80_vdu_ttyCRT_ttySetCursorPosition(JNIEnv *env, jobject o,jint row,jint col)
{
	printf("\033[%d;%dH",row,col);
	fflush(stdout);
}

JNIEXPORT void JNICALL Java_j80_vdu_ttyCRT_ttyPutchar(JNIEnv *env, jobject c,jbyteArray arr,jint len,jint a)
{
	int i;
	jbyte * body;


	body = (*env)->GetByteArrayElements(env, arr, 0);

	for (i = 0 ; i < len ; i++)
		putchar(body[i]);
	fflush(stdout);
}

JNIEXPORT jboolean JNICALL Java_j80_vdu_ttyCRT_ttyInit(JNIEnv *env, jobject c)
{
	printf("\33[2J");

}
JNIEXPORT jint JNICALL Java_j80_vdu_ttyCRT_ttyGetNumCol(JNIEnv *env, jobject c)
{
	return 80;
}

JNIEXPORT jint JNICALL Java_j80_vdu_ttyCRT_ttyGetNumRow(JNIEnv *env, jobject c)
{
	return 23;
}
JNIEXPORT jboolean JNICALL Java_j80_vdu_ttyCRT_ttyKbhit(JNIEnv *env, jobject c)
{
	return JNI_FALSE;
}

JNIEXPORT jint JNICALL Java_j80_vdu_ttyCRT_ttyGetch(JNIEnv *env, jobject c)
{
	return 0x4400;
}

#endif	// __NCURSES__
#endif  // __WIN32__
