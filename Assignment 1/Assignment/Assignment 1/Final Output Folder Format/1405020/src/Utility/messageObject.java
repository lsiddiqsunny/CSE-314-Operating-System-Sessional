package Utility;

import java.io.Serializable;
import java.util.Vector;

/**
 * Created by Asus on 9/23/2017.
 */
public class messageObject implements Serializable{
    int sender;
    int reciever;
    String message;
    int type;
    int clientId;
    String filename;
    long filesize;
    long fileId;
    int maxchunksize;
    byte [] filebytes;
    Vector<recievedFileInfo> r;

    public Vector<recievedFileInfo> getR() {
        return r;
    }

    public void setR(Vector<recievedFileInfo> r) {
        this.r = r;
    }

    public byte[] getFilebytes() {



        return filebytes;
    }

    public void setFilebytes(byte[] filebytes) {
        this.filebytes = filebytes;
    }

    public int getMaxchunksize() {
        return maxchunksize;
    }

    public void setMaxchunksize(int maxchunksize) {
        this.maxchunksize = maxchunksize;
    }

    public long getFileId() {
        return fileId;
    }

    public void setFileId(long fileId) {
        this.fileId = fileId;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public long getFilesize() {
        return filesize;
    }

    public void setFilesize(long filesize) {
        this.filesize = filesize;
    }

    public messageObject(int type){
        this.type=type;
    }

    public int getClientId() {
        return clientId;
    }

    public void setClientId(int clientId) {
        this.clientId = clientId;
    }

    public int getSender() {
        return sender;
    }

    public void setSender(int sender) {
        this.sender = sender;
    }

    public int getReciever() {
        return reciever;
    }

    public void setReciever(int reciever) {
        this.reciever = reciever;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
