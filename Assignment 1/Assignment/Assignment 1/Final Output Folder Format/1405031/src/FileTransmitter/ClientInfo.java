/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package FileTransmitter;


/**
 *
 * @author uesr
 */
public class ClientInfo {
    public ConnectionUtillities connection;
    public String username;
    
    public ClientInfo(ConnectionUtillities con,String User){
        username=User;
        connection=con;
    }
}
