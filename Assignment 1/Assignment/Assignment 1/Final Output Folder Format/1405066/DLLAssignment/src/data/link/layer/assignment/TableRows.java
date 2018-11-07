/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data.link.layer.assignment;



public class TableRows{
    
    private String fileId;
    private String fileName;
    private String fileSize;
    private String sender;

    public TableRows(String fileId, String fileName, String fileSize, String sender) {
        this.fileId = fileId;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.sender = sender;
    }

    public String getFileId() {
        return fileId;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFileSize() {
        return fileSize;
    }

    public String getSender() {
        return sender;
    }
    
    
    
    
 
}
