package sample;

import java.io.*;
import java.net.Socket;

/**
 * Created by DELL on 22-Sep-17.
 */
public class SendToClient implements Runnable {

    private Main main;
    private SecondPage secondPage;

    public Socket socket;
    public ObjectOutputStream oos;
    public ObjectInputStream ois;

    private String StudentId;

    private String mfilename;
    private int msize;

    private FileInputStream fis;
    private BufferedInputStream bis;

    private String path;


    public SendToClient(Main m, Socket sc, SecondPage sp) throws Exception {
        main = m;
        secondPage = sp;

        path = "E:\\Networking Server 2\\Server2\\src";

        socket = sc;
        oos = new ObjectOutputStream(socket.getOutputStream());
        ois = new ObjectInputStream(socket.getInputStream());


        StudentId = ois.readObject().toString();
        if(!secondPage.StudentIds2.contains(StudentId)){
            oos.writeObject("Successful Login");
            System.out.println("Successful Login");
            secondPage.StudentIds2.add(StudentId);

            secondPage.mapoutput.put(StudentId,oos);
            secondPage.mapinput.put(StudentId,ois);

            StartThread();

        }
        else{
            oos.writeObject("Failure");
            System.out.println("Failure Login");
            CloseConnection();
        }



    }

    private void CloseConnection() throws IOException {

        oos.close();
        ois.close();

    }

    public void StartThread(){
        Thread t = new Thread(this);
        t.start();
    }




    @Override
    public void run() {
        while(true) {
            try {
                System.out.println("Scanning");
                File folder = new File("E:\\Networking Server 2\\Server2\\src");
                int flag = 0;
                String name = "";
                int sizes = 0;
                while (true) {
                    File[] listOfFiles = folder.listFiles();
                    Thread.sleep(1000);

                    for (int i = 0; i < listOfFiles.length; i++) {
                        if (listOfFiles[i].isFile()) {
                            //System.out.println("File: " + listOfFiles[i].getName());
                            name = listOfFiles[i].getName();
                            String receiver[] = name.split("__");
                            //System.out.println("length: "+receiver.length);
                            if (receiver.length == 4) {
                                if (receiver[2].equals(StudentId)) {
                                    sizes = (int) listOfFiles[i].length();
                                    flag = 1;
                                    System.out.println("Break");
                                    break;
                                }
                            }
                        }
                        /*else if(listOfFiles[i].isDirectory()){
                            System.out.println("Directory");
                        }*/
                    }
                    if (flag == 1) {
                        break;
                    }
                }
                if (flag == 1) {



                        oos.writeObject("ReceiveFileFromServer");
                        String msg = ois.readObject().toString();
                        if (msg.equals("SendFileToClient")) {

                            String filename = name;
                            String rr[] = filename.split("__");
                            String sender = rr[1];
                            String file = rr[3];
                            int size = sizes;

                            mfilename = filename;
                            msize = size;

                            oos.writeObject(file);
                            oos.writeObject(sender);
                            oos.writeObject(size);

                            //System.out.println(file + " " + sender + " " + size);

                            String verdict = ois.readObject().toString();
                            if (verdict.equals("YES")) {

                                System.out.println("YES TX");

                                File curr_file = new File(path+"\\"+filename);
                                fis = new FileInputStream(curr_file);
                                bis = new BufferedInputStream(fis);


                                byte[] content;
                                int fileLen = (int) curr_file.length();

                                int curr_size = 0;
                                while (curr_size != fileLen) {
                                    int ss = 10000;
                                    if (fileLen - curr_size >= ss) {
                                        curr_size += ss;
                                    } else {
                                        ss = (int) (fileLen - curr_size);
                                        curr_size = fileLen;
                                    }
                                    content = new byte[ss];
                                    bis.read(content, 0, ss);
                                    oos.write(content);
                                    oos.flush();

                                    System.out.println("Sent");

                                    String msg2 = ois.readObject().toString();
                                    if(!msg2.equals("ReceivedChunk")){
                                        break;
                                    }

                                    /*long start = System.currentTimeMillis();
                                    String ack_msg = ois.readObject().toString();
                                    long end = System.currentTimeMillis();
                                    System.out.println("Acknowledgement: " + ack_msg);
                                    if ((end - start) > (3 * 1000)) {
                                        oos.writeObject("Terminate");
                                        break;
                                    } else {
                                        oos.writeObject("Continue");
                                    }*/

                                }
                                oos.flush();
                                bis.close();
                                fis.close();

                                File ff = new File(path+"\\"+filename);
                                ff.delete();
                                main.setPresentBufferSize(main.getPresentBufferSize() - size);


                            } else if (verdict.equals("NO")) {
                                System.out.println("NO TX");

                                File ff = new File(path+"\\"+filename);
                                System.out.println("filename is "+filename);
                                ff.delete();
                                main.setPresentBufferSize(main.getPresentBufferSize() - size);

                            }

                        }





                }


            } catch (Exception e) {
                //e.printStackTrace();
                System.out.println("Inside send to client try");

                try {
                    File ff = new File(path+"\\"+mfilename);
                    if(ff.exists()) {
                        oos.flush();
                        bis.close();
                        fis.close();

                        ff.delete();
                    }
                    main.setPresentBufferSize(main.getPresentBufferSize() - msize);
                } catch (IOException e1) {
                    System.out.println("Deleted file in server.");
                }



            }
        }




    }
}
