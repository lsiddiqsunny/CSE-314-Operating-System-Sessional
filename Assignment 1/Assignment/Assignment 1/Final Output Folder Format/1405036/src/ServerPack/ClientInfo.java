/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ServerPack;

import DataPack.ConnectionUtilities;
import java.net.Socket;

/**
 *
 * @author USER
 */
public class ClientInfo {
    
    public String ip=null;
    public Integer port=null;
    public ConnectionUtilities client=null;
    
    public ClientInfo()
    {}

    
    public ClientInfo(String i,int p,ConnectionUtilities sock)
    {
        ip=i;
        port=p;
        client=sock;
    }
    
    @Override
    public String toString()
    {
        return ip+","+port.toString();
    }
    
    
}
