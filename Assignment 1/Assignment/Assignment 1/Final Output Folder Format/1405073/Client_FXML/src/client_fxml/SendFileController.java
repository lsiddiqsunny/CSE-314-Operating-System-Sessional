/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client_fxml;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import static javafx.application.ConditionalFeature.FXML;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author user
 */
public class SendFileController implements Initializable {

    /**
     * Initializes the controller class.
     */
    boolean validID, isOnline, fileChosen, serverReady;

    @FXML
    private TextField recieverTextField, filepathTextField;
    @FXML
    private TextArea textArea;

    @FXML
    void checkValidityButtonPressed(ActionEvent e) {

        if (isOnline == false) { //maney kono reciever e nite pari nai, nite hobey

            //reciever er id nilam
            try {
                Client_FXML.receiverID = Integer.parseInt(recieverTextField.getText());
                validID = true;
            } catch (NumberFormatException numberFormatException) {
                recieverTextField.clear();
                validID = false;
                ShowAlert("ID-Error", "Enter Numbers Only.", Alert.AlertType.ERROR);
            }

            //server e pathai dilam
            if (validID) {
                try {
                    Client_FXML.dOut.writeInt(Client_FXML.receiverID);
                } catch (IOException ex) {
                    Logger.getLogger(SendFileController.class.getName()).log(Level.SEVERE, null, ex);
                }

                //server theke feedback nilam
                try {
                    Thread.sleep(500);
                    if (Client_FXML.dIn.readBoolean() == false) {
                        recieverTextField.clear();
                        ShowAlert("Reciever Status", "Reciever is not online, enter another Reciever-ID.", Alert.AlertType.CONFIRMATION);
                    } else {
                        isOnline = true;
                        ShowAlert("Receiver Status", "Reciever is online, select file and press Send button", Alert.AlertType.CONFIRMATION);
                    }

                } catch (InterruptedException | IOException ex) {
                    Logger.getLogger(SendFileController.class.getName()).log(Level.SEVERE, null, ex);
                }

            }

        } else {
            ShowAlert("Repeat Error", "You have already selected a receiver who is online,now select file.", Alert.AlertType.ERROR);
        }

    }

