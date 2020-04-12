/**

 thanks to Jack Palevich for his jgb Applet (1997)
           Marat Fayzullin & Pascal Felber (1995,1996) For the DisAsm Tables and GBZ80.java z80 emulator

 @Copyright X-tase 2001

*/
package packages.cpu;

import packages.controllers.*;
import packages.devices.*;
import packages.machine.*;

import java.io.*;
import java.awt.*;



public class GameBoyCPU extends GBZ80 {

 public final static int VBL_IFLAG = 0x01;			// vBlank interupt flag
 public final static int LCD_IFLAG = 0x02;
 public final static int TIM_IFLAG = 0x04;
 public final static int SIO_IFLAG = 0x08;
 public final static int EXT_IFLAG = 0x10;
// OffSets
 public final static int IFLAGS   = 0xFF0F;			/* Interrupt flags: 0.0.0.JST.SIO.TIM.LCD.VBL */
 public final static int ISWITCH  = 0xFFFF;			/* Switches to enable/disable interrupts      */
 
 public final static int JOYPAD  = 0xFF00;			/* Joystick: 1.1.P15.P14.P13.P12.P11.P10      */
 public final static int SIODATA = 0xFF01;			/* Serial IO data buffer                      */
 public final static int SIOCONT = 0xFF02;			/* Serial IO control register                 */
 public final static int DIVREG  = 0xFF04;			/* Divider register (???)                     */
 public final static int TIMECNT = 0xFF05;			/* Timer counter. Gen. int. when it overflows */
 public final static int TIMEMOD = 0xFF06;			/* New value of TimeCount after it overflows  */
 public final static int TIMEFRQ = 0xFF07;			/* Timer frequency and start/stop switch      */
 public final static int LCDCONT = 0xFF40;			/* LCD control register                       */
 public final static int LCDSTAT = 0xFF41;			/* LCD status register                        */
 public final static int SCROLLY = 0xFF42;			/* Starting Y position of the background      */
 public final static int SCROLLX = 0xFF43;			/* Starting X position of the background      */
 public final static int CURLINE = 0xFF44;			/* Current screen line being scanned          */
 public final static int CMPLINE = 0xFF45;			/* Gen. int. when scan reaches this line      */
 public final static int BGRDPAL = 0xFF47;			/* Background palette                         */
 public final static int SPR0PAL = 0xFF48;			/* Sprite palette #0                          */
 public final static int SPR1PAL = 0xFF49;			/* Sprite palette #1                          */
 public final static int WNDPOSY = 0xFF4A;			/* Window Y position                          */
 public final static int WNDPOSX = 0xFF4B;			/* Window X position                          */

 protected boolean inRun = false;
 protected GameBoy  gb   = null;

 protected int[][] ram    = new int[8][0x2000];
 protected int[][] pages  = new int[8][];			// 8 x 8kb pointeur d'addresses
 protected int     tCount = 0;					// Timer counter
 protected int     tStep  = 0;					// Timer increment
 protected int     iMask  = 0;					// masque d'event reset

  boolean mbcType;            			/* MBC type: 1 for MBC2, 0 for MBC1             */
  int romMap[][] = new int[256][];	       	/* Addresses of ROM banks                       */
  int romBank;            			/* Number of ROM bank currently used            */
  int romMask;					/* Mask for the ROM bank number                 */
  int romBanks;					/* Total number of ROM banks                    */
  int ramMap[][] = new int[256][];		/* Addresses of RAM banks                       */ 
  int ramBank;					/* Number of RAM bank currently used            */
  int ramMask;					/* Mask for the RAM bank number                 */
  int ramBanks;	


 protected boolean lineDelay = false;				// When 1, CMPLINE interrupts are delayed
 protected int     uPeriod   = 1;				// nb de vBlank par maj screen

 public GameBoyCPU(GameBoy gb) {
  this.gb = gb;
 }


 public void setRun(boolean inRun) {
  this.inRun = inRun;
  if (inRun) {
   debug("allumée", 3);
   reset();
   dumpSlot();
   initReg();
   execute();
  }
  else {
   reset();   
   debug("éteinte", 3);
  }
 }

 public void reset() {
  ram = new int[8][8192];
  super.reset();
  debug("reset", 3);
 }

