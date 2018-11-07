/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package messenger;

import java.io.IOException;
import java.util.HashMap;

/**
 *
 * @author TIS
 */
public class CreateClientConnection implements Runnable{
    public HashMap<String,Information> clientList;
    public ConnectionUtillities connection;

    public CreateClientConnection(HashMap<String,Information> list, ConnectionUtillities con){
        clientList=list;
        connection=con;
    }
    
    @Override
    public void run() {
        Object obj= null;
        try {
            obj = connection.read();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        String username=obj.toString();        
        
        //checking existing user name
        
        if(!clientList.containsKey(obj.toString())){
            Information inf = new Information(connection, username); 
            ServerIO s_IO = new ServerIO(username,connection, clientList);
            
            clientList.put(username,inf );
            System.out.println(username+" is online");
            
            new Thread(s_IO).start();
        
            
        }
        else {
            
            System.out.println("User: "+username+" Already logged in ");
           
        }
        
        
    }
    
    
    
}
