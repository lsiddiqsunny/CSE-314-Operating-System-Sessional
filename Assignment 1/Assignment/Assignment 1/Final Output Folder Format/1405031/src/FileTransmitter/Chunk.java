/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package FileTransmitter;

import java.io.Serializable;
import java.util.Arrays;

/**
 *
 * @author saad
 */
public class Chunk  implements Serializable{
    byte[] bytesArray ;
    int fileID ;
    public Chunk(int FileID , byte[] BytesArray)
    {
        fileID = FileID ;
        bytesArray = BytesArray ;
    }
    public void print()
    {
        System.out.println("fileID is : " + fileID );
        System.out.println("v=bytesarray is "+ Arrays.toString(bytesArray));
        
    }
}
