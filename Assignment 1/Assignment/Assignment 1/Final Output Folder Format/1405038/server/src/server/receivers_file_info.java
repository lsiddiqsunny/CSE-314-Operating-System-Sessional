/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.util.LinkedList;
import java.util.Queue;

/**
 *
 * @author User
 */
public class receivers_file_info {
    
    public Queue<singleinfo>queue;//=new Queue<singleinfo>();
    public receivers_file_info(){
     queue=new LinkedList<singleinfo>();
    }
    public void add_file(String ke_pathaise,int file_id){
        
        queue.add(new singleinfo(ke_pathaise,file_id));
    }
    public boolean is_empty(){
        return queue.isEmpty();
    }
    
}
class singleinfo{
    public String ke_pathaise;
    public int file_id;
    public singleinfo(String ke_pathaise,int file_id){
        this.ke_pathaise=ke_pathaise;
        this.file_id=file_id;
    }
}
