package sample;

import javafx.application.Platform;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by DELL on 21-Sep-17.
 */
public class ReceiveFromClientThread implements Runnable {

    private Main main;
    private ClientThread clientThread;
    private SecondPage secondPage;

    private int totalBufferSize;
    private int presentBufferSize;
    private int allowedchunksize;

    private String file_name;
    private int size_of_file;
    private File mfile;
    private int msum;

    private FileOutputStream fos;
    private BufferedOutputStream bos;

    //Frame characterstics
    private byte dataFrame;
    private byte seqPckt;
    private byte ackPckt;
    private byte[] payload;
    private byte checksum;
    private int sizeOfPayload;
    private int exactSizePayload; //in bits
    private byte[] actualPayload;
    private int sizeOfActualPayload;

    private int noOfIteration;




    public ReceiveFromClientThread(Main m, ClientThread ct, SecondPage sp){
        main = m;
        clientThread = ct;
        secondPage = sp;

        totalBufferSize = main.getSizeOfChunks();
        presentBufferSize = main.getPresentBufferSize();

        Thread t = new Thread(this);
        t.start();

    }




    @Override
    public void run() {
        try {
            while (true) {
               // if(clientThread.socket.isConnected()) {
                    String typeoftext = clientThread.ois.readObject().toString();
                    if (typeoftext.equals("ChooseFile")) {
                        file_name = (String) clientThread.ois.readObject();
                        size_of_file = (int) clientThread.ois.readObject();
                        //System.out.println("Inside choosefile");

                        if (size_of_file + main.getPresentBufferSize() <= main.getSizeOfChunks()) {
                            //main.setPresentBufferSize(size_of_file+presentBufferSize);
                            clientThread.oos.writeObject("Allowed");
                            //System.out.println("Allowed to send file "+main.getPresentBufferSize() + " "+main.getSizeOfChunks());

                            int chunksize = randomgen(size_of_file);
                            //System.out.println("Chunk size: "+chunksize);


                            clientThread.oos.writeObject(chunksize);
                            clientThread.oos.writeObject(clientThread.StudentId);

                        } else {
                            clientThread.oos.writeObject("Not Allowed");
                            System.out.println("Not allowed to send file "+main.getPresentBufferSize() + " "+main.getSizeOfChunks());
                        }
                    }
                    else if(typeoftext.equals("SendFile")){
                        //System.out.println("Inside Sendfile");
                        //byte[] AckFrame = new byte[7];

                        allowedchunksize = (int) clientThread.ois.readObject();
                        String receiver = (String) clientThread.ois.readObject();
                        String rec = "";
                        if(secondPage.StudentIds.contains(receiver)) {
                            clientThread.oos.writeObject("Granted");

                            noOfIteration = (int) clientThread.ois.readObject();


                            //String filename = Integer.toString(main.getUniqueID()) + "_" + clientThread.StudentId + "_" + receiver + "_" + file_name;
                            //main.setUniqueID(main.getUniqueID() + 1);

                            String filename = Integer.toString(clientThread.unique) + "__" + clientThread.StudentId + "__" + receiver + "__" + file_name;
                            mfile = new File(filename);


                            byte[] contents = new byte[100];
                            byte[] Frame = new byte[10000];

                            fos = new FileOutputStream(mfile);
                            bos = new BufferedOutputStream(fos);

                            int bytesRead = 0;
                            msum = 0;
                            int sizeofchunk = 0;

                            int times = 1;
                            int hold = -1;

                            int totalcount = 0;

                            while (true) {

                                boolean accept = true;
                                boolean decision;
                                boolean decision2;

                                while (true) {

                                    bytesRead = clientThread.ois.read(Frame, 0, 1000);

                                    if (bytesRead != -1) {
                                        /*ProcessFrame(Frame, bytesRead);
                                        if(seqPckt==1){
                                            accept = true;
                                            System.out.println("Accepted");
                                        }*/

                                        System.out.println("Received Frame: ");
                                        for(int bb=bytesRead-1;bb>=0;bb--){
                                            printbits(Frame[bb]);
                                        }
                                        System.out.println();

                                    }


                                    if(accept) {
                                        ProcessFrame(Frame, bytesRead);
                                        decision = FuncCheckSum(); //returns false if checksum has error
                                        decision2 = FrameDrop(times); //false means there is error

                                        //System.out.println("Each " + (int)ackPckt+" "+times);
                                        if(!decision) System.out.println("Bit changed in frame "+ times);
                                        if(!decision2) System.out.println("Frame dropped in frame "+ times);

                                        if (decision == true && decision2 == true) {
                                            //accept = true;

                                            bos.write(actualPayload, 0, sizeOfActualPayload);
                                            //if (msum + sizeofchunk >= size_of_file) break;
                                            //System.out.println(bytesRead+" "+allowedchunksize);
                                            bos.flush();

                                            msum += sizeOfActualPayload;

                                            //System.out.println("Msum is "+msum);
                                            hold = times;
                                        }
                                        else{
                                            accept = false;
                                            if(decision2==false) {
                                                System.out.println("Frame "+times+" rejected\nSending Acknowledgement 0");

                                                byte[] AckFrame = new byte[7];

                                                AckFrame[6] = 0b01111110; //header of frame
                                                AckFrame[5] = 0b00000010; //Acknowledgement Frame type(2)
                                                AckFrame[4] = (byte) (times); //Sequence No of Frame Acknowledged
                                                //AckFrame[3] = 0b00000001; //Ack byte
                                                AckFrame[2] = 0b00000000; //payload (doesnt matter)
                                                AckFrame[1] = 0b00000000; //checksum (doesnt matter)
                                                AckFrame[0] = 0b01111110; //trailer of frame

                                                //System.out.println("Not Received "+times);
                                                AckFrame[3] = (byte) 0b00000000;

                                                clientThread.oos.write(AckFrame);
                                                clientThread.oos.flush();
                                                times++; //because in case of frame drop, one frame less will be received
                                            }
                                        }

                                        /*if(decision2 == true){
                                            //accept = true;
                                        }
                                        else{
                                            //accept = false;
                                        }*/

                                    }


                                    if(accept){ //when decision is true, send acknowledgement
                                        byte[] AckFrame = new byte[7];
                                        System.out.println("Sequence No: "+seqPckt);
                                        System.out.println("Frame "+times+" accepted\nSending Acknowledgement 1");

                                        AckFrame[6] = 0b01111110; //Header
                                        AckFrame[5] = 0b00000010; // Ack Frame
                                        AckFrame[4] = (byte) (times); //Seq no of frame
                                        //AckFrame[3] = 0b00000001; //Payload
                                        AckFrame[2] = 0b00000000; // Payload(doesnt matter)
                                        AckFrame[1] = 0b00000000; //checksum (doesnt matter)
                                        AckFrame[0] = 0b01111110; //trailer of frame

                                        //System.out.println("Received");
                                        AckFrame[3] = (byte) 0b00000001;

                                        totalcount++;
                                        clientThread.oos.write(AckFrame);
                                        clientThread.oos.flush();

                                    }
                                    else{
                                        byte[] AckFrame = new byte[7];

                                        System.out.println("Frame "+times+" rejected\nSending Acknowledgement 0");

                                        AckFrame[6] = 0b01111110; //header of frame
                                        AckFrame[5] = 0b00000010; //Acknowledgement Frame type
                                        AckFrame[4] = (byte) (times); //Sequence No of Frame Acknowledged
                                        //AckFrame[3] = 0b00000001; //Ack byte
                                        AckFrame[2] = 0b00000000; //payload (doesn't matter)
                                        AckFrame[1] = 0b00000000; //checksum (doesn't matter)
                                        AckFrame[0] = 0b01111110; //trailer of frame

                                        //System.out.println("Not Received "+times);
                                        AckFrame[3] = (byte) 0b00000000;

                                        clientThread.oos.write(AckFrame);
                                        clientThread.oos.flush();
                                    }

                                    if(times==8) break;

                                    if (msum == size_of_file) break;

                                    if(times==noOfIteration) break;

                                    times++;

                                }

                                if(hold!=-1 && hold < 8){
                                    //System.out.println("Hold: "+hold);
                                    times = hold + 1;
                                    //clientThread.oos.flush();
                                    //clientThread.oos.reset();
                                }
                                else{
                                    times = 1;
                                    hold = -1;
                                }

                                //System.out.println("Hold: "+hold);

                                if (msum == size_of_file) break;

                            }

                            //System.out.println("Total count: "+totalcount);

                            bos.flush();
                            bos.close();
                            fos.close();




                            String complete = clientThread.ois.readObject().toString();
                            //System.out.println("msum: "+msum+" "+size_of_file);
                            if (complete.equals("Completely Sent")) {
                                //System.out.println("Inside completely sent");
                                if (msum == size_of_file) {

                                    clientThread.oos.writeObject("SuccessMsg");
                                    //System.out.println("Successmsg");
                                    main.setPresentBufferSize(size_of_file + main.getPresentBufferSize());

                                    String path = "E:\\Networking Server 2\\Server2\\src"+"\\"+filename;

                                    Path from = Paths.get(filename);
                                    Path to = Paths.get(path);

                                    Files.copy(from, to, StandardCopyOption.REPLACE_EXISTING);

                                    mfile.delete();



                                } else {
                                    clientThread.oos.writeObject("FailureMsg");
                                    System.out.println("FailureMsg");
                                    mfile.delete();
                                }
                            } else if (complete.equals("Deleted")) {
                                System.out.println("File has been deleted");
                                bos.close();
                                fos.close();
                                mfile.delete();
                            }
                        }
                        else{
                            clientThread.oos.writeObject("Not Granted");
                            System.out.println("Not Granted");
                        }


                    }

               // }
               // else {
                 //   break;
                //}
            }






        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Inside receive from client try");
            try{
                System.out.println("sizes: "+msum+" "+size_of_file);
                if(msum!=size_of_file){
                    //System.out.println("in this");

                    bos.flush();
                    bos.close();
                    fos.close();
                    mfile.delete();
                }

                File folder = new File("E:\\Networking Server 2\\Server2\\src");
                File[] listOfFiles = folder.listFiles();

                for (int i = 0; i < listOfFiles.length; i++) {
                    if (listOfFiles[i].isFile()) {
                        //System.out.println("File: " + listOfFiles[i].getName());
                        String name = listOfFiles[i].getName();
                        String receiver[] = name.split("__");
                        //System.out.println("length: "+receiver.length);
                        if (receiver.length == 4) {
                            if (receiver[2].equals(clientThread.StudentId)) {
                                File ff = new File("E:\\Networking Server 2\\Server2\\src"+"\\"+name);
                                int ss = (int) ff.length();
                                main.setPresentBufferSize(main.getPresentBufferSize() - ss);
                                ff.delete();
                            }
                        }
                    }

                }





                secondPage.StudentIds.remove(clientThread.StudentId);
                secondPage.StudentIds2.remove(clientThread.StudentId);

                secondPage.mapoutput.get(clientThread.StudentId).close();
                secondPage.mapinput.get(clientThread.StudentId).close();

                secondPage.mapoutput.remove(clientThread.StudentId);
                secondPage.mapinput.remove(clientThread.StudentId);


                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        secondPage.OnlineList.getItems().remove(clientThread.StudentId);
                    }
                });

