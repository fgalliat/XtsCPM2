
public class MEM {

    // from globals.h => not ram.h
    public final int MEMSIZE = (64 * 1024); // RAM + ROM memseg

    public final int TPASIZE = 60; // Can be 60 for CP/M 2.2 compatibility or more, up to 64 for extra memory
    // Values other than 60 or 64 would require rebuilding the CCP
    // For TPASIZE<60 CCP ORG = (SIZEK * 1024) - 0x0C00

    //// Size of the allocated pages (Minimum size = 1 page = 256 bytes)

    // BIOS Pages (always on the top of memory)
    public final int BIOSpage = (MEMSIZE - 256);
    public final int BIOSjmppage = (BIOSpage - 256);

    // BDOS Pages (depend on TPASIZE)
    public final int BDOSpage = (TPASIZE * 1024) - 768;
    public final int BDOSjmppage = (BDOSpage - 256);

    public final int DPBaddr = (BIOSpage + 64); // Address of the Disk Parameters Block (Hardcoded in BIOS)

    public final int SCBaddr = (BDOSpage + 16); // Address of the System Control Block
    public final int tmpFCB = (BDOSpage + 64); // Address of the temporary FCB

    // static uint8 RAM[RAMSIZE]; // Definition of the emulated RAM
    char[] RAM = new char[MEMSIZE];

    // uint8* _RamSysAddr(uint16 address) {
    // return(&RAM[address]);
    // }

    // uint8 _RamRead(uint16 address) {
    char _RamRead(int address) {
        return (RAM[address]);
    }

    int _RamRead16(int address) {
        return ((RAM[(address & 0xffff) + 1] << 8) | RAM[address & 0xffff]);
    }

    // void _RamWrite(uint16 address, uint8 value) {
    void _RamWrite(int address, char value) {
        RAM[address] = value;
    }

    // void _RamWrite16(uint16 address, uint16 value) {
    void _RamWrite16(int address, int value) {
        // Z80 is a "little indian" (8 bit era joke)
        _RamWrite(address, (char) (value & 0xff));
        _RamWrite(address + 1, (char) ((value >> 8) & 0xff));
    }

    // TODO: BEWARE w/ that
    // return a subCOPY of mem
    char[] _RamSysAddr(int address, int len) {
        char[] result = new char[len];
        for(int i=0; i < len; i++) {
            result[i] = RAM[address+i];
        }
    return result;
    }


}