package ServerPackage;

import Utility.clientUtility;

import java.util.HashMap;
import java.util.Vector;

/**
 * Created by Asus on 9/23/2017.
 */
public class ServerInfo {
    private HashMap<Integer, clientUtility> clientUtilMap;
    private HashMap<Integer, String> clientIPMap;
    private HashMap<Long, FileInfo> fileMap;
    private HashMap<Integer,Vector<Long>> recievedFiles;

    ServerInfo(){
        clientUtilMap = new HashMap<>();
        clientIPMap = new HashMap<>();
        fileMap = new HashMap<>();
        recievedFiles = new HashMap<>();
    }

    public synchronized clientUtility getClientUtility(Integer clientId){
        return clientUtilMap.get(clientId);
    }
    public synchronized void putClientUtility(Integer clientId, clientUtility c){

        clientUtilMap.put(clientId,c);
        if(recievedFiles.get(clientId)==null) {
            Vector<Long> v = new Vector<>();
            recievedFiles.put(clientId,v);
        }
    }

    public synchronized void putClientIP(Integer clientId, String IP){
        clientIPMap.put(clientId,IP);
    }
    public synchronized boolean hasIP(Integer clientId){
        return clientIPMap.containsKey(clientId);
    }

    public synchronized void putFileInfo(Long fileId, FileInfo f){
        fileMap.put(fileId,f);
    }

    public synchronized FileInfo getFileInfo(Long fileId){
        return fileMap.get(fileId);
    }


    public void putRecievedFileInfo(int recieverId,Long fileId){
        Vector<Long> v = recievedFiles.get(recieverId);
        v.addElement(fileId);
    }

    public Vector<Long> getRecievedFileInfo(int recieverId){
        Vector<Long> v = recievedFiles.get(recieverId);
        return v;
    }

    public void removeRecievedFileInfo(int recieverId,Long fileId){
        Vector<Long> v = recievedFiles.get(recieverId);
        v.removeElement(fileId);
    }



    public synchronized void removeClient(Integer clientId){
        try {
            clientUtilMap.remove(clientId);
            clientIPMap.remove(clientId);

        } catch (Exception e){
        }
    }

    public synchronized void removeFileInfo(Long fileId){
        try {
            fileMap.remove(fileId);

        } catch (Exception e){

        }
    }
}
