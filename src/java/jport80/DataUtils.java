
public class DataUtils {

	public static int TRUE = 1;
    public static int FALSE = 0;

    public static char int8(int v) { return (char)( v % 256 ); }
    public static int int16(int v) { return ( v % 65536 ); }
    
    public static char HIGH_REGISTER( int v ) { return (char)( v / 256 ); }
    public static char LOW_REGISTER( int v ) { return (char)( v % 256 ); }
    
    public static char HIGH_REGISTER( CPU.Register v ) { return HIGH_REGISTER(v.get()); }
	public static char LOW_REGISTER( CPU.Register v ) { return LOW_REGISTER(v.get()); }

    public static void SET_LOW_REGISTER( CPU.Register a, char v ) {
		a.set( (((a.get())&0xff00) | ((v)&0xff)) );
	}
	public static void SET_HIGH_REGISTER( CPU.Register a, char v ) {
		a.set( (((a.get())&0xff) | (((v)&0xff) << 8)) );
	}
	public static void SET_HIGH_REGISTER( CPU.Register a, int v ) {
		SET_HIGH_REGISTER(a, int8(v) );
	}
	public static void SET_LOW_REGISTER( CPU.Register a, int v ) {
		SET_LOW_REGISTER(a, int8(v) );
	}

	public static char LOW_DIGIT(int x) {
		return (char)((x) & 0xf);
	}

	public static char HIGH_DIGIT(int x) {
		return (char)(((x)>>4)&0xf);
	}

    public static int WORD16(int x)	{ return((x) & 0xffff); }

	//#define tohex(x)	((x) < 10 ? (x) + 48 : (x) + 87)
	public static final char tohex(char x) {
		return (char)( ((x) < 10 ? (x) + 48 : (x) + 87) );
	}

	public static final char toupper(char x) {
		if ( x >= 'a' && x <= 'z' ) {
			x = (char)( (int)x - (int)'a' + (int)'A' );
		}
		return x;
	}

}