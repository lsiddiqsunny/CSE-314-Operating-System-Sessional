/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

/**
 *
 * @author shariar076
 */
public class Files {
    String fileName;
    String receiver;
    int startInd;
    long fileSize;

    public Files(String fileName, String reciver, int startInd, long fileSize) {
        this.fileName = fileName;
        this.receiver = reciver;
        this.startInd = startInd;
        this.fileSize = fileSize;
    }
    
    
}
