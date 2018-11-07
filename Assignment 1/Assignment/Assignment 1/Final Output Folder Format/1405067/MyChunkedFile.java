
package file.transmission;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;


public class MyChunkedFile implements Serializable{
    private int fileId=0;
    private int senderId = 0;
    private int receiverId = 0;
    private String fileName = null;
    public int filesize=0;
    
    private Vector<byte[]> chunks = null;
    public MyChunkedFile(int id,String name,int size,int from,int to ) {
        this.fileId = id;
        this.receiverId = to;
        this.senderId = from;
        this.fileName = name;
        this.filesize = size;
        chunks = new Vector<byte[]>();
    }

    public int getFileId() {
        return fileId;
    }
    public String getFileName()
    {
        return fileName;
    }
    
    public void addChunks(byte[] c) {
        chunks.add(c);
    }  
    public boolean writeFile(String fileName)
    {
        FileOutputStream fos;
        try {
            fos = new FileOutputStream(new File(fileName));
            for(int i=0;i<chunks.size();i++)
            {
                fos.write(chunks.get(i));
                fos.flush();
            }
            fos.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(MyChunkedFile.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        } catch (IOException ex) {
            Logger.getLogger(MyChunkedFile.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }

        return true;
    }
}
