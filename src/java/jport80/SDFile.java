import java.io.File;
import java.io.IOException;

public class SDFile {

    public static int F_WRITE = SD.O_WRITE;

    public static int F_FILE_DIR_DIRTY = 16;

    protected String path;
    protected int m_curPosition;
    protected int flags;
    protected int m_fileSize;

    protected File descr;

    public SDFile(String path, int flags) {
        this.path = path;
        this.descr = new File( this.path );
        this.flags = flags;
        this.m_curPosition = 0;
        this.m_fileSize = exists() ? length() : 0;
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

    int length() {
        return (int)descr.length();
    }

    boolean isFile() {
        return !descr.isDirectory();
    }

    boolean isWrite() {
        return (flags & F_WRITE) == F_WRITE;
    }

    boolean seekSet(int pos) {
        // FIXME : TODO
        return true;
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