/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SocketProgramming;

import com.sun.glass.events.ViewEvent;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 *
 * @author hasan
 */
public class Server {
    HashMap<String, DataOutputStream>printwriterMap=new HashMap<>();
    HashMap<String,Integer>studentIdMap = new HashMap<>();
    private String filePath;
    private Long totalChunk = 2000L;
    private int fileId = 1;
    public static void main(String[] args) {
        
        new Server().go();
    }
    private void go(){
        
        filePath = "E:\\Course\\L3-T2\\CSE 322\\Server\\";
        
        //System.out.println("filePath print : " + filePath);
        try {
            ServerSocket serverSocket = new ServerSocket(5000);
            while (true) {                
                Socket socket = serverSocket.accept();
                DataInputStream msgRead = new DataInputStream(socket.getInputStream());
                DataOutputStream msgWrite = new DataOutputStream(socket.getOutputStream());
                
                String studentId = msgRead.readUTF();
                
                if(printwriterMap.get(studentId) != null){
                    
                    msgWrite.writeUTF("you can't login");
                    msgWrite.flush();
                    
                    continue;
                }
                else{
                    
                    msgWrite.writeUTF("you can login");
                    msgWrite.flush();
                    printwriterMap.put(studentId, msgWrite);
                }
                
                Thread t = new Thread(new Clienthandler(studentId,socket, msgRead, msgWrite));
                t.start();
                //System.out.println("connected with StudentId : " + studentId );
                
            }
        } catch (Exception e) {
            //System.out.println("1");
        }
    }
    private String getParentDirectory(String file){
            
            int len = file.length();
            len--;
            
                while(file.charAt(len) != '\\') len--;
            
            
            len--;
            int i = 0;
            String files = "";
            while(i <= len){
                files += file.charAt(i);
                i++;
            }
            
            return files;
        }
    
    public class Clienthandler implements Runnable{
        
        private Socket socket;
        private String fileName;
        private DataInputStream msgRead;
        private DataOutputStream msgWrite;
        private DataOutputStream write;
        private DataInputStream read;
        //private BufferedReader reader;
        //private PrintWriter writer;
        //private InputStream din;
        //private OutputStream dout;
        private String studentId;
        private Long initialSize;
        private byte []bytes;
        private byte []deStuffingBuffer;
        private byte []dataWithoutCheckSumBuffer;
        private byte []payloadBuffer;
        private int dataOrAck;
        private int seqNum;
        private int ackNum;
        private int ownFileId;
        int frameNum;
        int avbits = 8;
        int curbyte = 0;
        int counter = 0;
        boolean isFoundHeader = false;
        boolean isFoundTrailer = false;
        byte flag = 0x7E;
        boolean isMissedFrame = false;
        int curSeqNo = 0;
        
        Set<Integer>st;
        //private String filePath;

        public Clienthandler(String st,Socket clientSocket,DataInputStream reader,DataOutputStream writer) {
            socket = clientSocket;
            msgRead = reader;
            msgWrite = writer;
            studentId = st;
            bytes = new byte[500];
            frameNum = 1;
            //System.out.println("filePath vitore " + filePath);
        }

