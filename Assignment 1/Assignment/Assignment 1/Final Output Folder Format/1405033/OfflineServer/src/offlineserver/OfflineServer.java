/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package offlineserver;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import static offlineserver.OfflineServer.clientArr;
import static offlineserver.OfflineServer.loginClient;

/**
 *
 * @author a
 */
public class OfflineServer {

    public static ArrayList<String> loginClient = new ArrayList<String>();
    public static Socket clientArr[] = new Socket[100];
    public static int clientNum = 0;

    public static void main(String[] args) throws IOException {
        int workerThreadCount = 0;
        int id = 1;
        ServerSocket welcomeSocket = new ServerSocket(1234);

        while (true) {
            Socket connectionSocket = welcomeSocket.accept();
            //clientArr[id] = connectionSocket;
            WorkerThread wt = new WorkerThread(connectionSocket, id);
            Thread t = new Thread(wt);
            t.start();
            workerThreadCount++;
            System.out.println("Client [" + id + "] is now connected. No. of worker threads = " + workerThreadCount);
            id++;
        }
    }

//    String bitDeStuff(String getBit) {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//        String s = getBit.substring(8,getBit.length()-8);
//        StringBuilder str = new StringBuilder(s);
//        String newStr = "";
//        int pos = 0,len = getBit.length();
//        while(pos+5 <= s.length())
//        {
//            String ria = s.substring(pos, pos+5);
//            if (ria.equals("11111")) {
//                newStr+=ria;
//                pos = pos+6;
//            } else {
//                newStr+=ria;
//                pos = pos+5;
//            }
//        }
//        
//        if(pos<s.length() && pos>=0)
//        {
//            newStr+=s.substring(pos,s.length());
//        }        
//        return newStr;
//    }
}

class WorkerThread implements Runnable {

    private int clientID = 0;
    //private String[] loginClient = new String[100];
    private String clientSentence;
    private String writeFromServer1;
    private String writeFromServer2;
    private String getWithStuffing;
    private String[] users;
    private OfflineServer offServerObj = new OfflineServer();

    private Socket connectionSocket;
    private int id;

    public WorkerThread(Socket ConnectionSocket, int id) {
        this.connectionSocket = ConnectionSocket;
        this.id = id;
    }

    @Override
    public void run() {
        while (true) {
            try {
                DataOutputStream outToServer = new DataOutputStream(connectionSocket.getOutputStream());
                BufferedReader inFromServer = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
                clientSentence = inFromServer.readLine();
                writeFromServer1 = clientSentence;

                clientSentence = inFromServer.readLine();
                writeFromServer2 = clientSentence;
                int check = 0, clientFound, clientFoundSec;
                for (String s : loginClient) {
                    if (s.equals(writeFromServer1)) {
                        check = 1;
                        break;
                    }
                }
                if (check == 0) {

                    loginClient.add(writeFromServer1);
                    clientFound = Integer.parseInt(writeFromServer1);
                    clientArr[clientFound] = connectionSocket;

                } else if (check == 1) {
                    System.out.println("Previously connected. Cannot connect to server\n");
                }
                int flag = 0;
                clientFound = Integer.parseInt(writeFromServer2);

                for (String s : loginClient) {

                    if (s.equals(writeFromServer2)) {
                        flag = 1;
                        clientSentence = inFromServer.readLine();
                        getWithStuffing = clientSentence;
                        System.out.println(getWithStuffing);

                        String riaS = getWithStuffing.substring(8, getWithStuffing.length() - 8);
                        StringBuilder str = new StringBuilder(riaS);
                        String newStr = "";
                        int pos = 0, len = riaS.length();
                        while (pos + 5 <= len) {
                            String ria = riaS.substring(pos, pos + 5);
                            if (ria.equals("11111")) {
                                newStr += ria;
                                pos = pos + 6;
                            } else {
                                newStr += ria;
                                pos = pos + 5;
                            }
                        }

                        if (pos < len && pos >= 0) {
                            newStr += riaS.substring(pos, len);
                        }
                        
                        System.out.println(newStr);

                        // File f = saveFile(connectionSocket,"newfile.txt");
                        //sendFile(clientArr[clientFound],"another.txt");
                        /*InputStream in = connectionSocket.getInputStream();
                        OutputStream out = new FileOutputStream("new.txt");
                        byte[] bytes = new byte[16*1024];

                        int count;
                        while ((count = in.read(bytes)) > 0) {
                            out.write(bytes, 0, count);
                        }*/
                        //out.close();
                        //in.close();
                        break;
                    }
                }
                if (flag == 0) {
                    System.out.println("Not present in the list\n");
                }
            } catch (Exception e) {

            }
        }
    }
}
