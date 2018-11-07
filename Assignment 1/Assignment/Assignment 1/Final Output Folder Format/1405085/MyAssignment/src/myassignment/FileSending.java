
package myassignment;

import java.io.Serializable;


public class FileSending implements Serializable{
    
   
    public int x;
    String  from;
    String  to;
    
    public int length;    
    public byte[] data;
    
    public FileSending(int l,byte[] dataToBeSent){
     
        length = l;
        data = new byte[l];
        
        System.arraycopy(dataToBeSent, 0, data, 0, length);      
             
    
    }
    
     public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public int getLength() {
        return length;
    }

    public byte[] getData() {
        return data;
    }
    
    
}