 public void initReg() {
  debug("debut de l'inistialisation CPU", 9);

  for (int i = 0; (i <= (8 - 1)); i++) pages[i]=ram[i];
  romMap[0] = ram[2];
  romBanks  = 2 << hardRead( 0x0148);
  if( (hardRead(0x0149) & 0x03) == 3) ramBanks = 4;
  else                                ramBanks = 0;
  mbcType = (hardRead(0x0147) > 3);						// !!! si == 4 ou > 6 ERROR
  M_WRMEM(LCDCONT, 0x81);
  M_WRMEM(LCDSTAT, 0x00);
  M_WRMEM(CURLINE, 0x00);
  M_WRMEM(CMPLINE, 0xFF);
  M_WRMEM(IFLAGS,  0x00);
  M_WRMEM(ISWITCH, 0x00);
  M_WRMEM(TIMECNT, 0x01);
  M_WRMEM(TIMEMOD, 0x01);
  M_WRMEM(TIMEFRQ, 0xF8);
  M_WRMEM(SIODATA, 0x00);
  M_WRMEM(SIOCONT, 0x7E);
  M_WRMEM(BGRDPAL, 0xE4);
  M_WRMEM(SPR0PAL, 0xE4);
  M_WRMEM(SPR1PAL, 0xE4);

  if (romBanks < 3) romMask = 0;
  else {
   int i = 1;
   for(i=1;(i <= (romBanks - 1)); i <<= 1);
   romMask = i - 1;
   romBank = 1;
  }

  if (ramMap[0] == null) ramMask = 0;
  else {
   int i = 1;
   for(i=1;(i <= (ramBanks - 1)); i <<= 1);
   ramMask = i - 1;
   ramBank = 0;
  }

/*

 <...>

  hardPageWrite(3, hardPageRead(2) + 0x2000 );
*/

  debug( romBanks + "bancs de rom", 9);
  debug( ramBanks + "bancs de ram", 9);

  tStep  = 32768;
  tCount = 0;

  gb.getDisplayInterface().initInterface();
  debug("fin de l'inistialisation CPU", 9);
 }

 public void dumpSlot() {										// vide le slot dans la ram
  int[] slotB = gb.getSlotInterface().getSlotBuffer();
  for( int i = 0; (i <= (0x4000 - 1)); i++) hardWrite(i, slotB[i]);
  debug("vidage du slotBuffer dans la RAM effectué", 9);
 }

 public void M_WRMEM(int addr, int value) {								// writeMem
  if ( addr > 0xFEFF) processWrite(addr, value);
  else {
   if (addr > 0x7FFF) pages[addr>>13][addr&0x1FFF] =  value;
   else {
    switch( addr & 0xE000) {
     case 0xC000: break;
     case 0x8000: { hardWrite(addr, value);     return; }
     case 0xA000: { pages[5][addr&0x1FFF] = value; return; }
     case 0xE000: processWrite(addr, value);
     case 0x2000: {
      if (mbcType && ((addr & 0xFF00) != 0x2100)) return;
      value &= romMask;
      if (value == 0) value++;
      if( (romMask != 0) && (value != romBank)) {
       romBank = value;
       if (romMap[value] != null) { pages[2]  = romMap[value];  pages[3]  = romMap[value+1]; }
       else                       { pages[2]  = ram[2];         pages[2]  = ram[3]; debug("ici", 9); } // pointeuer de pages ???
       debug("rom bank selected :"+value, 9);
      }
     return;
     }

     case 0x4000: {
      value &= ramMask;
      if( (ramMask != 0) && ( !mbcType) && (value != ramBank)) {
       ramBank = value;
       if (ramMap[value] != null) pages[5] = ramMap[value];
       else                       pages[5] = ram[5];
       debug("ram bank selected :"+value, 9);
      }
     return;
     }

     case 0x0000: break;
     case 0x6000: { debug("tentative d'ecriture en rom @"+addr+"->"+value, 9); }
    }
   }
  }
 } // m_wrmem



