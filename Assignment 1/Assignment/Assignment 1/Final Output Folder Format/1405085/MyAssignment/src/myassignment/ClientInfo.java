/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package myassignment;

import java.io.Serializable;


public class ClientInfo implements Serializable {

    public String id;

    public ClientInfo(String x) {
        id = x;
    }
}