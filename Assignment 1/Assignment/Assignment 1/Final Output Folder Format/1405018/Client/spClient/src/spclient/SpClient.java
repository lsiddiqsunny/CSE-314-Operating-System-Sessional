/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spclient;

import NetUtil.ConnectionUtillities;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

/**
 *
 * @author Farhan
 */
public class SpClient 
{
    
    public static String ID;
    public static ConnectionUtillities senderConnection;
    public static ConnectionUtillities receiverConnection;
    public static int port;
    public static String tcpServerAddress;
    
    public static boolean sendLock=false;
    
    
    public static boolean Initialize(String address,int port,String clientID)
    {
        SpClient.tcpServerAddress=address;
        SpClient.port=port;
        ID=clientID;
        senderConnection=new ConnectionUtillities(tcpServerAddress,port);
        senderConnection.write(clientID);
        senderConnection.write("sender");
        String verdict=senderConnection.read().toString();
        receiverConnection=new ConnectionUtillities(tcpServerAddress,port);
        receiverConnection.write(clientID);
        receiverConnection.write("receiver");
        Receiver receiver=new Receiver(receiverConnection);
        return verdict.equals("loginok");
        
    }
    
    
    public static boolean LogIn() throws FileNotFoundException, IOException    
    {
        Scanner scanner=new Scanner(System.in);
        System.out.println("Enter User ID:");
        ID=scanner.nextLine();
        System.out.println("Enter Password:");
        String password=scanner.nextLine();
        
        if(ID.split(" ").length>1||password.split(" ").length>1)    return false;
        
        if(ID.equals("")||password.equals(""))    return false;
        
        FileReader fr;
        fr = new FileReader("src/spclient/loginInfo.txt");
        BufferedReader br; 
        br = new BufferedReader(fr);
        
        while(true)    {
            String line=br.readLine();
            if(line==null)    {
                break;
            }
            
            String[] up;
            up = line.split(" ");
            if(up[0].equals(ID) && up[1].equals(password))    {
                br.close();
                fr.close();
                
                if(!Initialize("127.0.0.1",4321,ID))    {
                    System.out.println("Already logged in from another IP!");
                    return false;
                }
                else    return true;
            }
        }
        br.close();
        fr.close();
        
        return false;
    }
    
    
    
    
    
    
    public static void SendFile() throws FileNotFoundException, IOException
    {
        if(sendLock)    {
            System.out.println("Already sending some other file!\nTry later.");
            return;
        }    
        
        System.out.println("Enter File Location:");
        Scanner scanner=new Scanner(System.in);
        String location=scanner.nextLine();
        File file=new File(location);
        if(!file.exists())    {
            System.out.println("File does not exist!");
            return;
        }
        System.out.println("Enter recipient Id:");
        String recipientID=scanner.nextLine();
        Sender sender=new Sender(senderConnection,recipientID,location);
         
    }
    
    
    public static void Terminate() throws IOException
    {
        senderConnection.Close();
        Sender.die=true;
        receiverConnection.Close();
        Receiver.die=true;
    }
    
    
    
    
    public static void main(String[] args) throws IOException, ClassNotFoundException
    {
        Scanner scanner=new Scanner(System.in);
        
        System.out.println("Client Started!");
        while(!LogIn())    {
            System.out.println("Retry : Enter 1\nTerminate : Enter 0");
            int opt=scanner.nextInt();
            if(opt==0)    return;
        }
        
        while(true)    {
            System.out.println("Send File : Press S");
            System.out.println("Terminate : Press T");
            String cmd=scanner.nextLine();
            
            switch (cmd)    {
                case "S":
                    SendFile();
                    break;
                case "R":
                    Receiver.receiveCmd=1;
                    break;
                case "I":
                    Receiver.receiveCmd=2;
                    break;
                case "T":
                    Terminate();
                    return;
                    
                default:
                    break;
            }
        }
        
        
    }
    
}
