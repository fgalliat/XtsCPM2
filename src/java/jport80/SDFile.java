import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/*
https://github.com/greiman/SdFat/blob/master/src/FatLib/FatFile.cpp
*/


public class SDFile {

    public static final int F_WRITE = SD.O_WRITE;
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
    }

    boolean exists() {
        return descr.exists();
    }

    void close() {
        // .....
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

    int length() {
        return (int)descr.length();
    }

    boolean isFile() {
        return !descr.isDirectory();
    }

    boolean isWrite() {
        return (m_flags & F_WRITE) == F_WRITE;
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
        InputStream in = new FileInputStream(descr);
        byte[] buff = new byte[maxLen];
        in.skip(m_curPosition);
        int read = in.read(buff, 0, maxLen);
        in.close();
        for(int i=0; i < read; i++) {
          dest[i] = (char)(buff[i] < 0 ? 255+buff[i] : buff[i] );
        }
        return read;
      } catch(Exception ex) {
        ex.printStackTrace();
        return 0;
      }
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
        if (length == 0) {
          // free all clusters
          if (!m_vol->freeChain(m_firstCluster)) {
            return false;
          }
          m_firstCluster = 0;
        } else {
          uint32_t toFree;
          int8_t fg = m_vol->fatGet(m_curCluster, &toFree);
          if (fg < 0) {
            return false;
          }
          if (fg) {
            // free extra clusters
            if (!m_vol->freeChain(toFree)) {
              return false;
            }
            // current cluster is end of chain
            if (!m_vol->fatPutEOC(m_curCluster)) {
              return false;
            }
          }
        }
        m_fileSize = length;
      
        // need to update directory entry
        m_flags |= F_FILE_DIR_DIRTY;
      
        if (!sync()) {
          return false;
        }
        // set file to correct position
        return seekSet(newPos);
      
        // return false;
      }




}