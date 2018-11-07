package sample;

import com.sun.org.apache.regexp.internal.RE;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Optional;

/**
 * Created by DELL on 21-Sep-17.
 */
public class SecondPage {

    public Main main;

    private String ipAddress = "127.0.0.1";
    private String ipAddress2 = "192.168.0.5";
    private int port = 5566;

    //Socket1 for sending file purpose
    public Socket socket;
    public ObjectInputStream ois;
    public ObjectOutputStream oos;

    //Socket2 for receiving file purpose
    public Socket socket2;
    public ObjectOutputStream oos2;
    public ObjectInputStream ois2;



    //All the page field initialization
    //Send
    public TextField ReceiverId;
    public Text SendText;
    public Text FileName;
    public Button SendBtn;

    //Receive
    public Text ReceiveText;
    public Text SenderId;
    public Text ReceiverFileName;
    public Text ReceiverFileSize;
    public Text YesOrNo;


    //FileInfo
    private String file_path;
    private int size_of_file;
    private int allowed_chunk_size;
    private String student_id;
    private String receiver_id;

    BufferedInputStream bis;
    FileInputStream fis;

    //User Id
    public Text CurrentUserId;

    public Button closeButton;

    private boolean sendAble = false;

    //public int sharedVar;

    private String modeOfError;



    //Modified Payload Characterstics
    private int checksum;
    private int endOfModPayload;
    private int sizeOfModPayload;


    //Frame characterstics
    private int sizeOfFrame;

    //Store Frame characterstics so that they can be re-sent
    private byte[] ResendFrame;
    private int posResendFrame;
    private String errorString;
    private ArrayList markers;

