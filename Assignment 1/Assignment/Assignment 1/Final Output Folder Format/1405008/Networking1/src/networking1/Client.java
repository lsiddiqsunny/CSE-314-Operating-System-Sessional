/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package networking1;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.net.Socket;
import java.util.Random;

/**
 *
 * @author Arup
 */
public class Client implements Runnable {

    Socket connectionSocket;
    BufferedReader inFromUser;
    BufferedReader inFromServer;
    DataOutputStream toServer;
    DataInputStream ServerPacket;

    public Client() throws IOException {
        connectionSocket = new Socket("localhost", 5678);
        inFromUser = new BufferedReader(new InputStreamReader(System.in));
        inFromServer = new BufferedReader(
                new InputStreamReader(connectionSocket.getInputStream()));
        toServer = new DataOutputStream(connectionSocket.getOutputStream());
        ServerPacket = new DataInputStream(connectionSocket.getInputStream());

    }

    public int getBit(byte ID, int position) {
        return (ID >> position) & 1;
    }

    public int checkSum(byte[] bytes) {
        int count = 0;
        for (int i = 0; i < bytes.length; i++) {
            byte num = bytes[i];
            for (int j = 0; j < 8; j++) {
                if (getBit(num, j) == 1) {
                    count++;
                }
            }
        }
        return count;
    }

    public byte[] bitStaffing(byte[] bytes) {
        int count = 0;
        int j = 0;
        int local_count = 0;
        int last = 0;
        byte[] byte2 = new byte[100];
        for (int i = 0; i < bytes.length; i++) {
            for (int x = 0; x < 8; x++) {
                if (x == 7 && i == bytes.length - 1) {
                    last = 1;
                }
                if (getBit(bytes[i], x) == 1) {
                    count++;
                    if (count == 5) {
                        byte2[j] = (byte) (byte2[j] | (1 << local_count));
                        local_count++;
                        if (local_count == 8) {
                            j++;
                            local_count = 0;
                        }

                        local_count++;
                        if (local_count == 8 && last != 1) {
                            j++;
                            local_count = 0;
                        }
                        count = 0;
                    } else {
                        byte2[j] = (byte) (byte2[j] | (1 << local_count));
                        local_count++;
                        if (local_count == 8 && last != 1) {
                            j++;
                            local_count = 0;
                        }

                    }
                } else {
                    local_count++;
                    if (local_count == 8 && last != 1) {
                        j++;
                        local_count = 0;
                    }
                    count = 0;
                }
            }
        }

        byte[] byte3 = new byte[j + 1];
        for (int x = 0; x < j + 1; x++) {
            byte3[x] = byte2[x];
        }
        return byte3;
    }

    public byte[] bitDeStaffing(byte[] bytes) {
        int count = 0;
        int local_count = 0;
        int j = 0;
        int last = 0;
        int stuffedBit = 0;

        byte[] byte2 = new byte[bytes.length + 1];

        for (int i = 0; i < bytes.length; i++) {
            for (int x = 0; x < 8; x++) {
                if (x == 7 && i == bytes.length - 1) {
                    last = 1;
                }
                if (getBit(bytes[i], x) == 1) {
                    count++;
                    if (count == 5) {
                        byte2[j] = (byte) (byte2[j] | (1 << local_count));
                        local_count++;
                        if (local_count == 8 && last != 1) {
                            j++;
                            local_count = 0;
                        }
                        count = 0;

                        x++;
                        if (x == 8) {
                            x = 0;
                            i++;
                        }
                        stuffedBit++;
                    } else {
                        byte2[j] = (byte) (byte2[j] | (1 << local_count));
                        local_count++;
                        if (local_count == 8 && last != 1) {
                            j++;
                            local_count = 0;
                        }

                    }
                } else {
                    local_count++;
                    if (local_count == 8 && last != 1) {
                        j++;
                        local_count = 0;
                    }
                    count = 0;
                }
            }
        }

        int numToRemove;

        if ((stuffedBit % 8) != 0) {
            numToRemove = (stuffedBit / 8) + 1;
        } else {
            numToRemove = (stuffedBit / 8);
        }
        byte[] byte3 = new byte[bytes.length - numToRemove];
        for (int x = 0; x < bytes.length - numToRemove; x++) {
            byte3[x] = byte2[x];
        }
        return byte3;

    }