        @Override
        public void run() {
            
            String message;
            try {
                while(true){
                    
                    message = msgRead.readUTF();
                    //if(message.length() <= 40) System.out.println("message while : " + message);
                    //if(message.length()) System.out.println("different kisu ashce");
                    
                    if(message.equals("Initiating sending")){
                        
                    
                        
                        
                        fileName = msgRead.readUTF();
                        initialSize = Long.parseLong(msgRead.readUTF());
                        String id = msgRead.readUTF();
                        DataOutputStream recMsgWrite = printwriterMap.get(id);
                        recMsgWrite.writeUTF("receive file from");
                        //recMsgWrite.flush();
                        recMsgWrite.writeUTF(studentId);
                        //recMsgWrite.flush();
                        Long koitaChunk = (initialSize/(10L*1024));
                        if(initialSize%(10L*1024) > 0) koitaChunk++;
                        
                        Long rest = totalChunk - koitaChunk;
                        if(rest < 0L){
                            msgWrite.writeUTF("you can't send");
                            //msgWrite.flush();
                            
                            continue;
                        }
                        ///////////
                        st = new LinkedHashSet<>();
                        
                        //////////
                        totalChunk -= koitaChunk;
                        //send confirmation msg,fileId,chunkSize
                        
                        
                        if(studentIdMap.get(studentId) != null){
                            ownFileId = studentIdMap.get(studentId);
                        }
                        else{
                            studentIdMap.put(studentId, fileId);
                            ownFileId = fileId;
                            fileId++;
                        }
                        
                        msgWrite.writeUTF("you can send");
                        //msgWrite.flush();
                        msgWrite.writeUTF(Integer.toString(ownFileId));
                        //msgWrite.flush();
                        msgWrite.writeUTF(Integer.toString(10));
                        //msgWrite.flush();
                        
                        
                        
                        
                        
                        
                        try {
                            
                            String filePth = filePath;
                            
                                
                                filePth = filePth +Integer.toString(ownFileId);
                                new File(filePth).mkdir();
                                write = new DataOutputStream(new FileOutputStream(new File(filePth+"\\"+fileName)));
                            
                            

                        } catch (IOException ex) {
                            //System.out.println("4");
                        }
                        
                        
                        
                        msgWrite.writeUTF("send a frame");
                        //msgWrite.flush();
                        

                        
                    }
                    
                    else if(message.equals("give ack")){
                        Iterator it = st.iterator();
                        while(it.hasNext()){
                            msgWrite.writeUTF("acknowledged");
                            msgWrite.writeUTF(Integer.toString((int) it.next()));
                            
                        }
                        st.clear();
                        isMissedFrame = false;
                    }
                    
                    else if(message.equals("sending usual frame")){
                        
                        int len = msgRead.read(bytes);
                        
                        System.out.print("after receiving : ");
                        for(int p = 0; p < len;p++){
                            for(int pp = 7; pp >= 0; pp--){
                                int bit = (1<<pp)&bytes[p];
                                if(bit > 0) bit = 1;else bit = 0;
                                System.out.print(bit);
                            }
                        }
                        System.out.println("");
                        if(isMissedFrame){
                            System.out.println("discarding");
                            continue;
                        }
                        
                        deStuffingBuffer = null;
                        dataWithoutCheckSumBuffer = null;
                        payloadBuffer = null;
                        deStuffingBuffer = new byte[500];
                        dataWithoutCheckSumBuffer = new byte[500];
                        payloadBuffer = new byte[500];
                        dataOrAck = seqNum = ackNum = 0;
                        
                        int lenAfterDestuffing = deStuffing(len);
                        if(lenAfterDestuffing == 0){
                            isMissedFrame = true;
                            System.out.println("discarding");
                            continue;
                        }
                        
                        System.out.print("after destuffing : ");
                        for(int p = 0; p < lenAfterDestuffing;p++){
                            for(int pp = 7; pp >= 0; pp--){
                                int bit = (1<<pp)&deStuffingBuffer[p];
                                if(bit > 0) bit = 1;else bit = 0;
                                System.out.print(bit);
                            }
                        }
                        System.out.println("");
                        //System.out.println("lenAfterDestuffing : " + lenAfterDestuffing);
                        int lenWithoutCheckSum = checkSumCheck(lenAfterDestuffing);
                        if(lenWithoutCheckSum == 0){
                            isMissedFrame = true;
                            System.out.println("discarding");
                            continue;
                        }
                        //System.out.println("lenWithoutCheckSum : " + lenWithoutCheckSum);
                        //check data or ack
                        dataOrAck = checkDataOrAck(lenWithoutCheckSum);
                        
                        if(dataOrAck == -1){
                            isMissedFrame = true;
                            System.out.println("discarding");
                            continue;
                        }
                        //System.out.println("data or ack : " + dataOrAck);
                        //get sequence num
                        seqNum = getSeqNum(lenWithoutCheckSum);
                        if(seqNum == -1){
                            isMissedFrame = true;
                            System.out.println("discarding");
                            continue;
                        }
                        //System.out.println("seq num : " + seqNum);
                        //System.out.println("");
                        //get acknowledge num
                        ackNum = getAckNum(lenWithoutCheckSum);
                        if(ackNum == -1){
                            isMissedFrame = true;
                            System.out.println("discarding");
                            continue;
                        }
                        //get original data or payload
                        st.add(seqNum);
                        int lenOfPayload = getPayload(lenWithoutCheckSum);
                        //if(frameNum <= 1000 || frameNum >=2000){
                            write.write(payloadBuffer,0,lenOfPayload);
                            //write.flush();
                        //}
                        
                        
                        //}
                        ++frameNum;
                        
                        //if(frameNum <= 1000 || frameNum >=2000){
                            //msgWrite.writeUTF("acknowledged");
                            //msgWrite.flush();
                        //}
                        
                    }
                    else if(message.equals("sending last frame")){
                        int len = msgRead.read(bytes);
                        System.out.print("after receiving : ");
                        for(int p = 0; p < len;p++){
                            for(int pp = 7; pp >= 0; pp--){
                                int bit = (1<<pp)&bytes[p];
                                if(bit > 0) bit = 1;else bit = 0;
                                System.out.print(bit);
                            }
                        }
                        System.out.println("");
                        if(isMissedFrame){
                            System.out.println("discarding");
                            continue;
                        }
                        
                        deStuffingBuffer = null;
                        dataWithoutCheckSumBuffer = null;
                        payloadBuffer = null;
                        deStuffingBuffer = new byte[500];
                        dataWithoutCheckSumBuffer = new byte[500];
                        payloadBuffer = new byte[500];
                        dataOrAck = seqNum = ackNum = 0;
                        
                        int lenAfterDestuffing = deStuffing(len);
                        if(lenAfterDestuffing == 0){
                            isMissedFrame = true;
                            System.out.println("discarding");
                            continue;
                        }
                        
                        System.out.print("after destuffing : ");
                        for(int p = 0; p < lenAfterDestuffing;p++){
                            for(int pp = 7; pp >= 0; pp--){
                                int bit = (1<<pp)&deStuffingBuffer[p];
                                if(bit > 0) bit = 1;else bit = 0;
                                System.out.print(bit);
                            }
                        }
                        System.out.println("");
                        //System.out.println("lenAfterDestuffing : " + lenAfterDestuffing);
                        int lenWithoutCheckSum = checkSumCheck(lenAfterDestuffing);
                        if(lenWithoutCheckSum == 0){
                            isMissedFrame = true;
                            System.out.println("discarding");
                            continue;
                        }
                        //System.out.println("lenWithoutCheckSum : " + lenWithoutCheckSum);
                        //check data or ack
                        dataOrAck = checkDataOrAck(lenWithoutCheckSum);
                        
                        if(dataOrAck == -1){
                            isMissedFrame = true;
                            System.out.println("discarding");
                            continue;
                        }
                        //System.out.println("data or ack : " + dataOrAck);
                        //get sequence num
                        seqNum = getSeqNum(lenWithoutCheckSum);
                        if(seqNum == -1){
                            isMissedFrame = true;
                            System.out.println("discarding");
                            continue;
                        }
                        //System.out.println("seq num : " + seqNum);
                        //System.out.println("");
                        //get acknowledge num
                        ackNum = getAckNum(lenWithoutCheckSum);
                        if(ackNum == -1){
                            isMissedFrame = true;
                            System.out.println("discarding");
                            continue;
                        }
                        //get original data or payload
                        st.add(seqNum);
                        int lenOfPayload = getPayload(lenWithoutCheckSum);
                        //if(frameNum <= 1000 || frameNum >=2000){
                            write.write(payloadBuffer,0,lenOfPayload);
                            //write.flush();
                        //}
                        
                        
                        //}
                        ++frameNum;
                        write.close();
                        
                        //if(frameNum <= 1000 || frameNum >=2000){
                            //msgWrite.writeUTF("acknowledged");
                            //msgWrite.flush();
                        //}
                        
                    }
                    
                    
                    
                    

                }
            } catch (Exception e) {
                //System.out.println("10");
            }
        }
        
