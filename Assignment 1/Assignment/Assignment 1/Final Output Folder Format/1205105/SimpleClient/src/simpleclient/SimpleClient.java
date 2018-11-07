/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package simpleclient;

import java.io.*;
import java.net.*;
import java.util.*;

public class SimpleClient
{
	
	
	public static void main(String args[])
	{
            CL application;
        
          application = new CL(); // connect to localhost
         // use args to connect   

        //application.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        application.runClient();
	}
	
	
}
