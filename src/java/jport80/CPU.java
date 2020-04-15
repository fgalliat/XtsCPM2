import z80core.MemIoOps;
import z80core.NotifyOps;
import z80core.Z80;

/*
  no static methods in hardware ... may want 2 CPUs ...
*/

public class CPU {

	protected MEM mem;

	protected Z80 z80;

	public CPU(MEM mem) {
		this.mem = mem;
		MemIoOps _mem = new MemIoOps() {
			@Override
			public int peek8(int address) {
				tstates += 3; // 3 clocks for read byte from RAM
				// return z80Ram[address] & 0xff;
				return CPU.this.mem._RamRead(address);
			}
		
			@Override
			public void poke8(int address, int value) {
				tstates += 3; // 3 clocks for write byte to RAM
				// z80Ram[address] = (byte)value;
				CPU.this.mem._RamWrite(address, (char)value);
			}

			@Override
			public int inPort(int port) {
				System.out.print("_Bdos()");
				tstates += 4; // 4 clocks for read byte from bus
				return z80Ports[port] & 0xff;
			}
		
			@Override
			public void outPort(int port, int value) {
				System.out.print("_Bios()");
				tstates += 4; // 4 clocks for write byte to bus
				z80Ports[port] = (byte)value;
			}

		};
		NotifyOps _not = new NotifyOps(){
		
			@Override
			public void execDone() {
				// TODO Auto-generated method stub
				System.out.println("Exec Done");
			}
		
			@Override
			public int breakpoint(int address, int opcode) {
				// TODO Auto-generated method stub
				System.out.println("breakpoint("+address+", "+ opcode +")");
				return 0;
			}
		};
		this.z80 = new Z80( _mem, _not ) {};
	}

	void setStatus(int status) {
		System.out.println("CPU.setStatus("+ status +")");
		// z80.state_HALT = false;
	}

	int getStatus() {
		System.out.println("CPU.getStatus()");
		return 0;
	}

	int getPCX() {
		return z80.getRegPC();
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

	Register AF = new Register()  /* AF register                                  */
	{
		void set(int value) {
			// z80.A = value / 256;
			// z80.F = value % 256;
			z80.setRegAF(value);
		}
		int get() {
			// return ( z80.A * 256 ) + z80.F;
			return z80.getRegAF();
		}
	};
	Register BC = new Register()  /* BC register                                  */
	{
		void set(int value) {
			// z80.B = value / 256;
			// z80.C = value % 256;
			z80.setRegBC(value);
		}
		int get() {
			// return ( z80.B * 256 ) + z80.C;
			return z80.getRegBC();
		}
	};
	Register DE = new Register()  /* DE register                                  */
	{
		void set(int value) {
			// z80.D = value / 256;
			// z80.E = value % 256;
			z80.setRegDE(value);
		}
		int get() {
			// return ( z80.D * 256 ) + z80.E;
			return z80.getRegDE();
		}
	};
	Register HL = new Register()  /* HL register                                  */
	{
		void set(int value) {
			// z80.H = value / 256;
			// z80.L = value % 256;
			z80.setRegHL(value);
		}
		int get() {
			// return ( z80.H * 256 ) + z80.L;
			return z80.getRegHL();
		}
	};

	// useful for Main.java
	Register PC = new Register()  /* program counter                              */
	{
		void set(int value) {
			z80.setRegPC(value);
		}
		int get() {
			return z80.getRegPC();
		}
	};
	
void Z80reset() {
	System.out.println("RESET");
	// z80.DEBUG = true;
	z80.reset();
}


boolean DBUG = !false;

void  Z80run() {
	System.out.println("CPU > run (*)");
	// int cycles = 4 * 1000; // 4MHz
	// while( z80.state_HALT == false ) {
	// 	z80.exec(cycles);
	// 	// System.out.print('.');
	// }

	int i = 0;
	while(true) {
		// System.out.println( i++  + "  "+z80.isHalted() );

		z80.execute();
	}
}

} 