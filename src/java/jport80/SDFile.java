import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/*
https://github.com/greiman/SdFat/blob/master/src/FatLib/FatFile.cpp
*/


public class SDFile {

    public static final int F_WRITE = SD.O_WRITE;
    public static final int F_APPEND = SD.O_APPEND;
    public static final int F_CREAT = SD.O_CREAT;

    public static final int F_FILE_DIR_DIRTY = 16;

    protected String path;
    protected int m_curPosition;
    protected int m_flags;
    protected int m_fileSize;

    protected File descr;

    public SDFile(String path, int flags) {
        this.path = path;
        this.descr = new File( this.path );
        this.m_flags = flags;
        if ( (this.m_flags & F_CREAT) == F_CREAT ) {
          createNewFile();
        }
        this.m_curPosition = 0;
        this.m_fileSize = exists() ? length() : 0;
        if ( (this.m_flags & F_APPEND) == F_APPEND ) {
          this.m_curPosition = this.m_fileSize;
        }
    }

    // for CPM use
    // if write() -> do not modify till close() ... !?
    int size() {
      // not certified
      if ( (m_flags & F_FILE_DIR_DIRTY) == F_FILE_DIR_DIRTY ) {
        m_fileSize = length();
      }
      return m_fileSize;
    }

    int available() {
      return size() - m_curPosition;
    }



    // FIXME : very slow !!!!
    char write(char c) {
      try {
        int fileSize = size();
        if ( m_curPosition >= fileSize ) {
          // extendFile case
          // FIXME : works only if fpos = size+1
          OutputStream fout = new FileOutputStream(descr, true);
          fout.write( c );
          fout.flush();
          fout.close();
          m_curPosition++;

          return 1;
        }

        InputStream in = new FileInputStream(descr);
        byte[] buff = new byte[fileSize];
        int read = in.read(buff, 0, fileSize);
        in.close();
        
        if ( m_curPosition <= fileSize ) {
          buff[ m_curPosition ] = (byte)c;
          m_curPosition++;
        } else {
          return 0;
        }

        OutputStream fout = new FileOutputStream(descr, false);
        fout.write( buff );
        fout.flush();
        fout.close();
        // to be certified
        return (char)1;
      } catch(Exception ex) {
        return 0;
      }
    }

    // file should be extend before calling that Op
    int write(char[] data, int len) {
      if ( m_curPosition + len > size() ) {
        throw new RuntimeException("Oups : Ur file should have been extended before write dataSeg");
      }
      try {
        int fileSize = size();
        InputStream in = new FileInputStream(descr);
        byte[] buff = new byte[fileSize];
        int read = in.read(buff, 0, fileSize);
        in.close();
      
        for(int i=0; i < len; i++) {
          buff[m_curPosition] = (byte)data[i];
          m_curPosition++;
        }

        OutputStream fout = new FileOutputStream(descr, false);
        fout.write( buff );
        fout.flush();
        fout.close();
        // to be certified
        return (char)1;

      } catch(Exception ex) {
        ex.printStackTrace();
        return 0;
      }
    }


    void close() {
      m_fileSize = length();
      m_curPosition = 0;
    }


    boolean exists() {
        return descr.exists();
    }

    boolean createNewFile() {
        try {
            boolean ok = descr.createNewFile();
            if ( !ok ) { return false; }
            m_curPosition = 0;
            m_fileSize = 0;
            return true;
        } catch(IOException ex) {
            return false;
        }
    }

    private int length() {
        return (int)descr.length();
    }

    boolean isFile() {
      return !descr.isDirectory();
    }

    boolean isDirectory() {
      return descr.isDirectory();
    }

    boolean isWrite() {
        return (m_flags & F_WRITE) == F_WRITE;
    }

    int min(int a, int b) { return DataUtils.min(a, b); }

    void getName(charP dest, int maxLen) {
      int len = min( maxLen, descr.getName().length() );
      // FIXME : to upperCase ??
      String fn = descr.getName();
      int i=0;
      for(; i < len; i++) {
        dest.set(fn.charAt(i));
        dest.inc();
      }
      // not certified (Cf 0x00 Vs '$'/'?')
      for(; i < maxLen; i++) {
        dest.set( (char)0x00);
        dest.inc();
      }
    }

    boolean seekSet(int pos) {
        if ( pos > m_fileSize ) {
          return false;
        }
        m_curPosition = pos;
        return true;
    }

    boolean seek(long pos) {
        // is it alias for seekSet or seekAdd

        // return seekSet(m_curPosition + (int)pos);
        return seekSet((int)pos);
    }

    int read(char[] dest, int maxLen) {
      try {
        System.out.println("DBG: File '"+descr.getName()+"' @"+m_curPosition);
        InputStream in = new FileInputStream(descr);
        byte[] buff = new byte[maxLen];
        in.skip(m_curPosition);
        int read = in.read(buff, 0, maxLen);
        m_curPosition += read;
        in.close();
        for(int i=0; i < read; i++) {
          dest[i] = (char)(buff[i] < 0 ? 255+(int)buff[i] : (int)buff[i] );
        }
        return read;
      } catch(Exception ex) {
        ex.printStackTrace();
        return 0;
      }
    }

    int readBytes(char[] dest, int maxLen) {
      return read(dest, maxLen);
    }

    boolean truncate(int length) {
        if ( !exists() ) {
            if ( ! createNewFile() ) {
                return false;
            }
        }

        int newPos;
        // error if not a normal file or read-only
        if (!isFile() || !isWrite()) {
          return false;
        }
        // error if length is greater than current size
        if (length > m_fileSize) {
          return false;
        }
        // fileSize and length are zero - nothing to do
        if (m_fileSize == 0) {
          return true;
        }
      
        // remember position for seek after truncation
        newPos = m_curPosition > length ? length : m_curPosition;
      
        // position to last cluster in truncated file
        if (!seekSet(length)) {
          return(false);
        }

        // =======================================
try {
        InputStream in = new FileInputStream(descr);
        byte[] contentKept = new byte[length];
        int read = in.read(contentKept, 0, length);
        in.close();

        OutputStream fout = new FileOutputStream(descr, false);
        fout.write( contentKept );
        fout.flush();
        fout.close();
} catch(Exception ex) {
  ex.printStackTrace();
  return false;
}        
        // =======================================

        // if (length == 0) {
        //   // free all clusters
        //   if (!m_vol->freeChain(m_firstCluster)) {
        //     return false;
        //   }
        //   m_firstCluster = 0;
        // } else {
        //   uint32_t toFree;
        //   int8_t fg = m_vol->fatGet(m_curCluster, &toFree);
        //   if (fg < 0) {
        //     return false;
        //   }
        //   if (fg) {
        //     // free extra clusters
        //     if (!m_vol->freeChain(toFree)) {
        //       return false;
        //     }
        //     // current cluster is end of chain
        //     if (!m_vol->fatPutEOC(m_curCluster)) {
        //       return false;
        //     }
        //   }
        // }
        m_fileSize = length;
      
        // // need to update directory entry
        // m_flags |= F_FILE_DIR_DIRTY;
      
        // if (!sync()) {
        //   return false;
        // }
        // set file to correct position
        return seekSet(newPos);
      
        // return false;
      }




}