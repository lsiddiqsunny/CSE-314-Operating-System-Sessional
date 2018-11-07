/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

/**
 *
 * @author uesr
 */
public class Writer implements Runnable{
    public ConnectionUtillities connection;
    
    public Writer(ConnectionUtillities con){
        connection=con;
    }

    @Override
    public void run() {
        
        while(true){
             Object o=connection.read();
            System.out.println(o.toString());            
        }
        
    }
    
    
}
