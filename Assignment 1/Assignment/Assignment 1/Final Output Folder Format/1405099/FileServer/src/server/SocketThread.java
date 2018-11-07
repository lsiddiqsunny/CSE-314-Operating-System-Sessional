package server;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.*;
import static java.lang.Thread.sleep;
import java.math.BigInteger;
import java.net.Socket;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SocketThread implements Runnable {

    Socket socket;
    MainFormServer main;
    DataInputStream dis;
    StringTokenizer st;
    String client, filesharing_username;
   

    private final int BUFFER_SIZE = 25;

    public SocketThread(Socket socket, MainFormServer main) {
        this.main = main;
        this.socket = socket;

        try {
            dis = new DataInputStream(socket.getInputStream());
        } catch (IOException e) {
            main.appendMessage("[SocketThreadIOException]: " + e.getMessage());
        }
    }

    @Override
    public void run() {
        try {
            while (true) {
                /**
                 * Get Client Data *
                 */
                String data = dis.readUTF();
                st = new StringTokenizer(data);
                String CMD = st.nextToken();
                /**
                 * Check CMD *
                 */
                switch (CMD) {
                    case "CMD_JOIN":

                        /**
                         * CMD_JOIN [clientUsername] *
                         */
                        String clientUsername = st.nextToken();
                        client = clientUsername;
                        main.AddtoclientUsernameList(clientUsername);
                        main.AddtoclientSocketList(socket);
                        main.appendMessage("[Client]: " + clientUsername + " joins the chatroom.!");
                        System.out.println("CMD_JOIN");

                        break;

                    case "CMD_SHARINGSOCKET":

                        main.appendMessage("CMD_SHARINGSOCKET : Client stablish a socket connection for file sharing...");
                        String file_sharing_username = st.nextToken();
                        filesharing_username = file_sharing_username;
                        main.AddtoFileSharingClientUsername(file_sharing_username);
                        main.AddtoClientFileSharingSocket(socket);
                        main.appendMessage("CMD_SHARINGSOCKET : Username: " + file_sharing_username);
                        main.appendMessage("CMD_SHARINGSOCKET : File sharing is now open");
                        System.out.println("CMD_SHARINGSOCKET");

                        break;

                    case "CMD_SENDFILE":
                        main.appendMessage("CMD_SENDFILE : Client sending a file...");
                        /*
                        Format: CMD_SENDFILE [Filename] [Size] [Recipient] [Sender]  from: Sender Format
                        Format: CMD_SENDFILE [Filename] [Size] [Sender] to  Format
                         */
                        String file_name = st.nextToken();
                        String filesize = st.nextToken();
                        String sendto = st.nextToken();
                        String Sender = st.nextToken();
                        main.appendMessage("CMD_SENDFILE : From: " + Sender);
                        main.appendMessage("CMD_SENDFILE : To: " + sendto);
                        /**
                         * Get the client Socket *
                         */
                        main.appendMessage("CMD_SENDFILE : preparing connections..");
                        Socket cSock = main.getClientFileSharingSocket(sendto);
                        /* Sender Socket  */
 /*   Now Check if the Sender socket was exists.   */
                        if (cSock != null) {
                            /* Exists   */
                            try {
                                main.appendMessage("CMD_SENDFILE : Connected..!");
                                /**
                                 * First Write the filename.. *
                                 */
                                main.appendMessage("CMD_SENDFILE : Sending file to client...");
                                DataOutputStream cDos = new DataOutputStream(cSock.getOutputStream());
                                cDos.writeUTF("CMD_SENDFILE " + file_name + " " + filesize + " " + Sender);
                                /**
                                 * Second send now the file content *
                                 */
                                InputStream input = socket.getInputStream();
                                FileOutputStream fos = new FileOutputStream(file_name);
                                
                                OutputStream sendFile = cSock.getOutputStream();
                                byte[] readbuffer = new byte[BUFFER_SIZE];
                                byte[] writebuffer = new byte[BUFFER_SIZE];
                                int nRead;
                                for (int i = 0; (nRead = input.read(readbuffer)) > 0; i++) {

                                    String ds = "";

                                    for (int j = 0; j < nRead; j++) {
                                        String s2 = String.format("%8s", Integer.toBinaryString(readbuffer[j] & 0xFF)).replace(' ', '0');
                                        ds = ds.concat(s2);
                                        //    System.out.println
                                    }

                                    StringBuilder dss = new StringBuilder(ds);
                                    for (int j = 0; j < dss.length(); j++) {

                                        if ((dss.charAt(j) == '0') && (dss.charAt(j + 1) == '1')) {
                                            break;
                                        } else if ((dss.charAt(j) == '0') && (dss.charAt(j + 1) == '0')) {
                                            dss.deleteCharAt(j);
                                            --j;
                                        }
                                    }

                                    String ds1 = dss.toString();
                                    main.appendMessage(ds1);
                                    ds1 = ds1.replaceAll("01111110", "");
                                    main.appendMessage("removing header trailer");
                                    main.appendMessage(ds1);
                                    int ds1Length = ds1.length();
                                    String ds3 = "";
                                    int dcount1 = 0;
                                    for (int j = 0; j < ds1Length; j++) {
                                        if (ds1.charAt(j) == '0') {
                                            dcount1 = 0;
                                            ds3 = ds3 + '0';
                                        }
                                        if (ds1.charAt(j) == '1' && dcount1 != 5) {
                                            ++dcount1;
                                            ds3 = ds3 + '1';
                                        }
                                        if (dcount1 == 5) {
                                            dcount1 = 0;
                                            j++;
                                        }
                                    }
                                    main.appendMessage("After bit de stuffing");
                                    main.appendMessage(ds3);
                                    String dchecksum = ds3.substring(ds3.length() - 8);
                                    main.appendMessage("Checksum " + dchecksum);
                                    ds3 = ds3.substring(0, ds3.length() - 8);
                                    String dpayload = ds3.substring(ds3.length() - 8);
                                    main.appendMessage("Payload " + dpayload);
                                    ds3 = ds3.substring(0, ds3.length() - 8);
                                    String dack = ds3.substring(ds3.length() - 8);
                                    main.appendMessage("Acknowledgement " + dack);
                                    ds3 = ds3.substring(0, ds3.length() - 8);
                                    String dseqno = ds3.substring(ds3.length() - 8);
                                    main.appendMessage("Seqno " + dseqno);
                                    ds3 = ds3.substring(0, ds3.length() - 8);
                                    main.appendMessage("Actual data after de stuffing");
                                    main.appendMessage(ds3);
                                    main.appendMessage("");

                                    writebuffer = new BigInteger(ds3, 2).toByteArray();
                                    byte sum = (byte) 0;
                                    for (int j = 0; j < writebuffer.length; j++) {
                                        sum = (byte) (sum + writebuffer[j]);
                                    }
                                    byte checksum = (byte) Integer.parseInt(dchecksum, 2);;
                                    sum = (byte) (sum + checksum);
                                    String cksum = String.format("%8s", Integer.toBinaryString(sum & 0xFF)).replace(' ', '0');
                                    main.appendMessage("Checking for data error using checksum");
                                    
                                    int check = Integer.parseInt(cksum, 2);
                                    if (check == 255) {
                                        main.appendMessage("Data is error free");
                                    }
                                    int seqno = Integer.parseInt(dseqno, 2);
                                    main.appendMessage("Packet no " + seqno + " is received ");
                                    fos.write(writebuffer, 0, writebuffer.length);
                                    

                                }
                                fos.flush();
                                fos.close();
                                FileInputStream fis = new FileInputStream(file_name);
                                int count;
                                for(int i=0;(count = fis.read(writebuffer))>0;i++){
                                    sendFile.write(writebuffer, 0, writebuffer.length);
                                }
                                sendFile.flush();
                                sendFile.close();
                                fis.close();
                                File f = new File(file_name);
                                f.delete();
                                /**
                                 * Remove client list *
                                 */
                                main.removeClientFileSharingUsername_Socket(sendto);
                                main.removeClientFileSharingUsername_Socket(Sender);
                                main.appendMessage("CMD_SENDFILE : File was send to client...");
                            } catch (IOException e) {
                                main.appendMessage("[CMD_SENDFILE]: " + e.getMessage());
                            }
                        } else {
                            /*   Not exists, return error  */
 /*   FORMAT: CMD_SENDFILEERROR  */
                            main.removeClientFileSharingUsername_Socket(Sender);
                            main.appendMessage("CMD_SENDFILE : Client '" + sendto + "' was not found.!");
                            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                            dos.writeUTF("CMD_SENDFILEERROR " + "Client '" + sendto + "' was not found, File Sharing will exit.");
                        }
                        System.out.println("CMD_SENDFILE");

                        break;

                    case "CMD_SENDFILERESPONSE":
                        /*
                        Format: CMD_SENDFILERESPONSE [username] [Message]
                         */
                        System.out.println("CMD_SENDFILERESPONSE");
                        String receiver = st.nextToken(); // get the  username
                        String rMsg = ""; // get the error message
                        main.appendMessage("[CMD_SENDFILERESPONSE]: username: " + receiver);
                        while (st.hasMoreTokens()) {
                            rMsg = rMsg + " " + st.nextToken();
                        }
                        try {
                            Socket rSock = (Socket) main.getClientFileSharingSocket(receiver);
                            DataOutputStream rDos = new DataOutputStream(rSock.getOutputStream());
                            rDos.writeUTF("CMD_SENDFILERESPONSE" + " " + receiver + " " + rMsg);
                        } catch (IOException e) {
                            main.appendMessage("[CMD_SENDFILERESPONSE]: " + e.getMessage());
                        }
                        System.out.println("CMD_SENDFILERESPONSE");
                        break;

                    case "CMD_SEND_FILE_XD":
                        System.out.println("CMD_SEND_FILE_XD");// Format: CMD_SEND_FILE_XD [sender] [] [filename]                       
                        try {
                            String file_sender = st.nextToken();
                            String file_ = st.nextToken();
                            String file_filename = st.nextToken();
                            main.appendMessage("[CMD_SEND_FILE_XD]: Host: " + file_sender);
                            //    this.createConnection(, sender, filename);
                            try {
                                main.appendMessage("[createConnection]: creating file sharing connection.");
                                /*   This will get the client socket from client socket list then stablish a connection    */
                                Socket s = main.getSocketofClient(file_);
                                if (s != null) { // Client was exists
                                    main.appendMessage("[createConnection]: Socket OK");
                                    DataOutputStream dosS = new DataOutputStream(s.getOutputStream());
                                    main.appendMessage("[createConnection]: DataOutputStream OK");
                                    // Format:  CMD_FILE_RECEIVE [sender] [] [filename]
                                    String format = "CMD_FILE_RECEIVE " + file_sender + " " + file_ + " " + file_filename;
                                    dosS.writeUTF(format);
                                    main.appendMessage("[createConnection]: " + format);
                                } else {// Client was not exist, send back to sender that  was not found.
                                    main.appendMessage("[createConnection]: Client was not found '" + file_ + "'");
                                    DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                                    dos.writeUTF("CMD_SENDFILEERROR " + "Client '" + file_ + "' was not found in the list, make sure it is on the online list.!");
                                }
                            } catch (IOException e) {
                                main.appendMessage("[createConnection]: " + e.getLocalizedMessage());
                            }
                        } catch (Exception e) {
                            main.appendMessage("[CMD_SEND_FILE_XD]: " + e.getLocalizedMessage());
                        }
                        System.out.println("CMD_SEND_FILE_XD");
                        break;

                    case "CMD_SEND_FILE_ERROR":  // Format:  CMD_SEND_FILE_ERROR [receiver] [Message]
                        String eReceiver = st.nextToken();
                        String eMsg = "";
                        while (st.hasMoreTokens()) {
                            eMsg = eMsg + " " + st.nextToken();
                        }
                        try {
                            /*  Send Error to the File Sharing host  */
                            Socket eSock = main.getClientFileSharingSocket(eReceiver); // get the file sharing host socket for connection
                            DataOutputStream eDos = new DataOutputStream(eSock.getOutputStream());
                            //  Format:  CMD_RECEIVE_FILE_ERROR [Message]
                            eDos.writeUTF("CMD_RECEIVE_FILE_ERROR " + eMsg);
                        } catch (IOException e) {
                            main.appendMessage("[CMD_RECEIVE_FILE_ERROR]: " + e.getMessage());
                        }
                        break;

                    case "CMD_SEND_FILE_ACCEPT": // Format:  CMD_SEND_FILE_ACCEPT [] [Message]
                        String a = st.nextToken();
                        String aMsg = "";
                        while (st.hasMoreTokens()) {
                            aMsg = aMsg + " " + st.nextToken();
                        }
                        try {
                            /*  Send Error to the File Sharing host  */
                            Socket aSock = main.getClientFileSharingSocket(a); // get the file sharing host socket for connection
                            DataOutputStream aDos = new DataOutputStream(aSock.getOutputStream());
                            //  Format:  CMD_RECEIVE_FILE_ACCEPT [Message]
                            aDos.writeUTF("CMD_RECEIVE_FILE_ACCEPT " + aMsg);
                        } catch (IOException e) {
                            main.appendMessage("[CMD_RECEIVE_FILE_ERROR]: " + e.getMessage());
                        }
                        break;

                    default:
                        main.appendMessage("[CMDException]: Unknown Command " + CMD);
                        break;
                }
            }
        } catch (IOException e) {
            /*   this is for chatting client, remove if it is exists..   */
            System.out.println(client);
            System.out.println("File Sharing: " + filesharing_username);
            main.removeFromClient_clientSocketList(client);
            if (filesharing_username != null) {
                main.removeClientFileSharingUsername_Socket(filesharing_username);
            }
            main.appendMessage("[SocketThread]: Client connection closed..!");
        }
    }

}
