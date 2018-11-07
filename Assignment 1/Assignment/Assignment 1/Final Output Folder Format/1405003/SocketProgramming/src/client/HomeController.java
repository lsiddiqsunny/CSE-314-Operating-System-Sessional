package client;

import ByteCalculation.Bytes;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;

import util.NetworkUtil;

import java.io.*;
import java.util.ArrayList;
import Helper.ByteAsObject;

import static java.lang.Math.min;


/**
 * Created by user on 9/19/2017.
 */
public class HomeController {

    private NetworkUtil nc;
    private File file;

    @FXML
    private TextField receiverId;

    @FXML
    private Label warning;

    public void chooseFile(ActionEvent event){

        FileChooser fc = new FileChooser();
        file = fc.showOpenDialog(null);
    }

    public void initData(NetworkUtil nc){

        this.nc = nc;

    }

    public void sendFile(ActionEvent event){
        int receiver = Integer.parseInt(receiverId.getText().toString());
        nc.write(receiver);
        String msg = (String)nc.read();
        if(msg.equals("u")){
            warning.setText("receiver not found");
        }
        else {
            warning.setText("");
            long fileSize = file.length();
            nc.write(fileSize);
            msg = (String)nc.read();
            if(msg.equals("u")){
                warning.setText("Main doesn't have space");
            }
            else{
                nc.write(file.getName());

                long chunkSize = (long)nc.read();


                int count;
                byte[] bytes = new byte[(int)chunkSize];

                try {
                    FileInputStream fin = new FileInputStream(file.getAbsoluteFile());
                    //byte[] fullFile = new byte[(int)fileSize];
                    //fin.read(fullFile);
                    int itr = (int) (fileSize / chunkSize);
                    if (fileSize % chunkSize != 0) itr++;
                    int frameCount = 1,ind = 0;
                    //System.out.println(itr);
                    while (itr > 0) {
                        //System.out.println("huhaha");
                        //FileOutputStream fos = new FileOutputStream("E:\\miu.txt");
                        int curr = min(itr, 10);
                        itr -= curr;
                        TimeCountThread thr = new TimeCountThread(nc,curr,frameCount);
                        int temp = curr , tempAgain = frameCount;
                        ArrayList arr = new ArrayList();
                       // ArrayList size = new ArrayList();
                        while (true) {
                            curr--;
                            //while ((count = fin.read(bytes)) > 0) {
                            count = fin.read(bytes);
                            System.out.println("Chunk read");
                            Bytes.print(bytes);

                            //System.out.println("haha");
                            /*int prev = ind;
                            ind = ind + (int)chunkSize;
                            count = (int)chunkSize;
                            if(ind>fileSize){
                                ind = (int)fileSize;
                                count = ind - prev;
                            }
                            System.out.println(prev + " " + ind);
                            int kk=0;
                            byte[] bytes = new byte[count];
                            for(;prev<ind;prev++){
                                bytes[kk++]=fullFile[prev];
                                Bytes.printByte(bytes[kk-1]);
                                System.out.println();
                            }*/
                            //Bytes.print(bytes);

                            int stuffedSize = compute(bytes);
                            stuffedSize = (int)Math.ceil(stuffedSize/8.0);

                            //adding header and trailer
                            byte[] frame = new byte[count+6+stuffedSize];
                            byte header = 126;
                            /*for(int i=7;i>=0;i--){
                                if(Bytes.get(header,i)==1){
                                    //System.out.println(frame[0]);
                                    frame[0] = (byte)(frame[0] | (1<<i));
                                    //frame[count+1] = (byte)(frame[count+1] | (1<<i));
                                    //Bytes.set(frame[0],i);
                                    //Bytes.set(frame[count+1],i);
                                    //System.out.println(frame[0]);
                                }
                                else{
                                    frame[0] = (byte)(frame[0] & (~(1<<i)));
                                    //frame[count+1] = (byte)(frame[count+1] & (~(1<<i)));
                                }
                            }*/
                            frame[0] = 126;

                            frame[1] = (byte)frameCount;
                            frame[2] = (byte)1;
                            frame[3] = 0;
                            frame[4] = checkSum(bytes,count);
                            //System.out.println((int)frame[1]);
                            frameCount++;
                            //if(frameCount==5)continue;
                            int currbit = 7 , currByte = 5,flag = 0;
                            /*for(int i=1;i<count+1;i++) {
                                for (int j = 7; j >= 0; j--) {
                                    if (Bytes.get(bytes[i - 1], j) == 1) {
                                        frame[i] = (byte) (frame[i] | (byte) (1 << j));
                                    } else {
                                        frame[i] = (byte) (frame[i] & (~(1 << j)));
                                    }
                                }
                            }*/
                            for(int i=1;i<count+1;i++){
                                for(int j=7;j>=0;j--){
                                    if(Bytes.get(bytes[i-1],j)==1){
                                        flag++;
                                        frame[currByte] = (byte)(frame[currByte] | (1<<currbit));
                                        currbit--;
                                        if(currbit==-1){
                                            currByte++;
                                            currbit=7;
                                        }
                                        if(flag==5){
                                            frame[currByte] = (byte)(frame[currByte] & (~(1<<currbit)));
                                            currbit--;
                                            if(currbit==-1){
                                                currByte++;
                                                currbit=7;
                                            }
                                            flag=0;
                                        }
                                        //Bytes.set(frame[i],j);
                                    }
                                    else{
                                        frame[currByte] = (byte)(frame[currByte] & (~(1<<currbit)));
                                        currbit--;
                                        if(currbit==-1){
                                            currByte++;
                                            currbit=7;
                                        }
                                        flag = 0;
                                        //Bytes.reset(frame[i],j);
                                    }
                                }
                            }
                            for(int i=7;i>=0;i--){
                                if(Bytes.get(header,i)==1){
                                    //System.out.println(frame[0]);
                                    frame[currByte] = (byte)(frame[currByte] | (1<<currbit));
                                    currbit--;
                                    if(currbit==-1){
                                        currByte++;
                                        currbit=7;
                                    }
                                    //frame[count+1] = (byte)(frame[count+1] | (1<<i));
                                    //Bytes.set(frame[0],i);
                                    //Bytes.set(frame[count+1],i);
                                    //System.out.println(frame[0]);
                                }
                                else{
                                    //System.out.println(currByte + " " + frame.length);
                                    frame[currByte] = (byte)(frame[currByte] & (~(1<<currbit)));
                                    currbit--;
                                    if(currbit==-1){
                                        currByte++;
                                        currbit=7;
                                    }
                                    //frame[count+1] = (byte)(frame[count+1] & (~(1<<i)));
                                }
                            }
                            //System.out.println(currByte + " " + currbit + " " + frame.length);

                            //size.add(frame.length);
                            //if(curr==5)continue;
                            /*byte[] result = new byte[4];

                            result[0] = (byte) ((frame.length) >> 24);
                            result[1] = (byte) ((frame.length) >> 16);
                            result[2] = (byte) ((frame.length) >> 8);
                            result[3] = (byte) ((frame.length) >> 0);
                            Bytes.print(result);
                            nc.writeByte(result,4);
                            byte[] again = new byte[1];
                            again[0]=(byte)(8-currbit);
                            nc.writeByte(again,1);*/
                            ByteAsObject byteAsObject = new ByteAsObject(frame);
                            arr.add(byteAsObject);
                            //nc.write(frame.length);
                            //byte more = frame[1];
                            //frame[1]=(byte)1;
                            /*byte[] test = new byte[frame.length];
                            for(int i=0;i<frame.length;i++){

                                for(int j=7;j>=0;j--){
                                    if(Bytes.get(frame[i],j)==1)test[i] |= 1<<j;
                                    else test[i] &= ~(1<<j);
                                }
                            }*/
                            //nc.writeByte(frame, frame.length);

                            /*byte[] dirty = new byte[frame.length];
                            for(int i=0;i<frame.length;i++){
                                for(int j=7;j>=0;j--){
                                    if(Bytes.get(frame[i],j)==1)dirty[i] |= 1<<j;
                                    else dirty[i] &= ~(1<<j);
                                }
                            }
                            dirty[5]=100;
                            System.out.println("Header , no of Frame , no of times sent , type of the frame , checkSum , payload , trailer");
                            dirty[5] = 100;
                            Bytes.print(dirty);
                            nc.write(new ByteAsObject(dirty));*/
                            System.out.println("Header , no of Frame , no of times sent , type of the frame , checkSum , payload , trailer");
                            Bytes.print(byteAsObject.buf);
                            nc.write(byteAsObject);

                            //frame[1] = more;
                            //System.out.println(frame.length);
                            //Bytes.print(frame);
                            //System.out.println(count);



                            //System.out.println((String) nc.read());



                            //nc.write("h");

                            //System.out.println("sofol");
                            //fos.write(bytes);
                            //for(int i=0;i<1000000;i++);

                            //}
                            if(curr==0)break;
                        }
                        while(thr.flag==0){
                            System.out.printf("");
                        }
                        //System.out.println("kochu");
                        //System.out.println(thr.start + " " + temp);
                        int lifeSaver = thr.start;

                        while(lifeSaver<tempAgain + temp){
                            System.out.println(lifeSaver);
                            int bin = lifeSaver;
                            thr = new TimeCountThread(nc,tempAgain+temp-bin,bin);
                            curr = tempAgain+temp-bin;
                            //System.out.println(tempAgain+temp-bin + " " + bin);

                            while(true){
                                curr--;
                                //while ((count = fin.read(bytes)) > 0) {

                                //byte[] bytes = (byte[])arr.get(bin-tempAgain);
                                ByteAsObject byteAsObject = (ByteAsObject) arr.get(bin - tempAgain);
                                //byte dhur = bytes[5];
                                //Bytes.print(byteAsObject.buf);
                                //System.out.println(bytes.length);
                                //System.out.println((int)bytes[1]);
                                //System.out.println(bin - tempAgain);
                                bin++;
                                //if(curr==5)continue;
                                byteAsObject.buf[2]++;

                                byte[] dirty = new byte[byteAsObject.buf.length];
                                for(int i=0;i<byteAsObject.buf.length;i++){
                                    for(int j=7;j>=0;j--){
                                        if(Bytes.get(byteAsObject.buf[i],j)==1)dirty[i] |= 1<<j;
                                        else dirty[i] &= ~(1<<j);
                                    }
                                }

                                //nc.write(bytes.length);
                                //nc.writeByte(bytes, bytes.length);
                                System.out.println("Header , no of Frame , no of times sent , type of the frame , checkSum , payload , trailer");
                                Bytes.print(byteAsObject.buf);
                                nc.write(new ByteAsObject(dirty));

                                //System.out.println(curr);
                                //System.out.println(count);



                                //System.out.println((String) nc.read());



                                //nc.write("h");

                                //System.out.println("sofol");
                                //fos.write(bytes);
                                //for(int i=0;i<1000000;i++);

                                //}
                                if(curr==0)break;
                            }
                            while(thr.flag==0){
                                System.out.printf("");
                            }
                            lifeSaver = thr.start;
                        }

                    }
                    //System.out.println("ya Allah");
                    nc.write("Finished");
                    fin.close();
                }
                catch (FileNotFoundException e){

                }
                catch(IOException e){

                }
                finally {

                }
                System.out.println((String)nc.read());
            }
        }
    }
    public int compute(byte[] bytes){
        int len = bytes.length;
        int size = 0,flag=0;
        for(int i=0;i<len;i++){
            for(int j=7;j>=0;j--){
                if(Bytes.get(bytes[i],j)==1){
                    flag++;
                    if(flag==5)
                    {
                        size++;
                        flag=0;
                    }
                }
                else flag=0;
            }
        }
        return size;
    }
    public byte checkSum(byte[] temp,int count){
        byte ret = 0;
        for(int i=0;i<count;i++){

            for(int j=7;j>=0;j--){
                if(Bytes.get(temp[i],j)==1)ret = (byte)(ret ^ (1<<j));
            }
        }
        return ret;
    }
}
