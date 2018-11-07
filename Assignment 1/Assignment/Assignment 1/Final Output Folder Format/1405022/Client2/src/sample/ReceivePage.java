package sample;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.Initializable;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Scanner;

/**
 * Created by DELL on 22-Sep-17.
 */
public class ReceivePage implements Runnable {

    private SecondPage secondPage;

    private String mfile;
    private int msize;


    public ReceivePage(SecondPage sp) throws Exception {
        secondPage = sp;

        String studentId = secondPage.main.getStudentId();

        Thread t = new Thread(this);
        t.start();


    }




    @Override
    public void run() {


        try{
            while (true){
                //secondPage.oos2.writeObject(studentId);
                //System.out.println("In this");
                String msg = secondPage.ois2.readObject().toString();
                if(msg.equals("ReceiveFileFromServer")){
                    secondPage.oos2.writeObject("SendFileToClient");


                    String file = secondPage.ois2.readObject().toString();
                    String sender = secondPage.ois2.readObject().toString();
                    int size = (int) secondPage.ois2.readObject();

                    mfile = file;
                    msize = size;


                    secondPage.SenderId.setText(sender);
                    secondPage.ReceiverFileName.setText(file);
                    secondPage.ReceiverFileSize.setText(Integer.toString(size));
                    secondPage.ReceiveText.setText("Do you want to receive file?");
                    secondPage.YesOrNo.setText("Type (y/n) in console");

                    System.out.println("Do you want to receive file?(y/n) ");
                    Scanner sc = new Scanner(System.in);
                    String ver = sc.next();

                    if(ver.equals("y")){
                        try {
                            secondPage.oos2.writeObject("YES");

                            secondPage.ReceiveText.setText("");

                            File newfile = new File(file);


                            byte[] contents = new byte[1000000];
                            FileOutputStream fos = new FileOutputStream(newfile);
                            BufferedOutputStream bos = new BufferedOutputStream(fos);

                            int bytesRead = 0; //number of bytes read in this transaction
                            int sum = 0; // total size of received file at this moment
                            int sizeofchunk = 0; // checks whether amount of data received in one transaction is complete or not
                            while(sum < size) {
                                sizeofchunk = 0;

                                while (sizeofchunk != 10000)
                                {
                                    bytesRead = secondPage.ois2.read(contents,0,100000);
                                    if(bytesRead == -1) break;

                                    //clientThread.oos.writeObject("Received");
                                        /*String st = (String) clientThread.ois.readObject();
                                        if(st.equals("Terminate")){
                                            break;
                                        }*/

                                        sizeofchunk += bytesRead;
                                        bos.write(contents, 0, bytesRead);

                                        if(sum+sizeofchunk == size) break;
                                        //System.out.println(bytesRead+" "+allowedchunksize);


                                    }
                                    secondPage.oos2.writeObject("ReceivedChunk");
                                    bos.flush();
                                    //secondPage.oos2.writeObject("Received");

                                    /*String rec =  clientThread.ois.readObject().toString();
                                    if(rec.equals("Terminate")){
                                        newfile.delete();

                                        break;
                                    }*/

                                    System.out.println("Recv");
                                    sum += sizeofchunk;
                                    if(sum == size) break;

                                }
                                bos.flush();
                                bos.close();
                                fos.close();

                                secondPage.SenderId.setText("");
                                secondPage.ReceiverFileName.setText("");
                                secondPage.ReceiverFileSize.setText("");
                                secondPage.ReceiveText.setText("File Received Successfully!");

                            secondPage.YesOrNo.setText("");




                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                    }
                    else if(ver.equals("n")){
                        try {
                            secondPage.oos2.writeObject("NO");


                            secondPage.SenderId.setText("");
                            secondPage.ReceiverFileName.setText("");
                            secondPage.ReceiverFileSize.setText("");
                            secondPage.ReceiveText.setText("File has been denied");

                            secondPage.YesOrNo.setText("");

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }




                }



            }
        }
        catch (Exception e) {
            //e.printStackTrace();
            System.out.println("Unexpected Problem in Server!");

            try {
                secondPage.CloseConnection2();
                secondPage.CloseConnection1();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }

    }


}
