/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DataPack;

import java.io.Serializable;

/**
 *
 * @author USER
 */
public class DataClass implements Serializable{
    public int fileId;
    public byte[] binData;
    public boolean isRead;
    public boolean isTimeOut;
    public String command;
    
    public DataClass()
    {
        fileId=-1;
        binData=null;
        isRead=true;
        command="";
        isTimeOut=false;
    }
    public void setData(String c)
    {
        command=c;
        fileId=-1;
        binData=null;
        isRead=true;
    }
    public void setData(int id,String c)
    {
        fileId=id;
        command=c;
        binData=null;
        isRead=true;
    }
    public void setData(int id,byte[] b)
    {
        fileId=id;
        binData=new byte[b.length];
        binData=b;

        isRead=true;
        command="";
    }
    public void setData(int id,boolean i)
    {
        fileId=id;
        isRead=i;
        binData=null;
        command="";
        
    }
    public void setData(int id,String s,byte[] b)
    {
        fileId=id;
        binData=new byte[b.length];
        binData=b;

        isRead=true;
        command=s;
    }
    @Override
    public String toString()
    {
        return ("File id: "+fileId+" command:"+command);
    }
    
}
