/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package messenger;

import util.ConnectionUtillities;

/**
 *
 * @author uesr
 */
public class Information {
    public ConnectionUtillities connection;
    public String username;
   // public String fileName;
   // public String fileID;
   // public long fsize;
    
    public Information(ConnectionUtillities con,String User){
        username=User;
        connection=con;
    }
  /*  public void setFileName(String str)
    {
        fileName=str;
    }
    public String getFileName()
    {
        return fileName;
    }
    public void setFileID(String str)
    {
        fileID=str;
    }
    public String setFileID()
    {
        return fileID;
    }
    
    public void setFsize(long a)
    {
        fsize=a;
    }
    public long getFsize()
    {
        return fsize;
    } */
}
