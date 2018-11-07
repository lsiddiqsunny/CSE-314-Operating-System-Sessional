/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package util;



import clientSide.Sender;

import java.io.Serializable;

/**
 *
 * @author Antu
 */
public class Information implements Serializable{
    public ConnectionUtilities senderConnection,receiverConnection;
    public String studentID;
    public long fileSize; //make it long
    public String fileName;
    public Sender clientSender;
//    public Receiver receiver;
    
    public Information(ConnectionUtilities con, ConnectionUtilities receiverConnection,
                       String studentID, Sender clientSender){

        this.studentID=studentID;
        senderConnection = con;
        this.receiverConnection = receiverConnection;
        this.clientSender = clientSender;
//        this.receiver = receiver;
    }
    public void setFileName(String name){
        this.fileName = name;
    }

    public void setStudentID(String studentID) {
        this.studentID = studentID;
    }

    public String getStudentID() {
        return studentID;
    }

    public long getFileSize() {
        return fileSize;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }
}