    public byte[] arrayConcatenate(byte[] byte1, byte[] byte2, byte[] byte3) {
        int length = byte1.length + byte2.length + byte3.length;
        byte[] newArray = new byte[length];
        for (int i = 0; i < byte3.length; i++) {
            newArray[i] = byte3[i];
        }
        int j = 0;
        for (int i = byte3.length; i < byte3.length + byte2.length; i++) {
            newArray[i] = byte2[j];
            j++;
        }
        j = 0;
        for (int i = byte3.length + byte2.length; i < length; i++) {
            newArray[i] = byte1[j];
            j++;
        }
        return newArray;
    }

    public byte[] sendAbleByte(byte[] bytes, int i, int error) {

        int c_sum = checkSum(bytes);
        if (error == 1) {
            bytes = ErrorInject(bytes);
        }
        byte[] serial = new byte[1];
        serial[0] = (byte) i;
        byte[] check_sum = new byte[1];
        check_sum[0] = (byte) c_sum;
        byte[] unstuffed = arrayConcatenate(serial, bytes, check_sum);
        byte[] stuffed = bitStaffing(unstuffed);
        int p = 126;
        byte[] flag = new byte[1];
        flag[0] = (byte) p;
        byte[] send = arrayConcatenate(flag, stuffed, flag);
        return send;

    }

    public byte[] removeFlag(byte[] bytes) {
        byte[] byte2 = new byte[bytes.length];
        int j = 0;
        for (int i = 1; i < bytes.length; i++) {
            if ((bytes[i] & 0xFF) != 126) {
                byte2[j] = bytes[i];
                j++;
            } else {
                break;
            }
        }
        byte[] byte3 = new byte[j];
        for (int x = 0; x < j; x++) {
            byte3[x] = byte2[x];
        }
        return byte3;
    }

    public byte[] ErrorInject(byte[] bytes) {
        int flag = 0;
        for (int i = 0; i < bytes.length; i++) {
            byte num = bytes[i];
            for (int x = 0; x < 8; x++) {
                if (getBit(num, x) == 0) {
                    bytes[i] = (byte) (bytes[i] | (1 << x));
                    flag = 1;
                    break;
                }

            }
            if (flag == 1) {
                break;
            }
        }
        return bytes;
    }
    
    public void Print(byte[] bytes ){
    
        for (int i = 0; i < bytes.length; i++) {
            System.out.println(Integer.toBinaryString(bytes[i] & 255 | 256).substring(1));
        }
        System.out.println("");
    }

    public byte[][] ExtractData(byte[] bytes) {
        byte[][] byte2 = new byte[10][100];
        int j = -1;
        int k = 0;
        int size[] = new int[100];
        int count = 0;
        for (int i = 0; i < bytes.length; i++) {
            if (count % 2 == 0) {
                if ((bytes[i] & 0xFF) != 126) {
                    break;
                } else {
                    count++;
                    j++;
                    k = 0;
                }

            } else if ((bytes[i] & 0xFF) == 126) {
                count++;
                size[j] = k;
            } else {
                byte2[j][k] = bytes[i];
                k++;
            }
        }
        byte[][] byte3 = new byte[j + 1][];
        for (int i = 0; i < j + 1; i++) {
            byte3[i] = new byte[size[i]];
            for (int x = 0; x < size[i]; x++) {
                byte3[i][x] = byte2[i][x];
            }
        }
        return byte3;

    }

