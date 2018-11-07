/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Offline2V4;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ASUS
 */
public class SendtoServer extends Thread {

    private Socket socket;
    private ServerSocket ssock;
    SendfromServer sendfromserver;
    StringTokenizer st;
    DataInputStream dis;
    String Reciever, Sender, filename;
    String newfilepath;
    ServerFrame2 sf;
    PrintStream myps;
    int AckNo=1;

    SendtoServer(Socket socket, ServerFrame2 sf) {
        this.socket = socket;
        this.sf = sf;
    }

    String getRecieverId() {
        return Reciever;
    }

    public static String MakeCheckSumBack(String s1, String s2) {

        int first = 7;
    
        StringBuilder sb = new StringBuilder();
        int carry = 0;
        while (first >= 0) {
            int sum = carry;
            if (first >= 0) {
                sum += s1.charAt(first) - '0';
                sum += s2.charAt(first) - '0';
                  first--;
            }
            carry = sum >> 1;
            sum = sum & 1;
            sb.append(sum == 0 ? '0' : '1');
        }
        sb.reverse();
        if (carry > 0) {

            int sum = carry;
            sum = +sb.charAt(0) + '1';
        }

        //System.out.println(sb.length());
        return String.valueOf(sb);
    }

    boolean hasChecksomeError(String s1, String s2) {
        String checksumResult = MakeCheckSumBack(s1, s2);
        checksumResult = checksumResult.replaceAll("0", "x").replaceAll("1", "0").replaceAll("x", "1");
        System.out.println("ChecksumResult is " + checksumResult);
        sf.accesstextarea("CheckSumResult is: " + checksumResult + "\n");
        if (checksumResult.equals("00000000")) {
            return true;
        } else {
            return false;
        }
    }

    StringBuffer BitdeStuff(String nstring) {

        //Arrays.copyOfRange(contents,0,7);
        // System.out.println(nstring);//this upper prtion will go to the main function
        //now bit destuffing------
        StringBuffer stb = new StringBuffer(nstring);
        int len = stb.length();
        char ch;
        int count = 0;
        for (int i = 0; i < len; i++) {
            ch = stb.charAt(i);
            if (ch == '0') {
                count = 0;
            } else {
                count++;
                if (count == 5) {
                    stb.deleteCharAt(i + 1);
                    len = len - 1;
                    count = 0;
                }

            }

        }
        System.out.println("after destuffing " + stb);
        sf.accesstextarea("after destuffing\n" + stb.toString() + "\n");
        return stb;
    }

    @Override

    public void run() {

        FileOutputStream fos = null;
        try {

            dis = new DataInputStream(socket.getInputStream());
            String Data = dis.readUTF();
            st = new StringTokenizer(Data);
            filename = st.nextToken();
            Sender = st.nextToken();
            Reciever = st.nextToken();
            sendfromserver = new SendfromServer(socket, filename, Sender, Reciever, sf);

        } catch (IOException ex) {
            System.out.println("there was no data in inputstream of the socket");
            sf.accesstextarea("there was no data in inputstream of the socket");
        }

        try {

            byte[] contents = new byte[1024];
            sf.accesstextarea("Data Recieving from " + Sender + "\n");
            newfilepath = "D:\\java\\routine.txt";
            fos = new FileOutputStream(newfilepath);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            InputStream is = socket.getInputStream();

            int bytesRead = 0;
            myps = new PrintStream(socket.getOutputStream());
            while ((bytesRead = is.read(contents, 0, 1024)) != -1) {
                ///ekhanei chunk kore recieve kortesi,........
                // System.out.println(contents);
                // sf.accesstextarea(contents.toString());
                byte[] Arrays = new byte[bytesRead - 16];

                for (int i = 0; i < bytesRead - 16; i++) {
                    Arrays[i] = contents[i + 8];
                    // System.out.println(contents[i + 8] + " " + Arrays[i]);
                }
                String nstring = new String(Arrays);
                sf.accesstextarea("Stuffed bits recieved \n");
                sf.accesstextarea("01111110" + nstring + "01111110\n");
                StringBuffer sbf = new StringBuffer(BitdeStuff(nstring));
                String destuffedstring = sbf.toString();
                int dsslen = destuffedstring.length();
                // System.out.println(dsslen);
                // char[] seq=new char[8];
                //destuffedstring.getChars(0, 7, seq, 0);
                // System.out.println("this is sequence"+seq);
                String seqs = destuffedstring.substring(8, 16);
                byte seqNo = Byte.valueOf(seqs);
                sf.accesstextarea("\nSequence No  is: " + seqNo + "\n");
                String css = destuffedstring.substring(dsslen - 8);
                sf.accesstextarea("Inverted destuffed checksum sequence is: " + css + "\n");
                String payload = destuffedstring.substring(16, dsslen - 8);
                System.out.println(payload);
                sf.accesstextarea("payload is: " + payload + "\n");
                int payloadlen = payload.length();

                // byte checksum=Byte.valueOf(css);
                String checksumBack = "00000000";
                for (int i = 0; i < payloadlen; i += 8) {
                    String s1 = payload.substring(i, i + 8);
                    //System.out.println("bits are " + s1);
                    checksumBack = MakeCheckSumBack(s1, checksumBack);
                }
                //int checksum=Integer.parseInt(css);
                System.out.println("checksum back is" + checksumBack + "\n");
                sf.accesstextarea("checksum at server is: " + checksumBack + "\n");
                //
                boolean cksm = hasChecksomeError(css, checksumBack);

                if (cksm) {
                    System.out.println("Data was recieved with out any loss");
                    sf.accesstextarea("Data was recieved with out any loss\n");
                } else {
                    System.out.println("Data was recieved with errors");
                    sf.accesstextarea("Data was recieved with errors\n");
                }

                int flen = payloadlen / 8;
                byte[] fbyte = new BigInteger(payload, flen).toByteArray();
                //System.out.println(fbyte);
                ////////////////////////////////////////////////////////////////
                String backtobyte;
                int indx = 0;
                byte[] nbyte = new byte[flen];
                for (int i = 0; i < payloadlen; i += 8) {

                    backtobyte = payload.substring(i, i + 8);
                    // System.out.println( backtobyte);
                    int pl = Integer.parseInt(backtobyte, 2);
                    nbyte[indx] = (byte) pl;
                    indx++;

                }
                //////////////////////////////////////////////////////////////////

                myps.println(Integer.toString(AckNo));
                AckNo++;
                bos.write(nbyte, 0, flen);
            }
            bos.flush();
            socket.close();

            System.out.println("File saved successfully!");

        } catch (FileNotFoundException ex) {
            Logger.getLogger(SendtoServer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(SendtoServer.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                fos.close();
            } catch (IOException ex) {
                Logger.getLogger(SendtoServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        sendfromserver.start();
    }
}