        private int checkDataOrAck(int len){
            int val = 0;
            if(len > 0){
                for(int j = 7; j >= 0; j--){
                    int bit = (1<<j)&(dataWithoutCheckSumBuffer[0]);
                    if(bit > 0) bit = 1;else bit = 0;
                    val |= (bit << j);
                }
            }
            else{
                val = -1;
            }
            
            
            return val;
        }
        
        private int getSeqNum(int len){
            int seq = 0;
            if(len > 1){
                for(int j = 7; j >= 0; j--){
                    int bit = (1<<j)&(dataWithoutCheckSumBuffer[1]);
                    if(bit > 0) bit = 1;else bit = 0;
                    seq |= (bit << j);
                }
            }
            else{
                seq = -1;
            }
            return seq;
        }
        
        private int getAckNum(int len){
            int ack = 0;
            if(len > 2){
                for(int j = 7; j >= 0; j--){
                    int bit = (1<<j)&(dataWithoutCheckSumBuffer[2]);
                    if(bit > 0) bit = 1;else bit = 0;
                    ack |= (bit << j);
                }
            }
            else{
                ack = -1;
            }
            return ack;
        }
        
        private int getPayload(int len){
            int in = 0;
            if(len > 3){
                for(int i = 3; i < len;i++){
                    for(int j = 7; j >= 0; j--){
                        int val = (1<<j)&dataWithoutCheckSumBuffer[i];
                        if(val > 0) val = 1;else val = 0;
                        payloadBuffer[in] |= (val << j);
                    }
                    in++;
                }
                
                return in;
            }
            else{
                return 0;
            }
        }
        
