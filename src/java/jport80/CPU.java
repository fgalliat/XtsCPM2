
/*
  no static methods in hardware ... may want 2 CPUs ...
*/

public class CPU {

	protected MEM mem;

	protected Z80 z80;

	public CPU(MEM mem) {
		this.mem = mem;
		this.z80 = new Z80(mem) {
			public void outb(int port,int value,int status) 
			{
				System.out.println("Bios call ("+ port +", "+value+", "+ status +")");
			}

			public int inb(int port,int hi) 
			{
				System.out.println("Bdos call ("+ port +", "+hi +")");
				return 0xff;
			}

			@Override
			public void step() {
				// System.out.print("+");
			}
		};
	}

	void setStatus(int status) {
		System.out.println("CPU.setStatus("+ status +")");
		z80.state_HALT = false;
	}

	int getStatus() {
		System.out.println("CPU.getStatus()");
		return 0;
	}

	int getPCX() {
		return z80.getPPC();
	}

	public static class Register {
		int value;
		void reset() { this.value = 0; }
		void set(int value) { this.value = value; }
		
		void add(int value) { this.value += value; }
		int sub(int value) { this.value -= value; return this.value; }
		void andEq(int value) { this.value &= value; }
		void orEq(int value) { this.value |= value; }

		int inc() { this.value++; return this.value; }
		int dec() { this.value--; return this.value; }
		int get() { return this.value; }
		void copyTo(Register reg) { reg.set(this.get()); }
	}

	// int PCX = 0; /* external view of PC                          */
	Register AF = new Register()  /* AF register                                  */
	{
		void set(int value) {
			z80.A = value / 256;
			z80.F = value % 256;
		}
		int get() {
			return ( z80.A * 256 ) + z80.F;
		}
	};
	Register BC = new Register()  /* BC register                                  */
	{
		void set(int value) {
			z80.B = value / 256;
			z80.C = value % 256;
		}
		int get() {
			return ( z80.B * 256 ) + z80.C;
		}
	};
	Register DE = new Register()  /* DE register                                  */
	{
		void set(int value) {
			z80.D = value / 256;
			z80.E = value % 256;
		}
		int get() {
			return ( z80.D * 256 ) + z80.E;
		}
	};
	Register HL = new Register()  /* HL register                                  */
	{
		void set(int value) {
			z80.H = value / 256;
			z80.L = value % 256;
		}
		int get() {
			return ( z80.H * 256 ) + z80.L;
		}
	};
    // Register IX = new Register();  /* IX register                                  */
	// Register IY = new Register();  /* IY register                                  */

	// useful for Main.java
	Register PC = new Register()  /* program counter                              */
	{
		void set(int value) {
			z80.setPC(value);
		}
		int get() {
			return z80.getPC();
		}
	};
	
    // Register SP = new Register();  /* SP register                                  */
    // Register AF1 = new Register(); /* alternate AF register                        */
    // Register BC1 = new Register(); /* alternate BC register                        */
    // Register DE1 = new Register(); /* alternate DE register                        */
    // Register HL1 = new Register(); /* alternate HL register                        */
    // Register IFF = new Register(); /* Interrupt Flip Flop                          */
    // Register IR = new Register();  /* Interrupt (upper) / Refresh (lower) register */
    // int Status = 0; /* Status of the CPU 0=running 1=end request 2=back to CCP */


void Z80reset() {
	System.out.println("RESET");
	// z80.DEBUG = true;
	z80.reset();
}


boolean DBUG = !false;

void  Z80run() {
	System.out.println("CPU > run (*)");
	int cycles = 4 * 1000; // 4MHz
	while( z80.state_HALT == false ) {
		z80.exec(cycles);
		// System.out.print('.');
	}
}

} 