    @Override
    public void run() {

        try {
            String str;
            System.out.println("Enter ID: ");
            str = inFromUser.readLine();
            toServer.writeBytes(str + '\n');
            str = inFromServer.readLine();

            int error = 0;

            if (str.equals("yes")) {   //Server sayes this client is ok
                while (true) {
                    System.out.println("Do you want to send file? type yes to send file, else type no");
                    str = inFromUser.readLine();
                    toServer.writeBytes(str + '\n');

                    if (str.equals("yes")) {
                        System.out.println("Enter receiver: ");
                        str = inFromUser.readLine();
                        toServer.writeBytes(str + '\n');

                        str = inFromServer.readLine();  //Server sayes wheather receiver is online or offline
                        if (str.equals("yes")) {
                            System.out.println("Enter file path: ");
                            str = inFromUser.readLine();
                            File file = new File(str);
                            FileInputStream in = new FileInputStream(file);

                            //sending file name and size and receiving chunk size and id
                            str = file.getName();
                            toServer.writeBytes(str + '\n');
                            long siz = file.length();
                            System.out.println("file size:" + siz);
                            toServer.writeBytes("" + siz + '\n');
                            str = inFromServer.readLine();

                            int chunk_size = Integer.parseInt(str);
                            str = inFromServer.readLine();
                            int id = Integer.parseInt(str);

                            connectionSocket.setSoTimeout(30000);  //set time out to 30 second

                            //sending by chunk
                            
                            long numSent = 0;
                            int loop = (int) (siz / chunk_size);
                            if (siz % chunk_size != 0) {
                                loop++;
                            }

                            int goBackN = 3;
                            byte[][] bytesArray = new byte[loop][];
                            int sendThisTime;
                            int ran = Integer.MAX_VALUE;
                            DataInputStream serverAck = new DataInputStream(connectionSocket.getInputStream());
                            
                            if (error == 1) {
                                Random rand = new Random();
                                ran = rand.nextInt(loop) + 1;
                                System.out.println("Error injected at " + ran + " no packet");
                            }
                            for (int i = 1; i <= loop; i++) {
                                long numThisTime = siz - numSent;
                                if (numThisTime > chunk_size) {
                                    numThisTime = chunk_size;
                                }
                                byte[] bytes = new byte[(int) numThisTime];
                                //bytesArray[arraySerial]=new byte[(int) numThisTime];
                                int numRead = in.read(bytes, 0, (int) numThisTime);
                                if (i == ran) {
                                    bytesArray[i - 1] = sendAbleByte(bytes, i, 1);
                                } else {
                                    bytesArray[i - 1] = sendAbleByte(bytes, i, 0);
                                }
                                numSent = numSent + numRead;
                            }

                            int sent = 0;
                            while (true) {
                                
                                if ((sent >= loop)) {
                                    break;
                                } else {
                                    sendThisTime = (loop - sent);
                                    if (sendThisTime > goBackN) {
                                        sendThisTime = goBackN;
                                    }
                                    sendThisTime = sendThisTime + sent;
                                }

                                for (int i = sent; i < sendThisTime; i++) {
                                    try {
                                        toServer.write(bytesArray[i], 0, bytesArray[i].length);
                                       // Print(bytesArray[i]);
                                    } catch (IOException ex) {
                                        System.out.println(ex);
                                    }
                                }
                                try {
                                    for (int i = sent; i < sendThisTime; i++) {
                                        byte[] b = new byte[3];
                                        int byteRead = serverAck.read(b, 0, 3);
                                        //b=removeFlag(b);
                                        Print(b);
                                        sent++;
                                        int num=b[1] & 0xFF;
                                        System.out.println("paichi "+num);
                                        
                                    }
                                } catch (IOException ex) {
                                    System.out.println(ex);
                                    
                                }
                                System.out.println("");
                            }

                            in.close();
                            connectionSocket.setSoTimeout(Integer.MAX_VALUE);
                            toServer.writeBytes("finished" + '\n');   //saying server that file sending is complete
                            str = inFromServer.readLine();   //server confirms that received size is correct
                            if (str.equals("success")) {
                                System.out.println("Waiting for receiver to response....");
                                str = inFromServer.readLine();
                                System.out.println("file sent successfully");
                            } else {
                                System.out.println("your file is not the size you told");

                            }
                        } else {
                            System.out.println(str);    //server says receiver is offline
                        }
                    } else if (str.equals("no")) {     //client is waiting to receive file
                        while (true) {
                            String sender = inFromServer.readLine();  //client receives  sender name
                            String string2 = inFromServer.readLine();  //client receives file name
                            String string3 = inFromServer.readLine();  //client receives file size
                            System.out.println("ID " + sender + " want to send you " + string2 + " file, size: " + string3 + " bytes."
                                    + "Type 'Yes' to accept or 'No' to reject ");

                            String string = inFromUser.readLine();
                            toServer.writeBytes(string + '\n');
                            if (string.equals("yes")) {
                                System.out.println("Downloading...");
                                int size = Integer.parseInt(string3);
                                string = inFromServer.readLine();   //client receives chunk size
                                int chunk_size = Integer.parseInt(string);

                                String file_name = string2;
                               

                                File file = new File("E:\\" + sender + "_" + file_name); //creating file in E drive

                                file.createNewFile();
                                FileOutputStream fos = new FileOutputStream(file);

                                int bytesReceived = 0;

                                int loop = (int) (size / chunk_size);
                                if (size % chunk_size != 0) {
                                    loop++;
                                }
                                int byteRead = -1;
                                connectionSocket.setSoTimeout(30000);
                                int dataExpected = 1;

                                int received = 0;
                                while (true) {
                                    if (received >= loop) {
                                        break;
                                    }

                                    byte[] bytes = new byte[120];
                                    try {
                                       
                                        byteRead = ServerPacket.read(bytes, 0, 150);
                                   
                                    } catch (InterruptedIOException iioe) {
                                        System.out.println(iioe);
                                        continue;
                                    }

                                    byte[][] allData = ExtractData(bytes);
                                    for (int x = 0; x < allData.length; x++) {
                                        // byte[] stuffed = removeFlag(bytes);
                                        byte[] stuffed = allData[x];
                                        byte[] deStuffed = bitDeStaffing(stuffed);
                                        byte b = deStuffed[0];

                                        int checksum = b & 0xFF;
                                        b = deStuffed[deStuffed.length - 1];
                                        int serialNo = b & 0xFF;
                                        byte[] data = new byte[deStuffed.length - 2];
                                        for (int p = 1; p < deStuffed.length - 1; p++) {
                                            data[p - 1] = deStuffed[p];
                                        }

                                        if (serialNo == dataExpected) {
                                            if (checksum == checkSum(data)) {
                                                fos.write(data);
                                                bytesReceived = bytesReceived + data.length;
                                                Server.available = Server.available - data.length;
                                                int fg = 126;
                                                byte[] flag = new byte[1];
                                                flag[0] = (byte) fg;
                                                byte[] serial = new byte[1];
                                                serial[0] = b;
                                                byte[] acknowledge = arrayConcatenate(flag, serial, flag);
                                                toServer.write(acknowledge, 0, acknowledge.length);
                                                byteRead = -1;
                                                dataExpected++;
                                                received++;
                                            } else {
                                                System.out.println("data is corrupted");
                                                break;
                                            }
                                        } else {
                                            //System.out.println("seial: " + dataExpected + " not reached");
                                        }
                                    }

                                }
                                fos.close();
                                connectionSocket.setSoTimeout(Integer.MAX_VALUE);

                                //client receiving file chunk by chunk

                                System.out.println("Your file is saved in your E drive");
                                fos.close();
                            }
                        }
                    }
                    else{
                        
                    }
                }
            } else {
                System.out.println(str);
                connectionSocket.close();
            }

        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