        private int checkSumCheck(int len){
            //check checksum and get original data
            int bit = 7,check = 0;
            while(bit >= 0){
                int xor = 0;
                for(int i = 0;i < len; i++){
                    int bitVal = (1<<bit)&deStuffingBuffer[i];
                    if(bitVal > 0) bitVal = 1;
                    else bitVal = 0;
                    xor ^= bitVal;
                    
                }
                //System.out.print(xor);
                check |= (xor << bit);
                bit--;
                
            }
            //System.out.println("");
            
            if(check == 0){
               
                for(int i = 0; i < len-1; i++){
                    for(int j = 7; j >= 0; j--){
                        int val = (1<<j)&deStuffingBuffer[i];
                        if(val > 0) val = 1;
                        else val = 0;
                        
                        dataWithoutCheckSumBuffer[i] |= (val << j);
                    }
                }
               
                return len-1;
            }
            
            else return 0;
        }
        
        private int deStuffing(int len){
            String header = "",flag = "01111110",data="",trailer = "";
            int curByte = 0,curBit = 8;
            boolean isFoundHeader = false,isFoundTrailer = false,isBreak = false;
            //first find header
            while(curByte < len){
                while(curBit > 0){
                    int val = (1<<(--curBit))&bytes[curByte];
                    if(val > 0) val = 1;
                    else val = 0;
                    val += 48;
                    //System.out.print(val);
                    header += (char)val;
                    //System.out.println("header " + header);
                    if(header.equals(flag)){
                        isFoundHeader = true;
                        isBreak = true;
                        break;
                    }
                    else if(header.length() == 8){
                        String tmp = header;
                        header = tmp.substring(1);
                    }
                }
                if(curBit == 0){
                    curByte++;
                    curBit = 8;
                }
                if(isBreak) break;
            }
            
            //System.out.println("");
            
            if(!isFoundHeader){
                System.out.println("yes1");
                return 0;
            }
            isBreak = false;
            while(curByte < len){
                while(curBit > 0){
                    int val = (1<<(--curBit))&bytes[curByte];
                    if(val > 0) val = 1;
                    else val = 0;
                    val += 48;
                    trailer += (char)val;
                    data += (char)val;
                    if(trailer.equals(flag)){
                        isFoundTrailer = true;
                        int dataLength = data.length();
                        dataLength -= 8;
                        String newData = data.substring(0, dataLength);
                        //System.out.println("newData : " + newData);
                        data = newData;
                        isBreak = true;
                        break;
                    }
                    else if(trailer.length() == 8){
                        String tmp = trailer;
                        trailer = tmp.substring(1);
                    }
                }
                if(curBit == 0){
                    curByte++;
                    curBit = 8;
                }
                if(isBreak) break;
            }
            if(!isFoundTrailer){
                System.out.println("yes2");
                return 0;
            }
            
            curBit = 8;
            curByte = 0;
            int zero = 0,one = 0;
            String modData = "";
            int cnt = 0;
            for(int k = 0; k < data.length();k++){
                if(data.charAt(k) == '1') cnt++;

                if(data.charAt(k) == '0'){
                    if(cnt < 5) modData = modData + '0';
                    cnt = 0;
                }
                else{
                    modData = modData + '1';
                }
            }
            
            for(int k = 0; k < modData.length();k++){
                char bit = modData.charAt(k);
                bit -= 48;
                int val = bit;
                deStuffingBuffer[curByte] |= (val << --curBit);
                if(curBit == 0){
                    curBit = 8;
                    curByte++;
                }
            }
            //if(curBit > 0 && curBit != 8) curByte++;
            return curByte;
            
        }
        
        
        
        
        
        
        
        private String getParentDirectory(String file){
            //String os = System.getProperty("os.name").toLowerCase();
            //boolean iswindows = true;
            //if(os.indexOf("win") >= 0) iswindows = true;
            //else iswindows = false;
            int len = file.length();
            len--;
            //if(iswindows){
                while(file.charAt(len) != '\\') len--;
            //}
            //else{
                //while(file.charAt(len) != '/') len--;
            //}
            
            len--;
            int i = 0;
            String files = "";
            while(i <= len){
                files += file.charAt(i);
                i++;
            }
            
            return files;
        }
        
        private String getSenderName(String file){
            String name = "";
            int i = 0;
            while(i < file.length() && file.charAt(i) != '_'){
                name += file.charAt(i);
                i++;
            }
            return name;
        }
        
        private String getReceiverName(String file){
            String name = "";
            int i = 0;
            while(i < file.length() && file.charAt(i) != '_'){
                i++;
            }
            i++;
            while(i < file.length() && file.charAt(i) != '_'){
                name += file.charAt(i);
                i++;
            }
            
            return name;
        }
        
        private String getFileName(String file){
            String name = "";
            int i = 0;
            while(i < file.length() && file.charAt(i) != '_'){
                i++;
            }
            i++;
            while(i < file.length() && file.charAt(i) != '_'){
                i++;
            }
            i++;
            while(i < file.length()){
                name += file.charAt(i);
                i++;
            }
            
            return name;
            
        }
        
        
    }
    
    
}