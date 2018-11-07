/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package serverside;

public class FileInfo {
    
    private long file_size;
    private int sender_id;
    private int rcvr_id;
    
    FileInfo(long file_size,int sender_id,int rcvr_id)
    {
        this.file_size = file_size;
        this.sender_id = sender_id;
        this.rcvr_id = rcvr_id;
    }

    long get_file_size()
    {
        return file_size;
    }
    int get_sender_id()
    {
        return sender_id;
    }
    int get_rcvr_id()
    {
        return rcvr_id;
    }
}
