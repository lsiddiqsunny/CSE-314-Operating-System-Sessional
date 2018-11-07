import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.math.BigInteger;
import java.net.Socket;
import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

public class Client extends JFrame implements ActionListener{

    public final static int SOCKET_PORT = 13267;      // you may change this
    public final static String SERVER = "127.0.0.1";  // localhost
    public static String FILE_TO_RECEIVED = null;
    public  static int FILE_SIZE = 70000000; // file size temporary hard coded
    public  static String FILE_TO_SEND = null;  // you may change this
    public static int id;
    public static int choice;
    public static int FileChooserConnected=1;
    public static int btnChooseClicked=0;



    public static JLabel jLogLabel,jSendIdLabel,fileIdLabel;
    public static JTextField userField,jSendIdField;
    public static JTextArea jTextArea,jLogMessage;
    public static JTextArea fileAddressField;
    public static JButton btnConnect,btnSend,btnReceive,btnRefresh,btnChoose,btnVerifyFileId;
    public static JSeparator jSeparator;
    public  static JComboBox fileIdField;




    public static FileChooserDemo fileChooserDemo=null;
    public static SendFileGui sendFileGui = null;
    public static JFrame frame=null;


    public Client(){
        id = -1;
        choice = -1;




        btnConnect=new JButton("Connect");
        btnConnect.addActionListener(this);
        btnConnect.setEnabled(true);
        btnSend=new JButton("Send");
        btnSend.addActionListener(this);
        btnSend.setEnabled(false);
        btnChoose=new JButton("Choose File");
        btnChoose.addActionListener(this);
        btnChoose.setEnabled(false);
        btnReceive=new JButton("Receive");
        btnReceive.addActionListener(this);
        btnReceive.setEnabled(false);
        btnRefresh=new JButton("Refresh");
        btnRefresh.addActionListener(this);
        btnRefresh.setEnabled(false);
        btnVerifyFileId=new JButton("Verify File Id");
        btnVerifyFileId.addActionListener(this);
        btnVerifyFileId.setEnabled(false);


        jLogLabel = new JLabel("Log Message");
        fileIdLabel = new JLabel("Enter file id to receive");
        jSendIdLabel = new JLabel("Enter Id to send file");


        userField = new JTextField("User Id",5);
        jSendIdField = new JTextField(5);
        jSendIdField.setEditable(false);
        //fileIdField=new JTextField(5);
        fileIdField=new JComboBox();
       // fileIdField.addItem(1);
        //fileIdField.addItem(2);
        //fileIdField.setEditable(false);

        jTextArea= new JTextArea(15,40);
        jTextArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(jTextArea);
        fileAddressField = new JTextArea(1,40);
        fileAddressField.setEditable(false);
        jLogMessage = new JTextArea();
        jLogMessage.setEditable(false);



        jSeparator = new JSeparator(JSeparator.VERTICAL);




        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);

        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);


        /*layout.setHorizontalGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup()
                        .addComponent(userField)
                        .addComponent(scrollPane)
                        .addComponent(jTextField)
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(jLogLabel)
                                .addComponent(jLogMessage)))
                .addGroup(layout.createParallelGroup()
                        .addComponent(btnConnect)
                        .addComponent(btnReceive)
                        .addGroup(layout.createParallelGroup()
                                .addComponent(btnChoose)
                                .addComponent(btnSend))

                        .addComponent(btnRefresh))

        );*/

        layout.setHorizontalGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup()
                        .addComponent(userField)
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(fileIdLabel)
                                .addComponent(fileIdField))
                        .addComponent(scrollPane)
                        .addComponent(fileAddressField)
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(jSendIdLabel)
                                .addComponent(jSendIdField))
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(jLogLabel)
                                .addComponent(jLogMessage)))
                .addGroup(layout.createParallelGroup()
                        .addComponent(btnConnect)
                        .addComponent(btnVerifyFileId)
                        .addComponent(btnReceive)
                        .addGroup(layout.createParallelGroup()
                                .addComponent(btnChoose)
                                .addComponent(btnSend))

                        .addComponent(btnRefresh))

        );




        layout.setVerticalGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup()
                        .addComponent(userField)
                        .addComponent(btnConnect))

                .addGroup(layout.createParallelGroup()
                        .addComponent(fileIdLabel)
                        .addComponent(fileIdField)
                        .addComponent(btnVerifyFileId))

                .addGroup(layout.createParallelGroup()
                        .addComponent(scrollPane)
                        .addComponent(btnReceive))
                .addGroup(layout.createParallelGroup()
                        .addComponent(fileAddressField)
                        .addComponent(btnChoose))
                .addGroup(layout.createParallelGroup()
                        .addComponent(jSendIdLabel)
                        .addComponent(jSendIdField)
                        .addComponent(btnSend))
                .addGroup(layout.createParallelGroup()
                        .addComponent(jLogLabel)
                        .addComponent(jLogMessage)
                        .addComponent(btnRefresh))


        );

        setTitle("Gui");
        setSize(1200,800);
        pack();
        setVisible(true);
        setResizable(false);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {


            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                if (JOptionPane.showConfirmDialog(getContentPane(),
                        "Are you sure to close this window?", "Really Closing?",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION){
                    System.out.println("Line disconnected........");
                    System.exit(1);
                    /*Iterator itr = Server.clients.iterator();
                    while(itr.hasNext()){
                        Client client15 = (Client) itr.next();
                        if(client15.id==id){
                            Server.clients.remove(client15);
                            break;
                        }

                    }
                    return;*/
                }
            }
        });
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object btn = e.getSource();

        if(btn == btnConnect){
            id = Integer.parseInt(userField.getText());
            btnConnect.setEnabled(false);
            btnSend.setEnabled(false);
            btnChoose.setEnabled(true);
            btnRefresh.setEnabled(true);
            System.out.println("btn connect is clicked");
        }
        if(btn == btnChoose){
            createAndShowGUI();
            //btnChooseClicked=1;
            /*while(FileChooserConnected == 1){
                try {
                    TimeUnit.MILLISECONDS.sleep(1000);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }*/



        }
        if(btn == btnSend){
            sendFileGui = new SendFileGui();
            btnRefresh.setEnabled(false);

            long time = System.currentTimeMillis();
            while(time+1000>System.currentTimeMillis());

            choice = 1;

        }
        if(btn == btnReceive){
            choice = 2;
        }
        if(btn == btnRefresh){
            choice = 3;
        }

    }

    public static void createAndShowGUI() {
       // FileChooserConnected=1;
        //Create and set up the window.
         //frame = new JFrame("FileChooserDemo");
        //frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

        //Add content to the window.
        fileChooserDemo = new FileChooserDemo();
        btnChooseClicked=1;
        //frame.add(fileChooserDemo);

        //Display the window.
        /*frame.pack();
        frame.setVisible(true);
        System.out.println("visible");
        //frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);*/
        /*frame.addWindowListener(new java.awt.event.WindowAdapter() {


            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                if (JOptionPane.showConfirmDialog(frame,
                        "Are you sure to close this window?", "Really Closing?",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION){
                    //System.out.println("Line disconnected........");
                    if(fileChooserDemo.file_name!=null){
                        fileAddressField.setText(fileChooserDemo.file_name);
                        jSendIdField.setEditable(true);
                        btnSend.setEnabled(true);
                    }*/
                    //System.exit(1);
                    /*Iterator itr = Server.clients.iterator();
                    while(itr.hasNext()){
                        Client client15 = (Client) itr.next();
                        if(client15.id==id){
                            Server.clients.remove(client15);
                            break;
                        }

                    }
                    return;*/
                //}
            //}
        //});

    }

    public static void main (String [] args ) throws IOException {







        int bytesRead;
        int current = 0;
        int chunkSize = 0;
        int total;
        String  input;
        String input2;
        int student_id_to_send=0;

        FileOutputStream fos = null;
        BufferedOutputStream bos = null;
        FileInputStream fis = null;
        BufferedInputStream bis = null;

        InputStream is = null;
        OutputStream os = null;
        BufferedReader in = null;
        PrintWriter out = null;
        BufferedReader stdIn = null;


        File myFile=null;
        int fileSize=0;

        new Client();

        Socket sock = null;
        try{
            sock = new Socket(SERVER, SOCKET_PORT);
            System.out.println("Connecting...");
            is = sock.getInputStream();
            os = sock.getOutputStream();
            in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            out = new PrintWriter(os, true);

            stdIn =
                    new BufferedReader(
                            new InputStreamReader(System.in));
        }catch (IOException e) {
            e.printStackTrace();
            return;
        }

        System.out.println("Enter student id:");

       /* if((input=stdIn.readLine())!=null){
            id = Integer.parseInt(input);
            out.println(id);
        }*/
        System.out.println("Starting loop.....");

       while(true){
           if(id == -1) {
               try {
                   TimeUnit.MILLISECONDS.sleep(10);
               } catch (InterruptedException e) {
                   e.printStackTrace();
               }

               continue;
           }
           else{
               System.out.println("Sending id:"+id);
               out.println(id);
               break;
           }
       }


        int confirmation;
        if((input=in.readLine())!=null){
            confirmation = Integer.parseInt(input);
            if(confirmation==0){
                System.out.println("You cannot connected");
                jLogMessage.setText("You cannot connected");
            }
            else{

                userField.setText("User Id : "+id);
                userField.setBackground(Color.CYAN);
                userField.setEditable(false);
                System.out.println("You are connected!!!!");
                jLogMessage.setText("You are connected");


                String Folder_Name = "D:\\hello\\"+id+"\\";
                new File(Folder_Name).mkdirs();
                FILE_TO_RECEIVED=Folder_Name;



                int num = 0;
                if((input = in.readLine()) != null){//-------------------------- 1



                    while(true){
                        System.out.println("What do you want?");
                        System.out.println("For File Sending : Enter 1");
                        System.out.println("For Receiving File (If Any) : Enter 2");
                        System.out.println("For Refreshing : Enter 3");
                        int message=0;
                        if((input = in.readLine())!=null){ //---------------2
                            message = Integer.parseInt(input);

                            if(message==0){
                                System.out.println("No pending message to receive.!!!!!");
                                jTextArea.setText("No Pending messege to receive.!!!");
                                fileIdField.setEditable(false);
                                btnReceive.setEnabled(false);
                                System.out.println();
                                System.out.println();
                            }
                            else{
                                out.println(100);
                                if((input = in.readLine())!=null){//---------------------3
                                    System.out.println("getting pending file info "+input);
                                    System.out.println("A pending File to Receive:");
                                    fileIdField.setEditable(true);
                                    btnReceive.setEnabled(true);
                                    int stringLength = input.length();
                                    int position=0;
                                    int count=0;//to track a file from another
                                    int index=0;
                                    int fileId;
                                    jTextArea.setText("");
                                    fileIdField.removeAllItems();
                                    while(position<stringLength){

                                        if(count==0){
                                            System.out.println(count);
                                            index = input.indexOf('&',position);
                                            System.out.println("Position :"+position+"  index:"+index);

                                            String subString1 = input.substring(position,index);
                                            System.out.println(subString1);

                                            position=index+1;
                                            index = input.indexOf('%',position);
                                            String subString2 = input.substring(position,index);
                                            fileIdField.addItem(Integer.parseInt(subString2));
                                            subString1 = subString1+subString2;
                                            System.out.println(subString2);
                                            jTextArea.append(subString1);
                                            position=index+1;
                                        }
                                        else {
                                            System.out.println(count);

                                            index = input.indexOf('%',position);
                                            String subString1 = input.substring(position,index);
                                            jTextArea.append(subString1);
                                            position=index+1;


                                        }

                                        if(count == 3){
                                            jTextArea.append("\n\n");
                                            count=0;
                                        }
                                        else {
                                            jTextArea.append("\n");
                                            count++;
                                        }



                                    }

                                    System.out.println(input);
                                    System.out.println();
                                    System.out.println();
                                }


                            }
                        }






                       while(true){
                           if(choice==-1){
                               try {
                                   TimeUnit.MILLISECONDS.sleep(10);
                               } catch (InterruptedException e) {
                                   e.printStackTrace();
                               }
                               if(btnChooseClicked==1){
                                   if(fileChooserDemo.file_name!=null){

                                       fileAddressField.setText(fileChooserDemo.file_name);
                                       jSendIdField.setEditable(true);
                                       btnSend.setEnabled(true);
                                   }
                               }


                               continue;
                           }
                           else{

                               break;

                           }

                       }
                        out.println(choice);//------------------------------4
                        num=choice;
                        choice=-1;
                        System.out.println("sending choice :"+num);
                        switch(num){
                            case 1:{//client wants to send
                                btnSend.setEnabled(false);
                                jSendIdField.setEditable(false);

                                try {
                                    // send file
                                    System.out.println("Enter student id to whom you want to send file:");
                                    //student_id_to_send = Integer.parseInt(stdIn.readLine());

                                    student_id_to_send = Integer.parseInt(jSendIdField.getText());

                                    out.println(student_id_to_send);//--------------------------------------5
                                    System.out.println("sending student id:"+student_id_to_send);


                                    int student_to_send_coneected=0;
                                    if((input = in.readLine())!=null){//-----------------------------------6
                                        System.out.println("getting student id is present :"+input);
                                        student_to_send_coneected = Integer.parseInt(input);


                                       /* out.println(student_to_send_coneected);//to recheck that if he is here
                                        while()*/

                                        if(student_to_send_coneected==0){
                                            jLogMessage.setText("Student not connected to whom you want to send file.");
                                            System.out.println("Student not connected to whom you want to send file.");
                                            System.out.println("Sending fail.");
                                            fileAddressField.setText("");
                                            jSendIdField.setEditable(false);
                                            btnSend.setEnabled(false);
                                            System.out.println();
                                            System.out.println();
                                            break;


                                        }
                                        else{


                                            FILE_TO_SEND=fileChooserDemo.file_name;


                                            myFile = new File(FILE_TO_SEND);

                                            //sending file name to server
                                            out.println(myFile.getName());//-----------------------------7
                                            System.out.println("ending file name:"+myFile.getName());

                                            fileSize = (int) myFile.length();

                                            System.out.println("File Size :"+fileSize);
                                            System.out.println("sending file size:"+input);
                                            out.println(fileSize);//-----------------------------------------8

                                            int fileLimit;
                                            if((input=in.readLine())!=null){//-----------------------------9
                                                System.out.println("getting buffer size:"+input);
                                                fileLimit = Integer.parseInt(input);
                                                if(fileLimit==0){
                                                    System.out.println("Buffer limit exceedes");
                                                    break;
                                                }
                                                else {
                                                    //getting chunk size from server
                                                    System.out.println("Buffer limit within limit");

                                                    String chunk;
                                                    if ((chunk = in.readLine()) != null) {//-------------------------------10
                                                        System.out.println("getting chunk size:" + chunk);
                                                        chunkSize = Integer.parseInt(chunk);
                                                        System.out.println("Chunk Size : " + chunkSize);
                                                    }

                                                    //reading from file
                                                    byte[] mybytearray = new byte[fileSize];
                                                    fis = new FileInputStream(myFile);
                                                    bis = new BufferedInputStream(fis);
                                                    bis.read(mybytearray, 0, mybytearray.length);

                                                    //starting sending
                                                    System.out.println("Sending " + FILE_TO_SEND + "(" + mybytearray.length + " bytes)");
                                                    //os.write(mybytearray,0,mybytearray.length);

                                                    total = 0;
                                                    int reading_length;
                                                    int remain = fileSize;
                                                    long sending_time = 0;
                                                    int global_sequence_no = 0;


/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////



                                                    int maximum_stuffed_byte = (int)Math.ceil(Math.floor((chunkSize*8)/5)/8);
                                                    int payLoad_size = chunkSize-maximum_stuffed_byte-4-2;//4 for kind of data or ack,ackNo,seqNo,checksum

                                                    int frame_number = 1;
                                                    int sequence_no = 1;
                                                    int n=1;
                                                    while (total != fileSize) {
                                                        if (fileSize - total < payLoad_size) {
                                                            payLoad_size = fileSize - total;
                                                        }
                                                        //out.println("");//------->

                                                        String str = new String();


                                                        System.out.println("sending start hosse........");
                                                        for (int i = total; i < total + payLoad_size; i++) {
                                                            str = str + String.format("%8s", Integer.toBinaryString(mybytearray[i] & 0xFF)).replace(' ', '0');
                                                            //System.out.println(mybytearray[i]+"  "+str);
                                                            //out.println("");//------>

                                                        }
                                                        //System.out.println("original data : "+str);


                                                        System.out.println("sending start hosse hosse........");

                                                        //frame without checksum forming starting
                                                        String frame_without_checksum = new String();
                                                        frame_without_checksum += "00000001";//for kind of frame (Data/ack)
                                                        frame_without_checksum += String.format("%8s", Integer.toBinaryString(sequence_no & 0xFF)).replace(' ', '0');
                                                        frame_without_checksum += "00000000";//for ackNo ---> for data frame it is arbitrary
                                                        frame_without_checksum += str; //for payload
                                                        //frame without checksum forming ended

                                                        //out.println("");//----->



                                                        System.out.println("checksum calculation starting....");
                                                        // checksum calculation start
                                                        String checkSum = new String();
                                                        int numbers_of_1;
                                                        for (int i = 0; i < 8; i++) {
                                                            numbers_of_1 = 0;
                                                            for (int j = i; j < frame_without_checksum.length(); j += 8) {
                                                                if (frame_without_checksum.charAt(j) == '1')
                                                                    numbers_of_1 += 1;
                                                            }
                                                            if ((numbers_of_1 % 2) == 1) checkSum += "1";
                                                            else checkSum += "0";

                                                            //out.println("");//---->

                                                        }
                                                        //checksum calculation ended
                                                        //System.out.println("original data +checksum : "+str+checkSum);
                                                        System.out.println("checksum calculation endinging....");


                                                        //frame with checksum forming starting
                                                        String frame = new String();
                                                        frame += frame_without_checksum;
                                                        frame += checkSum; // for checksum
                                                        //frame with checksum forming ended

                                                        //out.println("");//---->


                                                        System.out.println("start 123");



                                                        String text_before_in_frame_before_stuffing = sendFileGui.frame_before_stuffing_textarea.getText();
                                                        System.out.println("start 123");

                                                        text_before_in_frame_before_stuffing += "Sequence number : " + Integer.toString(sequence_no) + "\n";
                                                        text_before_in_frame_before_stuffing += frame;
                                                        text_before_in_frame_before_stuffing += "\n\n";
                                                        sendFileGui.frame_before_stuffing_textarea.setText(text_before_in_frame_before_stuffing);

                                                        //out.println("");//---->


                                                        System.out.println("stuffed bits entering starting....");
                                                        //Bit stuffing into frame
                                                        int count = 0;
                                                        String stuffed_frame = new String();
                                                        stuffed_frame = "01111110";
                                                        for (int i = 0; i < frame.length(); i++) {
                                                            if (frame.charAt(i) == '0') {
                                                                stuffed_frame = stuffed_frame + frame.charAt(i);
                                                                count = 0;
                                                            } else {
                                                                stuffed_frame = stuffed_frame + frame.charAt(i);
                                                                count++;
                                                            }

                                                            if (count == 5) {
                                                                stuffed_frame = stuffed_frame + '0';
                                                                count = 0;
                                                            }


                                                            //out.println("");//---->

                                                        }
                                                        stuffed_frame += "01111110";
                                                        //Bit stuffing ended
                                                        System.out.println("stuffed bits entering ending....");


                                                        String text_before_in_frame_after_stuffing = sendFileGui.frame_after_stuffing_textarea.getText();
                                                        text_before_in_frame_after_stuffing += "Sequence number : " + Integer.toString(sequence_no) + "\n";
                                                        text_before_in_frame_after_stuffing += stuffed_frame;
                                                        text_before_in_frame_after_stuffing += "\n\n";
                                                        sendFileGui.frame_after_stuffing_textarea.setText(text_before_in_frame_after_stuffing);





                                                        while((stuffed_frame.length()/8) < chunkSize){
                                                            stuffed_frame += "00000000";
                                                        }

                                                        //out.println("");//---->

                                                        //////////////////////////////////////////////////////////////ERROR CREATION
                                                        while (true) {
                                                            System.out.println("enter error button clicked :" + sendFileGui.enter_error_button_clicked);
                                                            if (sendFileGui.enter_error_button_clicked == 1) {
                                                                sendFileGui.enter_error_button.setEnabled(false);
                                                                sendFileGui.send_error_button.setEnabled(true);
                                                                sendFileGui.frame_error_textfield.setText(stuffed_frame);
                                                                sendFileGui.frame_error_textfield.setEditable(true);

                                                                //long time_needed = System.currentTimeMillis()+500;
                                                                while (true) {
                                                                    System.out.println("not clicked.....");

                                                                   // out.println("");
                                                                    //in.readLine();





                                                                    if (sendFileGui.send_error_button_clicked == 1) {


                                                                        System.out.println("clicked.....");

                                                                        sendFileGui.enter_error_button_clicked = 0;


                                                                        sendFileGui.enter_error_button.setEnabled(true);

                                                                        sendFileGui.send_error_button.setEnabled(false);


                                                                        sendFileGui.send_error_button_clicked = 0;



                                                                        sendFileGui.frame_error_textfield.setEditable(true);


                                                                        //out.println("");



                                                                        stuffed_frame = sendFileGui.frame_error_textfield.getText();
                                                                        sendFileGui.frame_error_textfield.setText("");


                                                                        break;
                                                                    }



                                                                }

                                                            }
                                                            else break;
                                                        }


                                                        /////////////////////////////////////////////////////////////ERROR CREATION


                                                        //out.println("");





                                                        System.out.println("Sending Frame number :" + sequence_no);
                                                        //out.println("");
                                                        out.println(stuffed_frame);
                                                        System.out.println("Frame send");
                                                        sending_time = System.currentTimeMillis() + chunkSize*2;
                                                        String confirmation_msg;
                                                        int flag = 0;
                                                        int flag5 = 0;
                                                        out.println("");//--------->
                                                        while (sending_time > System.currentTimeMillis()) {
                                                                //out.println("");
                                                                confirmation_msg = in.readLine();
                                                                if(confirmation_msg.equals("confirm")) flag5 = 1;
                                                                if (!(confirmation_msg.equals("")) && !(confirmation_msg.equals("confirm"))){
                                                                    flag = 0;


                                                                    //frame recognizing starting
                                                                    String frame_with_stuffed_bit_ack = new String();
                                                                    int first_index_of_frame = confirmation_msg.indexOf("01111110", 0);
                                                                    int last_index_of_frame = confirmation_msg.indexOf("01111110", first_index_of_frame + 8);
                                                                    frame_with_stuffed_bit_ack = confirmation_msg.substring(first_index_of_frame + 8, last_index_of_frame);
                                                                    //frame recognizing ended


                                                                    //stuffed bit removing...........
                                                                    int counter = 0;
                                                                    String frame_without_stuffed_bit_ack = new String();
                                                                    for (int i = 0; i < frame_with_stuffed_bit_ack.length(); i++) {
                                                                        if (frame_with_stuffed_bit_ack.charAt(i) == '0') {
                                                                            frame_without_stuffed_bit_ack += frame_with_stuffed_bit_ack.charAt(i);
                                                                            counter = 0;
                                                                        } else {
                                                                            frame_without_stuffed_bit_ack += frame_with_stuffed_bit_ack.charAt(i);
                                                                            counter++;
                                                                        }

                                                                        if (counter == 5) {
                                                                            i++;
                                                                            counter = 0;
                                                                        }
                                                                    }
                                                                    System.out.println("frame_without stuffed bit ack : " + frame_without_stuffed_bit_ack);
                                                                    //stuffed bit removed


                                                                    int frame_length_ack = frame_without_stuffed_bit_ack.length();


                                                                    String receivedCheckSum_ack = frame_without_stuffed_bit_ack.substring(frame_length_ack - 8, frame_length_ack);
                                                                    String frame_without_checksum_ack = frame_without_stuffed_bit_ack.substring(0, frame_length_ack - 8);


                                                                    // checksum calculation start
                                                                    String newCheckSum_ack = new String();
                                                                    int numbers_of_1_ack;
                                                                    for (int i = 0; i < 8; i++) {
                                                                        numbers_of_1_ack = 0;
                                                                        for (int j = i; j < frame_without_checksum_ack.length(); j += 8) {
                                                                            if (frame_without_checksum_ack.charAt(j) == '1')
                                                                                numbers_of_1_ack += 1;
                                                                        }
                                                                        if ((numbers_of_1_ack % 2) == 1)
                                                                            newCheckSum_ack += "1";
                                                                        else newCheckSum_ack += "0";
                                                                    }
                                                                    System.out.println("Calculated Checksum : " + newCheckSum_ack);
                                                                    System.out.println("Received Checksum : " + receivedCheckSum_ack);
                                                                    //checksum calculation ended


                                                                    if (receivedCheckSum_ack.equals(newCheckSum_ack)) {

                                                                        if (frame_without_checksum_ack.substring(0, 8).equals("00000000")) {
                                                                            System.out.println("Acknowledge found.");


                                                                            //getting sequence number (start)
                                                                            /*No need for acknowledge frame */
                                                                            //getting sequence number (end)


                                                                            //getting acknowledge number (start)
                                                                            String sequence_number_string_ack = (frame_without_checksum_ack.substring(16, 24));
                                                                            int sequence_number_ack = 0;
                                                                            for (int k = 0; k < 8; k++) {
                                                                                if (sequence_number_string_ack.charAt(k) == '1')
                                                                                    sequence_number_ack += Math.pow(2, (7 - k));
                                                                            }
                                                                            System.out.println("Acknowledge sequence number : " + sequence_number_ack);


                                                                            if (sequence_number_ack == sequence_no) {
                                                                                flag = 1;
                                                                                sendFileGui.log_msg_textarea.setForeground(Color.GREEN);
                                                                                sendFileGui.log_msg_textarea.setBackground(Color.WHITE);

                                                                                sendFileGui.log_msg_textarea.append("No Error detected! in FRAME SEQUENCE NO.");
                                                                                sendFileGui.log_msg_textarea.append(Integer.toString(global_sequence_no*255+sequence_no));
                                                                                sendFileGui.log_msg_textarea.append("\n");
                                                                                total = total + payLoad_size;



                                                                                if(sequence_no == 255) {
                                                                                    global_sequence_no ++;
                                                                                    sequence_no = 0;
                                                                                }
                                                                                //total = sequence_no * payLoad_size;
                                                                                sequence_no++;
                                                                                if(total == fileSize){
                                                                                    sendFileGui.log_msg_textarea.append("File Sending Complete");
                                                                                    sendFileGui.log_msg_textarea.setBackground(Color.GREEN);
                                                                                    sendFileGui.log_msg_textarea.setForeground(Color.WHITE);

                                                                                }


                                                                                System.out.println("Hello2");

                                                                                System.out.println("Hello3");
                                                                                break;




                                                                            } else {
                                                                                System.out.println("Frame error occurs. Frame retransmitting.....");
                                                                                sendFileGui.log_msg_textarea.setForeground(Color.RED);
                                                                                sendFileGui.log_msg_textarea.append("Error detected in FRAME SEQUENCE NO.");
                                                                                sendFileGui.log_msg_textarea.append(Integer.toString(global_sequence_no*255+sequence_no));
                                                                                sendFileGui.log_msg_textarea.append("! Frame retransmit starting......");
                                                                                sendFileGui.log_msg_textarea.append("\n");
                                                                                //out.println(stuffed_frame);
                                                                                //sending_time = System.currentTimeMillis() + chunkSize*2+6000;
                                                                                break;


                                                                            }





                                                                        }
                                                                    } else {
                                                                        System.out.println("Frame error occurs. Frame retransmitting.....");
                                                                        sendFileGui.log_msg_textarea.setBackground(Color.RED);
                                                                        sendFileGui.log_msg_textarea.append("Error detected in FRAME SEQUENCE NO.");
                                                                        sendFileGui.log_msg_textarea.append(Integer.toString(global_sequence_no*255+sequence_no));
                                                                        sendFileGui.log_msg_textarea.append("! Frame retransmit starting......");
                                                                        sendFileGui.log_msg_textarea.append("\n");
                                                                        //out.println(stuffed_frame);
                                                                        //sending_time = System.currentTimeMillis() + chunkSize*2+6000;
                                                                        break;


                                                                    }

                                                                    //break;
                                                                }
                                                                else{
                                                                    if(flag5 == 0){
                                                                        sendFileGui.timeout_textarea.setText(Integer.toString((int)(sending_time-System.currentTimeMillis())));
                                                                        out.println("");
                                                                    }
                                                                    else{

                                                                        sendFileGui.timeout_textarea.setText(Integer.toString((int)(sending_time-System.currentTimeMillis())));

                                                                    }

                                                                }



                                                        }

                                                        if(flag==0){
                                                            System.out.println("Frame error occurs. Frame retransmitting.....");
                                                            sendFileGui.log_msg_textarea.setBackground(Color.RED);
                                                            sendFileGui.log_msg_textarea.append("Error detected in FRAME SEQUENCE NO.");
                                                            sendFileGui.log_msg_textarea.append(Integer.toString(global_sequence_no*255+sequence_no));
                                                            sendFileGui.log_msg_textarea.append("! Frame retransmit starting......");
                                                            sendFileGui.log_msg_textarea.append("\n");


                                                        }


                                                }

                                                    out.println("1");
                                                    System.out.println("Done.");
                                                    while(true){//------------------------------13
                                                        if(((input = in.readLine()).equals("1"))){

                                                                System.out.println("getting 1");
                                                                System.out.println("sending succesful "+input);
                                                                break;

                                                        }


                                                    }
                                                    System.out.println("1111111111111111");
                                                    fileChooserDemo.file_name=null;
                                                    fileAddressField.setText("");

                                                    jSendIdField.setEditable(false);
                                                    btnSend.setEnabled(false);
                                                    btnRefresh.setEnabled(true);
                                                }
                                            }
                                        }

                                    }










                                } catch (FileNotFoundException e) {
                                    e.printStackTrace();
                                    return;
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    return;
                                } finally {
                                    if (bis != null) try {
                                        bis.close();
                                        //if (os != null) os.close();
                                        //if (sock!=null) sock.close();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                        return;
                                    }

                                }
                                break;
                            }//case 1 ends here
                            case 2:{//clients wants to receive

                                try{

                                    //System.out.println("hello");

                                    // receive file
                                    byte [] mybytearray  = new byte [FILE_SIZE];
                                    //System.out.println("hello 5");



                                    System.out.println("Enterb file id to receive.....");



                                    out.println(fileIdField.getItemAt(fileIdField.getSelectedIndex()));
                                   // System.out.println("outside  while loop");
                                    out.println(100);//----------------------------15
                                    System.out.println("sending 100");


                                    //to get file name

                                    String fileName=null;
                                    if((input = in.readLine())!=null){//------------------------------------16
                                        System.out.println("getting file name from server :"+input);
                                        fileName = input;
                                    }


                                    out.println(150);
                                    System.out.println("sending 150");

                                    //to get file size
                                    if((input = in.readLine())!=null){//-------------------------------------17
                                        System.out.println("getting file size from server :"+input);
                                        fileSize = Integer.parseInt(input);
                                    }


                                    out.println(200);
                                    System.out.println("sending 200");


                                    //to get file size
                                    if((input = in.readLine())!=null){//-------------------------------------17
                                        System.out.println("getting chunk size from server :"+input);
                                        chunkSize = Integer.parseInt(input);
                                    }


                                    out.println(250);
                                    System.out.println("sending 250");



                                    fos = new FileOutputStream(FILE_TO_RECEIVED+fileName);
                                    bos = new BufferedOutputStream(fos);

                                    total = 0;
                                    int total1 = 0;
                                    //chunkSize = 10000;
                                    int remain ;
                                    int s = 1;
                                    while(total!=fileSize){
                                        bytesRead = is.read(mybytearray,total1,chunkSize);//----------------------------18
               /* while(bytesRead != chunkSize){
                    bytesRead = bytesRead + is.read(mybytearray,total,chunkSize-bytesRead);
                }*/
                                        out.println(s);//to inform that getting 1 chunk//-------------------------19
                                        s++;
                                        total = total+bytesRead;
                                        total1 = total1+bytesRead;
                                        bos.write(mybytearray, 0 , total1);
                                        total1 = 0;
                                        remain = fileSize - total;
                                        System.out.println("bytesRead = "+bytesRead+" ,File  downloaded (" + total + " bytes read)");

                                        if(chunkSize>remain){
                                            chunkSize = remain;
                                        }
                                    }

                                    out.println(-1);//-------------------------------------20
                                    System.out.println("sending 50");

                                    bos.flush();
                                    System.out.println("File " + FILE_TO_RECEIVED
                                            + " downloaded (" + fileSize + " bytes read)");

                                }
                                finally {
                                    if (fos != null) fos.close();
                                    if (bos != null) bos.close();
                                    //if (sock != null) sock.close();
                                }
                                break;
                            }//case 2 ends here
                            case 3:{
                                break;
                            }//case 3 ends here
                        }


                    }
                }


            }
        }









    }


}