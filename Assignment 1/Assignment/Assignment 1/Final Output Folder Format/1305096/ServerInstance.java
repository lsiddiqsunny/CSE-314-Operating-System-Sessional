package com.company;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;


public class ServerInstance {
    ArrayList<String> iplist=new ArrayList<String>();
    ArrayList<String> chunks=new ArrayList<String>();
    ArrayList<String> receiverList=new ArrayList<String>();
    ArrayList<String> senderList= new ArrayList<String>();
    ArrayList<String> fileList= new ArrayList<String>();
    String[][] chunkList=new String[10][];
    int [] chunkListLen=new int[10];
    int sendIt[]=new int[10];


    String name;
    ObjectInputStream ois;
    ObjectOutputStream oos;
    String ip;
    int rPort;
    int chunkSize;
    String fileName;
    long fileSize;
    long maxBufferSize=1000000000;
    long curBufferSize=0;
    static long files=0;
    int flag=0;
    ServerInstance(){
        for(int i=0;i<10;i++) {
            sendIt[i] = 0;
            chunkList[i]=new String[1000];
            chunkListLen[i]=0;
        }

    }
}
