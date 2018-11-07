/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package networking;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author uesr
 */
public class Networking {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            InetAddress ipAddress=InetAddress.getByName("www.buet.ac.bd");
            System.out.println(ipAddress.getHostAddress());
            
            byte address[]=new byte[4];
            address[0]=(byte)192;
            address[1]=(byte)168;
            address[2]=(byte)44;
            address[3]=(byte)118;
            
            InetAddress checkIp=InetAddress.getByAddress(address);
            try {
                System.out.println(checkIp.isReachable(10000));
            } catch (IOException ex) {
                Logger.getLogger(Networking.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            
        } catch (UnknownHostException ex) {
            Logger.getLogger(Networking.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
