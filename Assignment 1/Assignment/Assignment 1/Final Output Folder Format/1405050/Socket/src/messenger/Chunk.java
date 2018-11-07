package messenger;

import java.io.Serializable;

/**
 *
 * @author TIS
 */
public class Chunk implements Serializable{
    private String fileId;
    private int size;
    private byte[] fileBytes;

    
    public Chunk (int _size, byte[] _fileBytes, String _fileId){
        size = _size;
        fileBytes = _fileBytes;
        fileId = _fileId;
    }
    
    
    public byte[] getFileBytes(){
        return fileBytes;
    }

    public String getFileId() {
        return fileId;
    }

    public int getSize() {
        return size;
    }
}
