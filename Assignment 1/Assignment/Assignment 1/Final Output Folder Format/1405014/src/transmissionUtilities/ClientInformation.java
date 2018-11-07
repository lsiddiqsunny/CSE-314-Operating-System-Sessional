/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package transmissionUtilities;

import java.util.LinkedList;

public class ClientInformation
{
    public ConnectionUtilities connection;
    private String username;
    public LinkedList <FileInformation> fileLinkedList;
    public boolean isLoggedIn;

    ClientInformation (ConnectionUtilities con, String User)
    {
        username=User;
        connection=con;
        fileLinkedList = new LinkedList<>();
        isLoggedIn = true;
    }

}
