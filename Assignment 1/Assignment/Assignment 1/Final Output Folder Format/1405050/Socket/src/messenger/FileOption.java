/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package messenger;

import java.io.File;

/**
 *
 * @author TIS
 */
public class FileOption {
    
    public File file; 
    public String fileName;
    public long fileSize;
    public String fileId; 
    
    public FileOption(){
        
    }
    public FileOption(String receiver,String sender, String directory){
        file = new File(directory);
        fileName= file.getName() ; 
        fileSize = file.length() ;
        fileId = receiver+"#" + sender + "#" + file.getName() + "#" + file.length();
    }
    
    
    
}
