package file.transmission;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;


public class FileChunk implements Serializable{
    
    
    private static final long serialVersionUID = 1L;
    public int fileId = 0;
    public int size=0;
    public byte[] chunkByte;
    public FileChunk(int size)
    {
        this.size=size;
        chunkByte = new byte[size];
        
    }
    
    boolean readChunkFromFile(FileInputStream fis)
    {
        int read = 0;
        int numRead = 0;
        try {
            while (read < size && (numRead = fis.read(chunkByte,read,size - read)) >= 0)
            {
                read = read + numRead;
            }
        } catch (IOException ex) {
            Logger.getLogger(FileChunk.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        return true;
    }
      
}