 public void processWrite(int addr, int value) {
  switch(addr) {
   case JOYPAD  : debug("ecriture sur le joypad", 9); break;
   case SIODATA : debug("ecriture sur le SIODATA", 9); break;
   case SIOCONT : debug("ecriture sur le SIOCONT", 9); break;
   case DIVREG  : debug("ecriture sur le DIVREG", 9); break;
   case TIMECNT : debug("ecriture sur le TIMECNT", 9); break;
   case TIMEMOD : debug("ecriture sur le TIMEMOD", 9); break;
   case TIMEFRQ : debug("ecriture sur le TIMEFRQ", 9); break;

   case LCDCONT : {
     debug("ecriture sur le LCDCONT", 9);
     gb.getDisplayInterface().setChrGen( (((value&0x10) != 0)? 0x8000: 0x8800) );
     gb.getDisplayInterface().setBgdTab( (((value&0x08) != 0)? 0x9C00: 0x9800) );
     gb.getDisplayInterface().setWndTab( (((value&0x40) != 0)? 0x9C00: 0x9800) );
    }
    break;

   case LCDSTAT : {
     debug("ecriture sur le LCDSTAT", 9);
     value = (value & 0xF8) | ( hardRead(LCDSTAT) & 0x07) ;
    }
    break;

   case SCROLLY : debug("ecriture sur le SCROLLY", 9); break;
   case SCROLLX : debug("ecriture sur le SCROLLX", 9); break;

   case CURLINE : {
     debug("ecriture sur le CURLINE", 9);
     value = 0;
    }
    break;

   case BGRDPAL : {
     debug("ecriture sur le BGRDPAL", 9);
     gb.getDisplayInterface().writeBPal(value);
    }
    break;

   case SPR0PAL : {
     debug("ecriture sur le SPR0PAL", 9);
     gb.getDisplayInterface().writeSPal0(value);
    }
    break;

   case SPR1PAL : {
     debug("ecriture sur le SPR1PAL", 9);
     gb.getDisplayInterface().writeSPal1(value);
    }
    break;

   case WNDPOSY : debug("ecriture sur le WNDPOSY", 9); break;
   case WNDPOSX : debug("ecriture sur le WNDPOSX", 9); break;
  }
  hardWrite(addr, value);
 }



 public int M_RDMEM(int addr) {										// readMem
  debug("request for memory read @"+addr, 10);
  switch(addr) {
   case JOYPAD  : debug("lecture sur le joypad", 9); break;
   case SIODATA : debug("lecture sur le SIODATA", 9); break;
   case SIOCONT : debug("lecture sur le SIOCONT", 9); break;
   case DIVREG  : debug("lecture sur le DIVREG", 9); break;
   case TIMECNT : debug("lecture sur le TIMECNT", 9); break;
   case TIMEMOD : debug("lecture sur le TIMEMOD", 9); break;
   case TIMEFRQ : debug("lecture sur le TIMEFRQ", 9); break;
   case LCDCONT : debug("lecture sur le LCDCONT", 9); break;
   case LCDSTAT : debug("lecture sur le LCDSTAT", 9); break;
   case SCROLLY : debug("lecture sur le SCROLLY", 9); break;
   case SCROLLX : debug("lecture sur le SCROLLX", 9); break;
   case CURLINE : debug("lecture sur le CURLINE", 9); break;
   case BGRDPAL : debug("lecture sur le BGRDPAL", 9); break;
   case SPR0PAL : debug("lecture sur le SPR0PAL", 9); break;
   case SPR1PAL : debug("lecture sur le SPR1PAL", 9); break;
   case WNDPOSY : debug("lecture sur le WNDPOSY", 9); break;
   case WNDPOSX : debug("lecture sur le WNDPOSX", 9); break;
  }
//  int res = hardRead(addr);
  int res = pages[addr>>13][addr&0x1FF];
 return(res);
 }

