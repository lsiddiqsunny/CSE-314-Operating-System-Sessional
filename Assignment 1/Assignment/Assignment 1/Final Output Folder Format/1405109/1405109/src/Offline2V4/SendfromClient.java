/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Offline2V4;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

/**
 *
 * @author ASUS
 */
public class SendfromClient extends Thread {

    private Socket socket;
    String username;
    int recieverId;
    String filename;
    DataOutputStream dos;
    String gs, ls;
    char ch;
    int count;
    byte seqNo;
    String checksum = "00000000";
    ClientForm cf;

    SendfromClient(Socket socket, String username, int recieverId, ClientForm cf) {
        this.socket = socket;
        this.username = username;
        this.recieverId = recieverId;
        this.cf = cf;
    }

    public static String MakeCheckSum(String s1, String s2) {

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

        // System.out.println(sb.length());
        return String.valueOf(sb);
    }

    @Override
    public void run() {

        FileInputStream fis = null;
        JFileChooser jfc = new JFileChooser();
        int returnvalue = jfc.showOpenDialog(null);
        if (returnvalue == JFileChooser.APPROVE_OPTION) {
            filename = jfc.getSelectedFile().toString();
        }
        try {
            dos = new DataOutputStream(socket.getOutputStream());
            dos.writeUTF(filename + " " + username + " " + recieverId);
            System.out.println(filename + " " + username + " " + recieverId);
        } catch (IOException ex) {
            Logger.getLogger(SendfromClient.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {

            File file = new File(filename);
            fis = new FileInputStream(file);
            BufferedInputStream bis = new BufferedInputStream(fis);

            OutputStream os = socket.getOutputStream();

            byte[] contents;
            long fileLength = file.length();

            long current = 0;
            seqNo = 1;

            int akrecieve = 1;
            while (current != fileLength) {
                long start = System.currentTimeMillis();
                if (akrecieve == 1) {
                    int size = 10;
                   // JDialog.setDefaultLookAndFeelDecorated(true);
                    //int response = JOptionPane.showConfirmDialog(null, "Do you want to recieve File?", "Confirm",
                           // JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

                    if (fileLength - current >= size) {
                        current += size;
                        System.out.println("Sending file ... " + (current * 100) / fileLength + "% complete!");
                        cf.writeData("Sending file ... " + (current * 100) / fileLength + "% complete!\n");
                    } else {
                        size = (int) (fileLength - current);
                        current = fileLength;
                        System.out.println("Sending file ... " + (current * 100) / fileLength + "% complete!");
                        cf.writeData("Sending file ... " + (current * 100) / fileLength + "% complete!\n");
                    }
                    contents = new byte[size];
                    char[] seqnobit = new char[8];
                    bis.read(contents, 0, size);
                    int sc = 0;
                    for (byte m = 1; m != 0; m <<= 1) {
                        int bit = ((seqNo & m) != 0) ? 1 : 0;
                        if (bit == 1) {
                            seqnobit[8 - sc - 1] = '1';
                        } else {
                            seqnobit[8 - sc - 1] = '0';
                        }
                        sc++;
                    }
                    String seqarray = new String(seqnobit);
                    System.out.println("this is seq no " + seqarray);
                    cf.writeData("this is seq no " + seqarray + "\n");
                    char[] cbit = new char[8];
                    String chararray;
                    String globalarray = "00000000";
                    //for data type let the byte be like 00000000;
                    globalarray = globalarray + seqarray;///special sequence last e add korbo:type of data+seq num add korlam ekhane

                    for (int i = 0; i < size; i++) {
                        int indx = 0;
                        for (byte m = 1; m != 0; m <<= 1) {
                            int bit = ((contents[i] & m) != 0) ? 1 : 0;
                            if (bit == 1) {
                                cbit[8 - indx - 1] = '1';

                            } else {
                                cbit[8 - indx - 1] = '0';
                            }
                            //   System.out.print(cbit);
                            indx++;
                        }
                        chararray = new String(cbit);
                        globalarray = globalarray + chararray; //ekhane payload add korlam

                    }
                    //  System.out.println("this is checksum " + checksum);
                    //checksum add korbo ekhane
                    /*  char[] csbit = new char[8];  //checksumbit
                    sc = 0;
                 for (byte m = 1; m != 0; m <<= 1) {
                        int bit = ((checksum & m) != 0) ? 1 : 0;
                        if (bit == 1) {
                            csbit[8 - sc - 1] = '1';
                        } else {
                            csbit[8 - sc - 1] = '0';
                        }
                        sc++;
                    }
                    String csarray = new String(csbit);*/
                    int globalarraylen = globalarray.length();

                    for (int i = 16; i < globalarraylen; i += 8) {
                        String s1 = globalarray.substring(i, i + 8);
                        checksum = MakeCheckSum(s1, checksum);
                    }
                    System.out.println("this is checksum " + checksum);
                    cf.writeData("this is checksum \n" + checksum + "\n");
                    System.out.println("checksum after complementing ");
                    cf.writeData("checksum after complementing \n");

                    checksum = checksum.replaceAll("0", "x").replaceAll("1", "0").replaceAll("x", "1");
                    System.out.println(checksum);
                    cf.writeData(checksum + "\n");
                    globalarray = globalarray + checksum;
                    //
                    //  globalarray = globalarray + "01111110"; bit stuff korar pore special seq add korbo
                    System.out.println(globalarray);
                    cf.writeData("the frame\n");
                    cf.writeData(globalarray + "\n");
                    StringBuffer stb = new StringBuffer(globalarray);
                    int len = stb.length();

                    for (int iK = 0; iK < len; iK++) {
                        ch = stb.charAt(iK);
                        if (ch == '0') {
                            count = 0;
                        } else {
                            count++;
                            if (count == 5) {
                                stb.insert(iK + 1, '0');
                            }

                        }

                    }
                    System.out.println("after bit stuffing");
                    cf.writeData("after bit stuffing\n");
                    System.out.println(stb);
                    cf.writeData(stb + "\n");
                    System.out.println("after adding special sequence");
                    cf.writeData("after adding special sequence\n");
                    StringBuffer sbuf = new StringBuffer();
                    sbuf.append("01111110").append(stb).append("01111110");
                    System.out.println(sbuf);
                    cf.writeData(sbuf.toString() + "\n");
                    //  System.out.println(sbuf.length());

                    contents = String.valueOf(sbuf).getBytes();
                    os.write(contents);
                    BufferedReader bf = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                    String ack = bf.readLine();
                    long end = System.currentTimeMillis();
                    if (end - start > 30000) {
                        cf.writeData("Sever not available " + "\n");
                        akrecieve = 0;
                        continue;
                    }
                    if (ack != null) {

                        System.out.println("Acknowledgement no " + ack + " was Received from receiver");
                        cf.writeData("Acknowledgement no " + ack + " was Received from receiver\n");
                        akrecieve = 1;

                        Thread.sleep(4000);
                    } else {
                        akrecieve = 0;

                    }
                    seqNo++;

                } else {
                    int size = 10;
                    current -= size;
                    seqNo--;
                    if (fileLength - current >= size) {
                        current += size;
                        System.out.println("ReSending file ... " + (current * 100) / fileLength + "% complete!");
                        cf.writeData("ReSending file ... " + (current * 100) / fileLength + "% complete!\n");
                    } else {
                        size = (int) (fileLength - current);
                        current = fileLength;
                        System.out.println("ReSending file ... " + (current * 100) / fileLength + "% complete!");
                        cf.writeData("ReSending file ... " + (current * 100) / fileLength + "% complete!\n");
                    }
                    contents = new byte[size];
                    char[] seqnobit = new char[8];
                    bis.read(contents, 0, size);
                    int sc = 0;
                    for (byte m = 1; m != 0; m <<= 1) {
                        int bit = ((seqNo & m) != 0) ? 1 : 0;
                        if (bit == 1) {
                            seqnobit[8 - sc - 1] = '1';
                        } else {
                            seqnobit[8 - sc - 1] = '0';
                        }
                        sc++;
                    }
                    String seqarray = new String(seqnobit);
                    System.out.println("this is seq no " + seqarray);
                    cf.writeData("this is seq no " + seqarray + "\n");
                    char[] cbit = new char[8];
                    String chararray;
                    String globalarray = "00000000";
                    //for data type let the byte be like 00000000;
                    globalarray = globalarray + seqarray;///special sequence last e add korbo:type of data+seq num add korlam ekhane

                    for (int i = 0; i < size; i++) {
                        int indx = 0;
                        for (byte m = 1; m != 0; m <<= 1) {
                            int bit = ((contents[i] & m) != 0) ? 1 : 0;
                            if (bit == 1) {
                                cbit[8 - indx - 1] = '1';

                            } else {
                                cbit[8 - indx - 1] = '0';
                            }
                            //   System.out.print(cbit);
                            indx++;
                        }
                        chararray = new String(cbit);
                        globalarray = globalarray + chararray; //ekhane payload add korlam

                    }
                    //  System.out.println("this is checksum " + checksum);
                    //checksum add korbo ekhane
                    /*  char[] csbit = new char[8];  //checksumbit
                    sc = 0;
                 for (byte m = 1; m != 0; m <<= 1) {
                        int bit = ((checksum & m) != 0) ? 1 : 0;
                        if (bit == 1) {
                            csbit[8 - sc - 1] = '1';
                        } else {
                            csbit[8 - sc - 1] = '0';
                        }
                        sc++;
                    }
                    String csarray = new String(csbit);*/
                    int globalarraylen = globalarray.length();

                    for (int i = 16; i < globalarraylen; i += 8) {
                        String s1 = globalarray.substring(i, i + 8);
                        checksum = MakeCheckSum(s1, checksum);
                    }
                    System.out.println("this is checksum " + checksum);
                    cf.writeData("this is checksum \n" + checksum + "\n");
                    System.out.println("checksum after complementing ");
                    cf.writeData("checksum after complementing \n");

                    checksum = checksum.replaceAll("0", "x").replaceAll("1", "0").replaceAll("x", "1");
                    System.out.println(checksum);
                    cf.writeData(checksum + "\n");
                    globalarray = globalarray + checksum;
                    //
                    //  globalarray = globalarray + "01111110"; bit stuff korar pore special seq add korbo
                    System.out.println(globalarray);
                    cf.writeData("the frame\n");
                    cf.writeData(globalarray + "\n");
                    StringBuffer stb = new StringBuffer(globalarray);
                    int len = stb.length();

                    for (int iK = 0; iK < len; iK++) {
                        ch = stb.charAt(iK);
                        if (ch == '0') {
                            count = 0;
                        } else {
                            count++;
                            if (count == 5) {
                                stb.insert(iK + 1, '0');
                            }

                        }

                    }
                    System.out.println("after bit stuffing");
                    cf.writeData("after bit stuffing\n");
                    System.out.println(stb);
                    cf.writeData(stb + "\n");
                    System.out.println("after adding special sequence");
                    cf.writeData("after adding special sequence\n");
                    StringBuffer sbuf = new StringBuffer();
                    sbuf.append("01111110").append(stb).append("01111110");
                    System.out.println(sbuf);
                    cf.writeData(sbuf.toString() + "\n");
                    //  System.out.println(sbuf.length());

                    contents = String.valueOf(sbuf).getBytes();
                    os.write(contents);
                    BufferedReader bf = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                    String ack = bf.readLine();

                    if (ack != null) {

                        System.out.println("Acknowledgement was Received from receiver");
                        cf.writeData("Acknowledgement no " + ack + " was Received from receiver\n");
                        akrecieve = 1;
                        Thread.sleep(4000);
                    } else {
                        akrecieve = 0;
                        

                    }
                    seqNo++;
                }
            }

            os.flush();

            socket.close();
            System.out.println("File sent succesfully!");
            cf.writeData("File sent succesfully!");
        } catch (FileNotFoundException ex) {
            Logger.getLogger(SendfromClient.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(SendfromClient.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(SendfromClient.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                fis.close();
            } catch (IOException ex) {
                Logger.getLogger(SendfromClient.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