                clientThread.oos.close();
                clientThread.ois.close();
            }catch (Exception e1){
                System.out.println("Inside receive from client second try");
            }
        }


    }


    public boolean FrameDrop(int times){

        int i = ackPckt; //ackPckt actually holds the frame no
        if(i==times) return true;
        else return false;


    }





    public boolean FuncCheckSum(){

        Integer chk = new Integer(checksum);
        int cc = (int) chk;

        int cnt=0;
        int mask=1;
        for(int i=0;i<sizeOfActualPayload;i++){
            byte pp = actualPayload[i];

            for(int j=0;j<8;j++){
                mask = 1<<j;
                if((mask & pp)!=0){
                    cnt++;
                }
            }
        }

        //System.out.println("counts: "+cnt+" "+cc);

        if(cnt==cc){
            return true;
        }
        else return false;



    }

    public void ProcessFrame(byte[] Frame, int size){
        /*System.out.println("Frame");
        for(int i=size-1;i>=0;i--){
            printbits(Frame[i]);
        }*/
        //System.out.println("Frame size "+size);

        int pointer = 0; //starting to read from the end of Frame
        int bound = ((size-4)*8)-1; //bound points to the starting bit of payload

        dataFrame = Frame[size-2];
        seqPckt = Frame[size-3];
        ackPckt = Frame[size-4];

        payload = new byte[40];
        int mask=1;
        int cnt=0;
        int noofones=0;

        for(pointer=0;pointer<=bound;pointer++){

            int x = pointer%8;
            int y = pointer/8;

            if((mask & Frame[y])!=0){
                noofones++;

            }
            else{
                noofones=0;
            }

            if(noofones==6){
                break;
            }


            cnt++;
            if(cnt==8){
                mask=1;
                cnt=0;
            }
            else{
                mask = mask<<1;
            }
        }

        pointer = pointer + 2; //pointer now points to the end of checksum


        //DeStuffing done in this part

        int aa = bound; //points to the start of payload
        int bb = pointer; //points to the end of checksum
        //we have to destuff the payload and checksum

        //System.out.println("AA BB: "+aa + " "+ bb);

        ArrayList<Byte> TemPayloadandCS = new ArrayList<>();
        //ArrayList<Byte> TemChecksum = new ArrayList<>();
        byte pp = 0b00000000;

        cnt = 0;
        int mmm = 128;
        for(; aa>=bb; aa--){

            int x = aa%8;
            int y = aa/8;

            mask = 1<<x;
            if((mask & Frame[y])!=0){ //we have seen 1
                cnt++;
                pp = (byte) (pp | mmm);

            }
            else{
                cnt=0;
                int xx = mmm;
                xx = ~xx;
                xx = xx & 0xff;

                pp = (byte) (pp & xx);
            }
            if(cnt==5){
                aa--;
                cnt = 0;
            }

            if(mmm == 1){
                mmm = 256;
                TemPayloadandCS.add(pp);

                pp = 0b00000000; //clearing pp


            }
            mmm = mmm >>1;

        }
        //System.out.println();

        int s = TemPayloadandCS.size();
        sizeOfActualPayload = s - 1;
        actualPayload = new byte[sizeOfActualPayload];

        int k=1;
        for(int i=0;i<s-1;i++){
            pp = TemPayloadandCS.get(i);
            actualPayload[sizeOfActualPayload - k] = pp;
            k++;
            //printbits(pp);
        }

        checksum = TemPayloadandCS.get(s-1);

        System.out.println("Frame After DeStuffing: ");
        System.out.print("01111110 ");
        printbits(dataFrame);
        printbits(seqPckt);
        printbits(ackPckt);
        for(int cc=sizeOfActualPayload-1;cc>=0;cc--){
            printbits(actualPayload[cc]);
        }
        printbits(checksum);
        System.out.print("01111110");
        System.out.println();




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


    public int randomgen(int size){

        /*Random rand = new Random();
        int n = rand.nextInt(21);

        int aa[] = new int[23];
        for(int i=10;i<31;i++){
            aa[i-10] = size/i;
        }*/

        //return aa[n];

        int xx = size/10;
        if(xx<20) xx=20;
        else if(xx>32) xx = 32;

        return xx;
        //return 20;
    }


}