    public void setMain(Main m){
        try {
            main = m;

            SendBtn.setVisible(false);


            socket = new Socket(ipAddress, port);
            oos = new ObjectOutputStream(socket.getOutputStream());
            ois = new ObjectInputStream(socket.getInputStream());

            socket2 = new Socket(ipAddress, port);
            oos2 = new ObjectOutputStream(socket2.getOutputStream());
            ois2 = new ObjectInputStream(socket2.getInputStream());


            oos.writeObject(main.getStudentId());
            oos2.writeObject(main.getStudentId());

            //System.out.println("Until this");

            String rec = ois.readObject().toString();
            String rec2 = ois2.readObject().toString();
            if (rec.equalsIgnoreCase("Failure")) {
                System.out.println("Connection closed");
                CloseConnection1();
                CloseConnection2();
                ShowAlert();
            } else if (rec.equals("Successful Login")) {
                System.out.println("Successful Login");

                CurrentUserId.setText(main.getStudentId());

            /*socket2 = new Socket(ipAddress, port);
            oos2 = new ObjectOutputStream(socket2.getOutputStream());
            ois2 = new ObjectInputStream(socket2.getInputStream());*/

                new ReceivePage(this);

            }
        }catch (Exception e){
            System.out.println("Unexpected Problem in Server!");

            try {
                CloseConnection1();
                CloseConnection2();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }





    }


    public void ChooseFileBtnClicked(){
        try {

            FileDialog fd = new FileDialog(new JFrame());
            fd.setVisible(true);
            File[] f = fd.getFiles();
            String file = "";
            if (f.length > 0) {
                oos.writeObject("ChooseFile");

                //System.out.println(fd.getFiles()[0].getAbsolutePath());
                file = fd.getFiles()[0].getAbsolutePath();
                String ff[] = file.split("\\\\");
                String file_name = ff[ff.length - 1];
                FileName.setText(file_name);
                file_path = file;
                File F = new File(file_path);
                size_of_file = (int) F.length();
                System.out.println("size of file: " + size_of_file);


                oos.writeObject(file_name);
                oos.writeObject(size_of_file);

                String rec = (String) ois.readObject();
                //System.out.println("Server has " + rec);
                if (rec.equals("Allowed")) {
                    SendText.setText("You are allowed to send file");

                    //System.out.println("Allowed to send file");
                    SendBtn.setVisible(true);


                    allowed_chunk_size = (int) ois.readObject();
                    student_id = ((String) ois.readObject());


                } else if (rec.equals("Not Allowed")) {
                    SendText.setText("You are not allowed to send file");
                    SendBtn.setVisible(false);
                }

            }
        }catch (Exception e){
            System.out.println("Unexpected Problem in Server!");

            try {
                CloseConnection1();
                CloseConnection2();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }


    }

    public void SendBtnClicked() throws IOException {
        try {
            ResendFrame = new byte[400];

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Error Introduction Options");
            alert.setHeaderText("Which of the following errors would like to introduce during sending packets?");
            alert.setContentText("Choose your option!");

            ButtonType First = new ButtonType("Bit Change");
            ButtonType Second = new ButtonType("Frame Drop");
            ButtonType Third = new ButtonType("None");

            alert.getButtonTypes().setAll(First, Second, Third);

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == First){
                //System.out.println("Bit change");
                errorString = "Bit";
            } else if (result.get() == Second) {
                //System.out.println("Frame Drop");
                errorString = "Frame";

            } else if (result.get() == Third) {
               // System.out.println("None clicked");
                errorString = "None";

            }

            oos.writeObject("SendFile");
            oos.writeObject(allowed_chunk_size);
            receiver_id = ReceiverId.getText().toString();

            oos.writeObject(receiver_id);

            String auth = ois.readObject().toString();

            if (auth.equals("Granted")) {

                File curr_file = new File(file_path);
                fis = new FileInputStream(curr_file);
                bis = new BufferedInputStream(fis);


                byte[] content;
                int fileLen = (int) curr_file.length(); //total size of file to be sent

                int curr_size = 0; //current size of amount of sent file

                if(errorString == "None") {

                   // System.out.println("No of iterations: "+ (fileLen / (allowed_chunk_size - 7)));
                    int noOfIteration = fileLen / (allowed_chunk_size - 7);

                    if((fileLen%(allowed_chunk_size - 7))!=0){
                        noOfIteration +=1;
                    }
                    oos.writeObject(noOfIteration);

                    main.fileNo = 0;

                    while (curr_size != fileLen) {


                        main.sharedVar = -1;
                        main.permit = 0;
                        new ThreadForRead(this, socket, ois, oos, main, noOfIteration, 1);

                        int times = 1;

                        posResendFrame = 0;
                        while (true) {
                            int size = allowed_chunk_size - 7;
                            if (fileLen - curr_size >= size) {
                                curr_size += size;
                            } else {
                                size = (int) (fileLen - curr_size);
                                curr_size = fileLen;
                            }
                            content = new byte[size];
                            bis.read(content, 0, size);

                            System.out.println("Frame before stuffing");
                            System.out.print("01111110 ");
                            System.out.print("00000001 ");
                            System.out.print("00000000 ");
                            printbits((byte)times);
                            for(int bb = size-1;bb>=0;bb--){
                                printbits(content[bb]);
                            }

                            checksum = 0;
                            byte[] mod_payload = bitStuffing(content, size);

                            printbits((byte)checksum);
                            System.out.print("01111110 ");
                            System.out.println();

                            byte[] Frame = new byte[4 + sizeOfModPayload]; //4 bytes for header, type of frame, seqNo, acqNo
                            sizeOfFrame = 4 + sizeOfModPayload;

                            Frame[sizeOfFrame - 1] = 0b01111110; // Header's unique bit sequence
                            Frame[sizeOfFrame - 2] = 0b00000001; // Data Frame(to be worked on!)
                            Frame[sizeOfFrame - 3] = 0b00000000; // sequence no of Frame being sent
                            Frame[sizeOfFrame - 4] = (byte) (times); // ack no which I have modified to be the frame no

                            for (int kk = 0; kk < sizeOfModPayload; kk++) {
                                Frame[kk] = mod_payload[kk];
                            }

                            System.out.println("Frame after stuffing");
                            for(int bb = sizeOfFrame-1;bb>=0;bb--){
                                printbits(Frame[bb]);
                            }
                            System.out.println();

                            //System.out.println("frame size " + sizeOfFrame);
                            oos.write(Frame);
                            oos.flush();

                            //CopyFrame(Frame, sizeOfFrame);


                            if (times == 8) break;
                            if (curr_size == fileLen) break;
                            times++;
                        }



                        //System.out.println("File sizes " + curr_size + " " + fileLen);


                        while (main.permit == 0) {
                            Thread.sleep(1000);
                        }

                        //System.out.println("Shared: " + main.sharedVar);


                        times = 1;


                    }


                    //ois.read(dummy, 0, 8);
                    //ois.readObject();
                    //System.out.println("Bit success");


                }
                else if(errorString == "Bit"){

                    int noOfIteration = fileLen / (allowed_chunk_size - 7);

                    if((fileLen%(allowed_chunk_size - 7))!=0){
                        noOfIteration +=1;
                    }
                    oos.writeObject(noOfIteration);

                    //System.out.println("No of iterations: "+ noOfIteration);


                    main.fileNo = 0;

                    boolean change = true;

                    while (curr_size != fileLen) {

                        //System.out.println("File No: "+main.fileNo);

                        main.sharedVar = -1;
                        main.permit = 0;
                        new ThreadForRead(this, socket, ois, oos, main, noOfIteration, 1);

                        int times = 1;

                        markers = new ArrayList();

                        posResendFrame = 0;

                        int resend = 0;

                        while (true) {
                            int size = allowed_chunk_size - 7;
                            if (fileLen - curr_size >= size) {
                                curr_size += size;
                            } else {
                                size = (int) (fileLen - curr_size);
                                curr_size = fileLen;
                            }
                            content = new byte[size];
                            bis.read(content, 0, size);

                            System.out.println("Frame before stuffing");
                            System.out.print("01111110 ");
                            System.out.print("00000001 ");
                            System.out.print("00000000 ");
                            printbits((byte)times);
                            for(int bb = size-1;bb>=0;bb--){
                                printbits(content[bb]);
                            }


                            checksum = 0;
                            byte[] mod_payload = bitStuffing(content, size);

                            printbits((byte)checksum);
                            System.out.print("01111110 ");
                            System.out.println();

                            byte[] Frame = new byte[4 + sizeOfModPayload]; //4 bytes for header, type of frame, seqNo, acqNo
                            sizeOfFrame = 4 + sizeOfModPayload;

                            Frame[sizeOfFrame - 1] = 0b01111110; // Header's unique bit sequence
                            Frame[sizeOfFrame - 2] = 0b00000001; // Data Frame(to be worked on!)
                            Frame[sizeOfFrame - 3] = 0b00000000; // sequence no of Frame being sent
                            Frame[sizeOfFrame - 4] = (byte) (times); // ack no which I have modified to be the frame no

                            for (int kk = 0; kk < sizeOfModPayload; kk++) {
                                Frame[kk] = mod_payload[kk];
                            }

                            CopyFrame(Frame, sizeOfFrame);

                            System.out.println("Frame after stuffing");
                            for(int bb = sizeOfFrame-1;bb>=0;bb--){
                                printbits(Frame[bb]);
                            }
                            System.out.println();

                            if(change && times == 3) {
                                Frame = ChangeBit(Frame,sizeOfFrame);
                                change = false;

                                System.out.println("Frame after bit drop");
                                for(int bb = sizeOfFrame-1;bb>=0;bb--){
                                    printbits(Frame[bb]);
                                }
                                System.out.println();
                            }



                            //System.out.println("Sent size "+sizeOfFrame + " "+curr_size );

                            oos.write(Frame);
                            oos.flush();



                            if (times == 8) break;
                            if (curr_size == fileLen) break;
                            times++;
                        }



                        //System.out.println("File sizes " + curr_size + " " + fileLen);


                        while (main.permit == 0) {
                            Thread.sleep(1000);
                        }

                       // System.out.println("Shared: " + main.sharedVar + " "+ times);

                        if(main.sharedVar != times){

                            FuncResendFrame(main.sharedVar, times, noOfIteration, (resend+1));

                        }



                    }

                    byte[] dummy = new byte[10];
                    //ois.read(dummy, 0, 8);

                    //System.out.println("Bit success");


                }
                else if(errorString == "Frame"){

                   // System.out.println("No of iterations: "+ (fileLen / (allowed_chunk_size - 7)));
                    int noOfIteration = fileLen / (allowed_chunk_size - 7);

                    if((fileLen%(allowed_chunk_size - 7))!=0){
                        noOfIteration +=1;
                    }
                    oos.writeObject(noOfIteration);

                    //System.out.println("No of iterations: "+ noOfIteration);

                    main.fileNo = 0;

                    boolean change = true;

                    while (curr_size != fileLen) {

                        main.sharedVar = -1;
                        main.permit = 0;
                        new ThreadForRead(this, socket, ois, oos, main, noOfIteration, 1);

                        int times = 1;

                        markers = new ArrayList();

                        posResendFrame = 0;

                        int resend = 0;

                        while (true) {
                            int size = allowed_chunk_size - 7;
                            if (fileLen - curr_size >= size) {
                                curr_size += size;
                            } else {
                                size = (int) (fileLen - curr_size);
                                curr_size = fileLen;
                            }
                            content = new byte[size];
                            bis.read(content, 0, size);

                            System.out.println("Frame before stuffing");
                            System.out.print("01111110 ");
                            System.out.print("00000001 ");
                            System.out.print("00000000 ");
                            printbits((byte)times);
                            for(int bb = size-1;bb>=0;bb--){
                                printbits(content[bb]);
                            }

                            checksum = 0;
                            byte[] mod_payload = bitStuffing(content, size);

                            printbits((byte)checksum);
                            System.out.print("01111110 ");
                            System.out.println();

                            byte[] Frame = new byte[4 + sizeOfModPayload]; //4 bytes for header, type of frame, seqNo, acqNo
                            sizeOfFrame = 4 + sizeOfModPayload;

                            Frame[sizeOfFrame - 1] = 0b01111110; // Header's unique bit sequence
                            Frame[sizeOfFrame - 2] = 0b00000001; // Data Frame(to be worked on!)
                            Frame[sizeOfFrame - 3] = 0b00000000; // sequence no of Frame being sent
                            Frame[sizeOfFrame - 4] = (byte) (times); // ack no which I have modified to be the frame no

                            for (int kk = 0; kk < sizeOfModPayload; kk++) {
                                Frame[kk] = mod_payload[kk];
                            }

                            CopyFrame(Frame, sizeOfFrame);

                            System.out.println("Frame after stuffing");
                            for(int bb = sizeOfFrame-1;bb>=0;bb--){
                                printbits(Frame[bb]);
                            }
                            System.out.println();

                            //System.out.println("frame size " + sizeOfFrame);

                            if(change && times == 3){
                                change = false;

                                System.out.println("Frame Dropped");
                                for(int bb = sizeOfFrame-1;bb>=0;bb--){
                                    printbits(Frame[bb]);
                                }
                                System.out.println();

                            }
                            else{
                                oos.write(Frame);
                                oos.flush();
                            }

                            if (times == 8) break;
                            if (curr_size == fileLen) break;
                            times++;
                        }



                        //System.out.println("File sizes " + curr_size + " " + fileLen);


                        while (main.permit == 0) {
                            Thread.sleep(1000);
                        }

                        //System.out.println("Shared: " + main.sharedVar + " "+ times);

                        if(main.sharedVar != times){

                            FuncResendFrame(main.sharedVar, times, noOfIteration, (resend+1));

                        }


                        times = 1;


                    }

                    byte[] dummy = new byte[10];
                    //ois.read(dummy, 0, 8);
                    //ois.readObject();
                    //System.out.println("Bit success");


                }

                if (curr_size == fileLen) {
                    oos.writeObject("Completely Sent");


                    String msg = ois.readObject().toString();
                    //System.out.println("After file tx "+msg);
                    if (msg.equals("SuccessMsg")) {
                        SendText.setText("File Sent Successfully!");
                    } else if (msg.equals("FailureMsg")) {
                        SendText.setText("File could not be sent successfully");
                    }

                } else {
                    oos.writeObject("Deleted");
                    SendText.setText("File could not be sent");
                }

                oos.flush();
                bis.close();
                fis.close();

                FileName.setText("");

                SendBtn.setVisible(false);
            } else if (auth.equals("Not Granted")) {
                SendText.setText("Receiver is offline. Cannot send file");
            }
        }catch (SocketTimeoutException ss){
            //oos.writeObject("Terminate");
            ss.printStackTrace();

            oos.writeObject("Deleted");
            SendText.setText("File could not be sent");

            oos.flush();
            bis.close();
            fis.close();

            FileName.setText("");
            SendBtn.setVisible(false);

        }
        catch (Exception e){
            System.out.println("Unexpected Problem in Server!");
            e.printStackTrace();
            try {
                CloseConnection1();
                CloseConnection2();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }

    }


    public void CopyFrame(byte[] Frame, int size){

        for(int i=0;i<size;i++){
            ResendFrame[posResendFrame] = Frame[i];
            posResendFrame++;
        }
        markers.add(posResendFrame - 1); //marksers will contain those value until which i must read to get each frame distinctly

    }

    public void FuncResendFrame(int shared, int times, int iterations, int resend) throws Exception{

        main.permit = 0;

        int c = shared + 1;

        int start, end;
        start = (int) markers.get(shared - 1) + 1; //markers(shared-1) = indicates the start of 3rd frame | markers(shared) indicated end of 3rd frame
        end = (int) markers.get(shared);

        new ThreadForRead(this, socket, ois, oos, main, iterations, shared+1);

        for(; c<=times ; c++) { //will send all the unreceived Frames again

            byte[] Frame = new byte[end - start + 1];
            int framesize = end-start+1;
            int i;
            for (i = 0; start <= end; start++, i++) {
                Frame[i] = ResendFrame[start];

            }

            Frame[i-3] = (byte) resend;

            oos.write(Frame);
            oos.flush();

            System.out.println("Resending Frame");
            for(int jj=framesize-1;jj>=0;jj--){
                printbits(Frame[jj]);
            }
            System.out.println();

            start = (int) markers.get(shared) + 1;
            shared++;
            if(c != times) end = (int) markers.get(shared);


        }

        while(main.permit == 0){
            Thread.sleep(1000);
        }

        //System.out.println("After resend: "+main.sharedVar + " " + posResendFrame );





    }

    public byte[] ChangeBit(byte[] Frame, int size){

        int mask = 4; //trying to change the 3rd bit from LSB

        if((mask & Frame[size-5])!=0){
            mask = ~mask;
            mask = mask & 0xff;
            Frame[size-5] = (byte) (Frame[size-5] & mask);
        }
        else{
            mask = mask & 0xff;
            Frame[size-5] = (byte) (Frame[size-5] | mask);
        }


        return Frame;
    }




    public byte[] bitStuffing(byte[] payload, int ss){

        //for(int i=ss-1;i>=0;i--){
            //printbits(payload[i]);
        //}

        int count = 0;
        int noOfGroups = 0;
        int mask=128;

        for(int i=ss-1; i>=0; i--){
            byte pp = payload[i];
            mask=128;

            for(int j=0; j<8; j++){
                if((mask & pp)!=0){
                    count++;
                }
                else count = 0;

                if(count==5){
                    noOfGroups++;
                    count = 0;
                }
                mask = mask>>1;
            }
        }
        int rem = noOfGroups%8;
        int tobeadded = noOfGroups/8;
        if(rem!=0) tobeadded+=1;

        //int newsize = ss + tobeadded + 2; //2 is added for accomodating checksum and trailer
        int newsize = ss + tobeadded + 3; //3 is added for accomodating checksum(with stuffing) and trailer

        byte mod_payload[] = new byte[newsize]; //newsize contains size of modified payload
        int counter_payload = (newsize*8) - 1;

        for(int i=ss-1;i>=0;i--){
            byte pp = payload[i];
            mask = 128;

            for(int j=0; j<8; j++){

                if((mask & pp)!=0){
                    count++;
                    int y = counter_payload%8; //finding exact location in a specific byte to add 1
                    int yy = 1<<y; // shifting 1 y no of bits to append 1
                    int z = counter_payload/8; // finding the specific numbered byte to change

                    mod_payload[z] = (byte) (mod_payload[z] | yy);

                    counter_payload--;
                    checksum++;
                }
                else{
                    count = 0;

                    int y = counter_payload%8; //finding exact location in a specific byte to add 1
                    int yy = 1<<y; // shifting 1 y no of bits to append 1
                    yy = ~yy;
                    yy = yy & 0xff; //extracting only the 8 LSB bits
                    int z = counter_payload/8; // finding the specific numbered byte to change

                    mod_payload[z] = (byte) (mod_payload[z] & yy);

                    counter_payload--;
                }



                if(count==5){

                    int y = counter_payload%8; //finding exact location in a specific byte to add 1
                    int yy = 1<<y; // shifting 1 y no of bits to append 1
                    yy = ~yy;
                    yy = yy & 0xff; //extracting only the 8 LSB bits
                    int z = counter_payload/8; // finding the specific numbered byte to change

                    mod_payload[z] = (byte) (mod_payload[z] & yy);

                    count = 0;
                    counter_payload--;
                }

                mask = mask>>1;
            }

        }

        /*for(int i=ss-1;i>=0;i--){
            printbits(payload[i]);
        }
        System.out.println("");

        for(int i=newsize-1;i>=0;i--){
            printbits(mod_payload[i]);
        }
        System.out.println("");
        */


        Integer ii = new Integer(checksum);
        byte checksumbytes = ii.byteValue();
        //System.out.println("Checksum");
        //printbits(checksumbytes);
        byte trailer = 0b01111110;

        count = 0;
        mask=128;
        for(int i=0;i<8;i++){
            if((mask & checksumbytes)!=0){
                int y = counter_payload%8; //finding exact location in a specific byte to add 1
                int yy = 1<<y; // shifting 1 y no of bits to append 1
                int z = counter_payload/8; // finding the specific numbered byte to change

                mod_payload[z] = (byte) (mod_payload[z] | yy);

                counter_payload--;
                count++;
            }
            else{
                int y = counter_payload%8; //finding exact location in a specific byte to add 1
                int yy = 1<<y; // shifting 1 y no of bits to append 1
                yy = ~yy;
                yy = yy & 0xff; //extracting only the 8 LSB bits
                int z = counter_payload/8; // finding the specific numbered byte to change

                mod_payload[z] = (byte) (mod_payload[z] & yy);

                counter_payload--;
                count = 0;
            }

            if(count == 5){
                int y = counter_payload%8; //finding exact location in a specific byte to add 1
                int yy = 1<<y; // shifting 1 y no of bits to append 1
                yy = ~yy;
                yy = yy & 0xff; //extracting only the 8 LSB bits
                int z = counter_payload/8; // finding the specific numbered byte to change

                mod_payload[z] = (byte) (mod_payload[z] & yy);

                counter_payload--;
                count = 0;

            }

            mask = mask >> 1;

        }

        mask=128;
        for(int i=0;i<8;i++){
            if((mask & trailer)!=0){
                int y = counter_payload%8; //finding exact location in a specific byte to add 1
                int yy = 1<<y; // shifting 1 y no of bits to append 1
                int z = counter_payload/8; // finding the specific numbered byte to change

                mod_payload[z] = (byte) (mod_payload[z] | yy);

                counter_payload--;
            }
            else{
                int y = counter_payload%8; //finding exact location in a specific byte to add 1
                int yy = 1<<y; // shifting 1 y no of bits to append 1
                yy = ~yy;
                yy = yy & 0xff; //extracting only the 8 LSB bits
                int z = counter_payload/8; // finding the specific numbered byte to change

                mod_payload[z] = (byte) (mod_payload[z] & yy);

                counter_payload--;
            }

            mask = mask >> 1;

        }
        /*
        for(int i=ss-1;i>=0;i--){
            printbits(payload[i]);
        }
        System.out.println("");

        for(int i=newsize-1;i>=0;i--){
            printbits(mod_payload[i]);
        }
        System.out.println("");
        */
        sizeOfModPayload = newsize;
        endOfModPayload = counter_payload;
        return mod_payload;


    }

    public void printbits(byte one){
        int mask=128;
        for(int i=0;i<8;i++){
            if((mask & one)!=0){
                System.out.print("1");
            }
            else System.out.print("0");
            mask = mask >> 1;
        }
        System.out.print(" ");

    }



    @FXML
    public void handleCloseButtonAction(ActionEvent event) throws IOException {

        //oos.writeObject("Close Connection");
        //oos2.writeObject("Close Connection");

        CloseConnection1();
        CloseConnection2();
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }





    public void CloseConnection1() throws IOException {

        ois.close();
        oos.close();

    }

    public void CloseConnection2() throws IOException {

        ois2.close();
        oos2.close();

    }

    public void ShowAlert() throws Exception {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Fail Message");
        alert.setHeaderText(null);
        alert.setContentText("Login has failed! Please check Student Id ");

        alert.showAndWait();

        main.showLoginPage();

    }






}
