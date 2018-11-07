/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.net.Socket;

/**
 *
 * @author Shariar Kabir
 */
public class User {
    String userID;
    Socket socket;

    public User(String userID, Socket socket) {
        this.userID = userID;
        this.socket = socket;
    }

}
