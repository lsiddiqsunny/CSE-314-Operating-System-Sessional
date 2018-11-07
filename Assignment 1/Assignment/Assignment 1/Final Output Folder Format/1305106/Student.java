import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

import static javax.swing.JFileChooser.*;

public class Student extends Component {
    private Socket socket = null;
    private boolean isConnected = false;
    private ObjectInputStream objectInputStream;
    private ObjectOutputStream outputStream = null;


    File dstFile = null;
    FileOutputStream fileOutputStream = null;
    //File yourFolder;
    File file;

    int roll;
    File yourFolder;
    private String sourceDirectory ;
    int portNumber;

    ArrayList<String> a=new ArrayList<>();

    private String destinationDirectory ;
    private int fileCount = 0;
    NetworkUtil nc;
    static int flag=0;
    String serv;
    public Student() {

        sourceDirectory="D://codes";



    }

    public void connect() {
        //while (!isConnected) {
        String serv = JOptionPane.showInputDialog("Enter Server Address:", null);
        //serverAddress = Integer.parseInt(s1);

        //String s1 = JOptionPane.showInputDialog("Enter Port Number:", null);
        //portNumber = Integer.parseInt(s1);

        String s2 = JOptionPane.showInputDialog("Enter  Roll Number:", null);
        roll = Integer.parseInt(s2);
        portNumber =50;

        try {
            socket = new Socket(serv, portNumber);
            nc=new NetworkUtil(socket);
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            objectInputStream=new ObjectInputStream(socket.getInputStream());

            nc.write(2);
            System.out.println("written");




        } catch (IOException e) {
            e.printStackTrace();
        }
        //}
    }

    public void sendRoll(){


        nc.write(roll);


    }
    public void getfile() throws IOException {

        while(true){

            String j=(String) nc.read();

            int tt= (int) nc.read();


        file=new File(sourceDirectory+File.separator+roll);
        file.mkdir();
        String outputFile =  file.getAbsolutePath()+File.separator+j;
        dstFile = new File(outputFile);
        fileOutputStream = new FileOutputStream(dstFile);
        BufferedOutputStream bos = new BufferedOutputStream(fileOutputStream);





        int bytesRead = 0;
        int total = 0;
        byte[] contents = new byte[1024];
        //int r=(int)nc.read();

        for(int i=0;i<tt;i++)
        {

            try{
                contents=(byte[])objectInputStream.readObject();


            }catch(IOException e){

                System.out.println("This is from user run function");
            } catch (ClassNotFoundException ex){
                System.err.println("YOUR PROBLEM WAS HERE...");
            }
            outputStream.writeObject("received");
            outputStream.flush();
            //total+=contents.length;
            fileOutputStream.write(contents);
        }
        bos.flush();

        fileOutputStream.flush();
        fileOutputStream.close();}

    }



    public static void main(String[] args) throws IOException {
        Student student = new Student();
        student.connect();
        student.sendRoll();
        student.getfile();

    }
}