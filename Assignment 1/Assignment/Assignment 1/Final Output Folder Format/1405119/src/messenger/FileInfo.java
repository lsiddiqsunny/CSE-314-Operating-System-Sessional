package messenger;

import java.io.File;


public class FileInfo {
 
    public String sender;
    public String receiver;
    public long fsize;
    public String fileName;
    public File ff ;
    
    public FileInfo(String sen, String rec, long f, File fname)
    {
        sender=sen;
        receiver=rec;
        fsize=f;
        ff=fname;
    }
    public File getFile()
    {
        return ff;
    }
}
