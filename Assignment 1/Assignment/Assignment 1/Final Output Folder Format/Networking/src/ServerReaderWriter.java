/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


import java.util.HashMap;
import util.ConnectionUtillities;

/**
 *
 * @author uesr
 */
public class ServerReaderWriter implements Runnable{

    public HashMap<String,Information> clientList;
    public ConnectionUtillities connection;
    public String user;
    long buffersize=99999;
    
    public ServerReaderWriter(String username,ConnectionUtillities con, HashMap<String,Information> list){
        connection=con;
        clientList=list;
        user=username;
    }
    
    @Override
    public void run() {
        while(true){
            
            
              Object r=connection.read();
              String recieverid=r.toString();
           //   System.out.println(recieverid);
              
              
              if(clientList.containsKey(recieverid)){
              
                  connection.write("Enter Filename");    
              
              
                  
              Object o=connection.read();
              String data=o.toString();
              System.out.println(data);
              
              Information info=clientList.get(recieverid);
              info.connection.write(data);
            
            
            
//              String msg[]=data.split(":",2);
//           
//              String username=msg[0];
//              String msgInfo=msg[1];
//            
//            if(clientList.containsKey(username)){
//                Information info=clientList.get(username);
//                info.connection.write(user+" - "+msgInfo);
//            }
//           else{
//               connection.write(username+" not found ");
//            }
                  
                  
              
              }
              else{
                  connection.write("recieverid not found ");
              }
              
              
            

            
        }
    }
    
    
}