    @FXML
    void browseButtonPressed(ActionEvent e) {

        //reciever online e na thakle file select krte dibey na
        if (isOnline) {

            filepathTextField.clear();

            FileChooser fc = new FileChooser();
            Client_FXML.chosenFile = fc.showOpenDialog(null);

            filepathTextField.appendText(Client_FXML.chosenFile.getAbsolutePath());

            //System.out.println(Client_FXML.chosenFile.length());
            Client_FXML.fileSize = Client_FXML.chosenFile.length();
            fileChosen = true;

            try {
                textArea.appendText("File Name :\n" + Client_FXML.chosenFile.getName() + "\n");
                textArea.appendText("File Size :\n" + Long.toString(Client_FXML.fileSize) + " byte(s)\n");

            } catch (Exception ex) {
                System.out.println("Text Area tey append hocchey na");
            }
            try {
                textArea.setVisible(true);
            } catch (Exception ex) {
                System.out.println("Text Area Visible hocchey na");
            }
        } else {
            ShowAlert("File Selection Error", "Please, enter Reciever ID first and check validity.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    void sendButtonPressed(ActionEvent e) {

        //server e file size pathaitey hobe
        System.out.println("Send button press hoisey");
        try {
            Client_FXML.dOut.writeLong(Client_FXML.fileSize);
            Thread.sleep(500);
        } catch (IOException | InterruptedException ex) {
            Logger.getLogger(SendFileController.class.getName()).log(Level.SEVERE, null, ex);
        }
        //System.out.println("FileSize pathano hoisey");

        //server theke chunk-size nitey hobey
        try {
            if (Client_FXML.dIn.readBoolean() == true) {
                Client_FXML.chunkSize = Client_FXML.dIn.readInt();
                textArea.appendText("Chunk Size :\n" + Integer.toString(Client_FXML.chunkSize) + " byte(s)\n");
                System.out.println("Chunk-Size: " + Client_FXML.chunkSize);
                serverReady = true;

                //server e fileName pathaidilam
                Client_FXML.dOut.writeUTF(Client_FXML.chosenFile.getName());
            } else {
                ShowAlert("Server Error", "Server buffer is full, cannot send file. Terminating Program.", Alert.AlertType.ERROR);
                System.exit(0);
            }
        } catch (IOException ex) {
            Logger.getLogger(SendFileController.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (serverReady) {
            //client er buffer array banailam
            Client_FXML.clientBuffer = new byte[(int) Client_FXML.fileSize];

            //file ta buffer e niye nilam
            try {
                FileInputStream fIn = new FileInputStream(Client_FXML.chosenFile);
                if (fIn.read(Client_FXML.clientBuffer) != Client_FXML.fileSize) {
                    System.out.println("Failed to load the file in buffer");
                    System.exit(0);
                }
                System.out.println("File is loaded in client's buffer");
            } catch (FileNotFoundException ex) {
                Logger.getLogger(SendFileController.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(SendFileController.class.getName()).log(Level.SEVERE, null, ex);
            }

            System.out.print("ShowFrame-GUI asar agey\n");
            System.out.println("File Size " + Client_FXML.fileSize + " ChunkSize " + Client_FXML.chunkSize + "\n");

            /*
             //##################################
             int testVar1=0,testVar2;
             testVar2=(int)(Client_FXML.fileSize/Client_FXML.chunkSize);
             for (int i = 0; i < Client_FXML.fileSize; i++) {
             //System.out.print(Integer.toBinaryString((int)Client_FXML.clientBuffer[i]) + "\t");
             if (i%Client_FXML.chunkSize==0)
             {
             System.out.println("\nChunk No "+(testVar1++)+"\n");
             }
             System.out.print(i+" ");
             printBits(Client_FXML.clientBuffer[i]);
             if (i != 0 && (i % 19 == 0)) {
             System.out.println();
             }
             }
             System.out.println();
             //##################################################
             */
            //packet banaite hobey
            byte packets[][] = new byte[255][];
            int seq = 0;
            int start = 0;

            for (int i = 0; i < Client_FXML.fileSize / Client_FXML.chunkSize; i++) {

                packets[seq] = new byte[Client_FXML.chunkSize + 4];

                packets[seq][0] = -86;//kind
                packets[seq][1] = (byte) seq;
                packets[seq][2] = 0; //no ack

                int byteIndex = 3;

                for (int l = 0; l < Client_FXML.chunkSize; l++) {
                    packets[seq][byteIndex++] = Client_FXML.clientBuffer[start++];
                }
                seq++;
                //System.out.println();
                //System.out.print("SeqNo: ");
                //System.out.print(Integer.toBinaryString((int) packets[seq][1]) + "\t");
                //printBits(packets[seq][1]);
                //System.out.println("Payload:");
//                for (int l = 3; l < Client_FXML.chunkSize + 3; l++) {
//
//                    //    System.out.print(Integer.toBinaryString((int) packets[seq][l]) + "\t");
//                    System.out.print((seq * Client_FXML.chunkSize + (l - 3)) + " ");
//                    printBits(packets[seq][l]);
//
//                    if ((l - 3) % 19 == 0 && (l - 3) != 0) {
//                        System.out.println();
//                    }
//                }
//                System.out.println("\n");
//                seq++;
            }
            int totalPackets;
            //baki onghso packet kra lagbey
            totalPackets = seq = (int) (Client_FXML.fileSize / Client_FXML.chunkSize);
            if (seq * Client_FXML.chunkSize < Client_FXML.fileSize) {
                totalPackets += 1;
                int remaining = (int) (Client_FXML.fileSize - seq * Client_FXML.chunkSize);
                //System.out.println("\nThe last seqNo is " + seq);
                //System.out.println("Remaining bytes " + remaining);
                packets[seq] = new byte[remaining + 4];

                packets[seq][0] = -86;//kind
                packets[seq][1] = (byte) seq;
                packets[seq][2] = 0; //no ack

                int byteIndex = 3;
                start = seq * Client_FXML.chunkSize;

                //System.out.print("Last SeqNo: ");
                //System.out.print(Integer.toBinaryString((int) packets[seq][1]) + "\t");
                //printBits(packets[seq][1]);
                //System.out.println("Payload:");
                //  System.out.println("Start " + start + " End " + (start + remaining - 1));
                for (int l = 0; l < remaining; l++) {
                    // System.out.print(start + " ");
                    packets[seq][byteIndex++] = Client_FXML.clientBuffer[start++];
                    //System.out.print(Integer.toBinaryString((int) packets[seq][l]) + "\t");
                    //  printBits(packets[seq][l + 3]);
//                    if (l % 19 == 0 && l != 0) {
//                        System.out.println();
//                    }
                }

            }

            //checkSum ready kra lagbey
            for (int pckts = 0; pckts < totalPackets; pckts++) {

                long numberOfOnes = 0;
                for (int showbytes = 0; showbytes < packets[pckts].length; showbytes++) {

                    for (byte bit = 7; bit >= 0; bit--) {
                        if ((((byte) 1 << bit) & packets[pckts][showbytes]) != 0) {
                            numberOfOnes++;
                        }
                    }

                    if (showbytes == packets[pckts].length - 1) {
                        packets[pckts][showbytes] = (byte) (numberOfOnes % 255);
                    }
                }
            }
            //checkSum o ready

            //NOT TO 
            //shob packet print kre dekhai *************************
            /*
             for (int pckts = 0; pckts < totalPackets; pckts++) {
             long numberOfOnes = 0;
             System.out.println("\n\nPacket No: " + pckts);
             // System.out.println("\nSeqNo: " + packets[pckts][1] + " PayLoad: ");
             for (int showbytes = 0; showbytes < packets[pckts].length; showbytes++) {
             if ((showbytes) > 0 && (showbytes) % 20 == 0) {
             System.out.println();
             }

             printBits(packets[pckts][showbytes]);
             }
             }
             */
            //stuffing krte hbe 
            for (int pckts = 0; pckts < totalPackets; pckts++) {
                int counter = 0;
                String s = new String();
                System.out.println("\n\nPacket No: " + pckts + " BEFORE stuffing");
                for (int showbytes = 0; showbytes < packets[pckts].length; showbytes++) {
                    for (byte bit = 7; bit >= 0; bit--) {
                        if ((((byte) 1 << bit) & packets[pckts][showbytes]) != 0) {
                            counter++;
                            if (counter == 5) {
                                s += 0;
                                counter = 0;
                            }
                            s += 1;

                        } else {
                            s += 0;
                            counter = 0;
                        }
                    }
                    if ((showbytes) > 0 && (showbytes) % 20 == 0) {
                        System.out.println();
                    }
                    printBits(packets[pckts][showbytes]);
                }

                //stuffed obosthai ki seta recover kra lagbe
//                
                System.out.println("\n\nPacket No: " + pckts + " AFTER stuffing");
                int totalBitInStuffedPacket = s.length();
                //System.out.println("Total bits in stuffed Packets " + totalBitInStuffedPacket);

                int newPacketSize;
                if (totalBitInStuffedPacket % 8 != 0) {
                    int rem = 8 - totalBitInStuffedPacket % 8;
                    for (int extrabit = 0; extrabit < rem; extrabit++) {
                        s += 0;
                    }
                    totalBitInStuffedPacket = s.length();

                }
                newPacketSize = (int) (totalBitInStuffedPacket / 8);

               // System.out.println();
                //System.out.println("Total bits in stuffed Packets after adding extra 0 : " + totalBitInStuffedPacket);
//
                byte[] newPacket = new byte[newPacketSize];
//
               // System.out.println("New Packet created");

//                for (int bit=0;bit<totalBitInStuffedPacket;bit++)
//                {
//                    if (bit%8==0 && bit>0) System.out.print(" ");
//                    
//                    System.out.print(s.charAt(bit));
//                    
//                    if ((bit/160)>0 && (bit%160==0))
//                        System.out.println();
//                }
//                System.out.println();
//                
                for (int bit = 0; bit < totalBitInStuffedPacket; bit++) {

                    newPacket[bit / 8] = (byte) (newPacket[bit / 8] | s.charAt(bit));
                   
                    if (bit % 8 != 7) {
                        newPacket[bit / 8] = (byte) (newPacket[bit / 8] << 1);
                    }

                }
                //System.out.println("Packet " + pckts + " is stuffed");
                packets[pckts] = newPacket;
                for (int showbytes = 0; showbytes < packets[pckts].length; showbytes++) {
                    if ((showbytes) > 0 && (showbytes) % 20 == 0) {
                        System.out.println();
                    }

                    printBits(packets[pckts][showbytes]);
                }
                
                byte [] packetWithFlag=new byte[packets[pckts].length+2];
                
                packetWithFlag[0]=126;
                packetWithFlag[packets[pckts].length+1]=126;
                
                for (int newp=1;newp<packets[pckts].length+2-1;newp++)
                {
                    packetWithFlag[newp]=packets[pckts][newp-1];
                }
                
                //eita k pathaite hobey

            }

        }

    }

    void ShowAlert(String title, String ctext, Alert.AlertType type) {
        Alert failed = new Alert(type);
        failed.setTitle(title);
        failed.setContentText(ctext);
        failed.setHeaderText(null);
        failed.showAndWait();
    }

    void printBits(byte b) {

        for (int i = 7; i >= 0; i--) {
            if (((1 << i) & b) != 0) {
                System.out.print(1);
            } else {
                System.out.print(0);
            }
        }
        System.out.print(" ");
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        serverReady = validID = isOnline = fileChosen = false;

    }

}
