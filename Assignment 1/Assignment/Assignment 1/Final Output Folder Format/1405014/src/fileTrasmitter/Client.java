/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fileTrasmitter;

import transmissionUtilities.ConnectionUtilities;

import java.util.Scanner;

public class Client {

    public static void main (String[] args) {

        final ConnectionUtilities connection = new ConnectionUtilities ("127.0.0.1", 22222);

        System.out.println ("New client joined the network.");
        System.out.println ("Enter your username : ");

        Scanner in = new Scanner (System.in);
        String username = in.nextLine ();
        while (!username.contains("1405")) {
            System.out.println ("Invalid username. Please try again.");
            username = in.nextLine ();
        }

        synchronized (connection) {
            connection.writeString (username);
        }
        String information = connection.readString ();

        if (information.equals ("Doesn't exist")) {
            String serverMessage;
            synchronized (connection) {
                serverMessage = connection.readString ();
            }
            System.out.println (serverMessage);
            new Thread (new ClientReaderWriter (connection)).start ();
        } else if (information.equals ("Exists")) {
            System.out.println (username + " is already logged in to the server");
        }

    }

}