 public int doInterrupt() {
  debug("request for interrupt", 10);
  int[] LCDstates = new int[]{2, 2, 3, 3, 3, 3, 0, 0, 0, 0, 0};
  int   lCount    = 0;
  int   uCount    = 1;
  int   aCount    = 0;
  GameBoyDisplayInterface gbDi = gb.getDisplayInterface();

  hardWrite(DIVREG, hardRead(DIVREG) + 1 );
  lCount = (lCount + 1) % 11;
  if (hardRead(CURLINE) < 144) hardWrite(LCDSTAT, ( (hardRead(LCDSTAT) & 0xFC) | LCDstates[lCount]) );

  switch(lCount) {
   case 0: break;

   case 2: { 								// Vblank Interupt
    if (hardRead(CURLINE) == 144) {
     if ( ((hardRead(ISWITCH) & VBL_IFLAG) != 0) && ((hardRead(LCDCONT) & 0x80) != 0) ) {
      hardWrite(IFLAGS, VBL_IFLAG);
      iMask = VBL_IFLAG;
      return( 0x0040 );
     }
    }
    return( 0xFFFF );	// no Interupt
   }

   case 7: {								// Hblank Interupt
    if ( ((hardRead(LCDSTAT) & 0x08) != 0) && (hardRead(CURLINE) < 144) ) {
     if ( ((hardRead(ISWITCH) & LCD_IFLAG) != 0) && ((LCDCONT & 0x80) != 0) ) {
      hardWrite(IFLAGS, LCD_IFLAG);
      iMask = LCD_IFLAG;
      return( 0x0048 );
     }
    }
    return( 0xFFFF );	// no Interupt
   }
//   default : return( 0xFFFF );	// no Interupt (seulement action sur lcdstate)
  }

  hardWrite(CURLINE, (hardRead(CURLINE) + 1) % 154 );
  if ( uCount == 0 && (hardRead(CURLINE) < 144)) gbDi.refreshLine( hardRead(CURLINE) );
  gbDi.setSprFlag( gbDi.getSprFlag() | hardRead(LCDCONT) & 0x02 );

  int increment = - 1;
  if (lineDelay) increment = (hardRead(CURLINE)+1 ) % 154;
  else           increment = hardRead(CURLINE);

  if (increment != hardRead(CMPLINE) ) {
   hardWrite(LCDSTAT, hardRead(LCDSTAT) & 0xFB );
   increment = 0x00;
  }
  else {
   hardWrite(LCDSTAT, hardRead(LCDSTAT) | 0x04 );
   increment = 0x40;
  }

  if (hardRead(CURLINE) == 144) {
   hardWrite(LCDSTAT, (hardRead(LCDSTAT) & 0xFC) | 0x01);				// set VBLANK
   increment |= 0x10;
   
// ecriture du son ....

   if (uCount != 0) uCount--;						// refresh screen si besoin
   else {
    if ((gbDi.getSprFlag() != 0 ) && ((hardRead(LCDCONT) & 0x80) != 0)) gbDi.resfreshSprites();
    gbDi.refreshScreen();
    gbDi.setSprFlag(0);
    uCount = uPeriod;
   }

// gestion interruption link

// gestion du joystick   
  }

  // genere l'interrupt LCD
  if ( ((increment & hardRead(LCDSTAT)) != 0) && ((hardRead(ISWITCH) & LCD_IFLAG) != 0) 
       && ((hardRead(LCDCONT) & 0x80) != 0) ){
   hardWrite(IFLAGS, (hardRead(IFLAGS) | LCD_IFLAG ));
  }

  // genere l'interrupt du timer
  if( (hardRead(TIMEFRQ) & 0x04) != 0 ) {
   tCount += tStep;
   if ( (tCount & 0xFFFF0000) != 0) {
    int l = hardRead(TIMECNT) + (tCount >> 16);
    tCount &= 0x0000FFFF;
    if ((l & 0xFFFFFF00) != 0) {
     hardWrite(TIMECNT, hardRead(TIMEMOD));
     if ((hardRead(ISWITCH) & TIM_IFLAG) != 0) hardWrite(IFLAGS, TIM_IFLAG);
    }
    else hardWrite(TIMECNT, l);
   }
  }

  // determine l'addr d'interruption
  if (increment == hardRead(IFLAGS) ) {
   if ((increment & EXT_IFLAG) != 0) { iMask = EXT_IFLAG; return(0x0060); }
   if ((increment & SIO_IFLAG) != 0) { iMask = SIO_IFLAG; return(0x0058); }
   if ((increment & TIM_IFLAG) != 0) { iMask = TIM_IFLAG; return(0x0050); }
   if ((increment & LCD_IFLAG) != 0) { iMask = LCD_IFLAG; return(0x0048); }
   if ((increment & VBL_IFLAG) != 0) { iMask = VBL_IFLAG; return(0x0040); }
  }

 return(0xFFFF);						// no Interupt
 }

 protected int hardRead( int addr ) {
  int res = ram[addr>>13][addr&0x1FFF];
  if (res < 0) debug("!!! RAM["+addr+"] ------> '"+res+"'", 8);
 return(res);
 }

 protected void hardWrite(int addr, int value) {
  if (value < 0) debug("!!! '"+value+"'  ----> for RAM["+addr+"]", 8);
  ram[addr>>13][addr&0x1FFF] = value;
 }


 protected void debug( String msg, int level) { GameBoy.debug("-cpu> "+msg, level); }

}