
package file.transmission;

import java.util.Vector;


public class Client {
    public int studentId = 0;
    public boolean isOnline = false;
    public Vector<MyChunkedFile> incomingFile = null;
    public Client(int id,boolean b)
    {
        studentId = id;
        isOnline = b;
        incomingFile = new Vector<MyChunkedFile>(); 
        
    }
    
    public void addToIncoming(MyChunkedFile f)
    {
        incomingFile.add(f);
    }
}
