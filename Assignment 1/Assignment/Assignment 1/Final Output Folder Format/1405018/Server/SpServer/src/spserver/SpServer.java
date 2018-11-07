/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spserver;

import NetUtil.ConnectionUtillities;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Farhan
 */
public class SpServer 
{

    /**
     * @param args the command line arguments
     */
    
    private static final int maxChunkCapacity=1024*(1024);
    private static int occupiedCapacity=0;
    private static Initialize init;
    
    
    
    public static int getMaxChunkCapacity()
    {
        return maxChunkCapacity;
    }
    
    public static int getOccupiedCapacity()
    {
        return occupiedCapacity;
    }
    
    public static void setOccupiedCapacity(int cap)
    {
        occupiedCapacity=cap;
    }
    
    
    
    public static void Start()
    {
        System.out.println("Server is going to Initialize");
        try {
            init=new Initialize(4321);
            occupiedCapacity=0;
            System.out.println("Server successfully Initialized");
        } catch (IOException ex) {
            Logger.getLogger(SpServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    public static void Terminate()
    {
        ConnectionUtillities con=new ConnectionUtillities("127.0.0.1",4321); 
        con.write("$$$terminating$$$");
        try {
            con.Close();
        } catch (IOException ex) {
            Logger.getLogger(SpServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    
    public static void main(String[] args)
    {
        Start();
        
        
    }
    
}
