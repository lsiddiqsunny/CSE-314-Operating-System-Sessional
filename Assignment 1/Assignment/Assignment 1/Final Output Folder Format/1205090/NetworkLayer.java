/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Abdullah Al Maruf
 */
package dlloffline;

import java.util.*;
import java.net.*;
import java.io.*;

//+++++++++++++++++Class: NetworkLayer+++++++++++++++++++++++++++++++++
class NetworkLayer extends Thread {

    public DataLinkLayer dll;
    public Buffer<Packet> packetBuffer;
    BufferedReader br;

	boolean enable;
	
    int totalfilesize = 0;
	byte[] totalFile = null;
	
    //=======================================================

    public static void main(String args[]) throws Exception {
        int port = 0;
        int timeOut = 5;

        int argCount = args.length;
        if (argCount > 0) {
            port = Integer.parseInt(args[0]);
        }
        if (argCount > 1) {
            timeOut = Integer.parseInt(args[1]);
        }


        NetworkLayer networkLayer = new NetworkLayer(port, timeOut);
        networkLayer.join();
    }
    //=======================================================

    public NetworkLayer(int swPort, int timeOut) {
        br = new BufferedReader(new InputStreamReader(System.in));
        dll = new DataLinkLayer(timeOut, this);
        packetBuffer = new Buffer<Packet>("NL Packet", 1);
        enable = false;
        start();
    }
    //=======================================================

    public void run() {
        try {
            while (true) {
                String input = br.readLine();

                int gt = input.indexOf(">");
                String Type = input.substring(0, gt);

                if (Type.equals("msg")) {


                    int colon = input.indexOf(':');
                    int numOfTimes = Integer.parseInt(input.substring(gt + 1, colon));
                    String text = input.substring(colon + 1);
                    for (int i = 0; i < numOfTimes; i++) {
                        String temp = new String(text + "-" + i);
                        Packet p = new Packet(temp.getBytes());
                        storePacket(p);
                        while (true) {
                            if (getEnable() == true) {
                                break;
                            }
                        }
                        dll.addNetworkLayerReadyEvent();
                    }
                }
                if (Type.equals("file")) {
                    String filename = input.substring(gt + 1);
                    File file = new File("D:\\CSE 322\\Assignment 2\\DLL\\src\\Shared\\" + filename);
                    int filesize = (int)file.length();

                    FileInputStream in=new FileInputStream(file);
				    byte[] b=new byte[filesize];
				    in.read(b);

                    Packet pak = null;
                    int lastpos;
                    for (lastpos = 511; lastpos < b.length; lastpos = lastpos + 512) {
                        byte[] a = new byte[512];
                        System.arraycopy(b, lastpos - 512 + 1, a,0 , 512);
                        pak = new Packet(a, 1, file, lastpos - 512 + 1, 512);

                        storePacket(pak);
                        while (true) {
                            if (getEnable() == true) {
                                break;
                            }
                        }
                        dll.addNetworkLayerReadyEvent();

                    }

                    if((b.length % 512) != 0)
                    {
                        byte[] a = new byte[b.length-lastpos+512-1];
                        System.arraycopy(b, lastpos-512+1, a,0 ,b.length-lastpos+512-1);
                        pak = new Packet(a, 1, file, lastpos-512+1,b.length-lastpos+512-1);
                    }


                    storePacket(pak);
                    while (true) {
                        if (getEnable() == true) {
                            break;
                        }
                    }
                    dll.addNetworkLayerReadyEvent();



                }
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //=================Call From others====================

    Packet getPacket() {//from DLL
        Packet p = null;
        try {
            synchronized (packetBuffer) {
                p = packetBuffer.get();
                enable = false; //do not allow to create next event
                packetBuffer.notify();
            }
        } catch (Exception e) {
        }
        return p;
    }
    //======================================================

    Packet storePacket(Packet p) {//from DLL
        try {
            synchronized (packetBuffer) {
                if (packetBuffer.full()) {
                    packetBuffer.wait();
                }
                packetBuffer.store(p);
                //packetBuffer.notify();
            }
        } catch (Exception e) {
        }
        return p;
    }
    //======================================================

    synchronized boolean getEnable() {
        return enable;
    }
    //======================================================

    synchronized void setEnable() {
        enable = true;
    }
    //======================================================

    synchronized void disable() {
        enable = false;
    }
    //======================================================

    public void to_network_layer(byte[] b) {
        System.out.println("Showing Packet: " + new String(b) + "\n");
    }

    public void to_network_layer(Packet b)
    {
	    byte[] packetByteArray = b.getBytes();
	    
	    byte[] infoType = new byte[3];
	    System.arraycopy(packetByteArray, 4, infoType, 0, 3);	// infoType determines whether msg or file
	    String infoString = new String(infoType);
	    
	    byte[] restOfContent = new byte[packetByteArray.length-4];
        System.arraycopy(packetByteArray, 4, restOfContent, 0, packetByteArray.length-4);	//restOfContent is header+data
	    
	    
	    if(infoString.equals("msg"))
	    {
            System.out.println("\tMessage Received: "+" "+new String(restOfContent)+"\n");
	    }
        else
        {

            String fileContent = new String(restOfContent);
            
            int trackLength = 0;
            String token = null;
            
            StringTokenizer tokenizer = new StringTokenizer(fileContent, " ");
            token = tokenizer.nextToken();	//gets 'file'
            trackLength += token.length();
            
            if(token.equals("file"))
            {
                String fileName = tokenizer.nextToken();	 //gets 'fileName'
                trackLength += fileName.length();

                token = tokenizer.nextToken();	//gets fileSize
                trackLength += token.length();
                int fileSize = Integer.parseInt(token);
                
                if(totalfilesize == 0)	//global
                {	
                    totalFile = new byte[fileSize];
                }

                token = tokenizer.nextToken();	//gets startByte
                trackLength += token.length();
                int startByte = Integer.parseInt(token);
                trackLength += 4; // cause startByte is an integer
                
                
                int chunkSize = 512;
                int remain = fileSize % 512;
                
                if(fileSize == 0)
                {
                    chunkSize = 0;
                    trackLength++; // to write 0
                }
                else if(remain == 0)
                {
                    trackLength += 3; //to write 512
                }
                else if(remain<10)
                {
                    trackLength ++; //1 digit
                }
                else if(remain<100)
                {
                	trackLength += 2; //2 digit
                }
                else
                {
                	trackLength += 3; //less than 512, but still 3 digit
                }


                byte[] fileContentByteArray = (fileContent.substring(trackLength)).getBytes();

                totalfilesize += chunkSize;
                
                if(totalfilesize > fileSize)
                {
                    chunkSize = remain;
                    totalfilesize = fileSize;
                }


                for(int i=startByte; i<startByte+chunkSize; i++)
                {
                    totalFile[i] = fileContentByteArray[i-startByte];
                }


                if(totalfilesize == fileSize)
                {
                    totalfilesize = 0;
                    System.out.println("File Sent Successfully!\nSaving file as: reply_"+fileName);
                    try
                    {
                        File file = new File("D:\\CSE 322\\Assignment 2\\DLL\\src\\Shared\\reply_"+fileName);
                        FileOutputStream fos = new FileOutputStream(file);
                        fos.write(totalFile);
                    }
                    catch(Exception e)
                    {
                    	System.out.println("Write Error");
                    	e.printStackTrace();

                    }

                }

            }

        }

	}
}

