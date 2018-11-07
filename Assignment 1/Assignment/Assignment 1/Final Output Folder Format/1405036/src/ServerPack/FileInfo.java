/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ServerPack;

import java.io.File;

/**
 *
 * @author USER
 */
public class FileInfo {
    
    public int fileId=-1;
    public String sender=null;
    public String receiver=null;
    public String fileName=null;
    public int fileSize=0;
    public File newFile;
    
    
    public String des=null;

    public FileInfo() {}
    
    public FileInfo(int f,String fn,int fs,String s,String r)
    {
        fileId=f;
        fileName=fn;
        fileSize=fs;
        sender=s;
        receiver=r;
        newFile=new File(""+fileId+"."+getExtension(fn));
    }
    
    private String getExtension(String fName) 
    {
        String extension = "";

        int i = fName.lastIndexOf('.');
        if (i > 0) {
            extension = fName.substring(i + 1);
        }
        return extension;
    }
    
}
