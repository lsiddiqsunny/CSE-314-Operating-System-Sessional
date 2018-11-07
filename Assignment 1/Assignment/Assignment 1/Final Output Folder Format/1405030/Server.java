package Server;

import Adaptor.ListEntryCellRenderer;
import Client.Client;
import Model.ListEntry;
import util.NetworkUtil;

import javax.swing.*;
import java.awt.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Hashtable;
import javax.swing.DefaultListModel;

/**
 * Created by sadiq on 9/19/17.
 */
public class Server {

    private ServerSocket ServSock;
    public static DefaultListModel<ListEntry> model;
    public int i = 1;
    public static Hashtable<String, NetworkUtil> table;
    public static final int MAX_BUFFER_SIZE=1024*1024*64;
    public static int CURRENT_BUFFER_SIZE=0;
    public static ServerGui sgui;
    //public static Hashtable<String,Boolean> fileQueue;

    Server() {
        table = new Hashtable<>();
        new ShowCurrentUser();
        try {
            ServSock = new ServerSocket(33333);
            //new WriteThreadServer(table, "Server");

            while (true) {
                Socket clientSock = ServSock.accept();
                NetworkUtil nc=new NetworkUtil(clientSock);
                //clientSock.setSoTimeout(30);


                String id = (String) nc.read();

                if(!table.containsKey(id)) {
                    table.put(id, nc);
                    nc.write("Connected");
                    //System.out.println("id "+id+" Connected");
                    //fileQueue.put(id,false);
                    sgui.console.append("\nid "+id+" Connected");
                    new TransmissionStateServer(nc, id);

                }

                else {
                    nc.write("ID Already Connected");
                    nc.closeConnection();
                }

            }
        }catch(Exception e) {
            System.out.println("Server starts:"+e);
        }
    }




    public static void main(String args[]) {
        Client.setNimbusLookAndFeel();
        createUIComponent();
        createModel();

        Server objServer = new Server();
    }

    static void createModel(){
        model = new DefaultListModel();

        sgui.list.setModel(model);

        sgui.list.setCellRenderer(new ListEntryCellRenderer());

    }

    static void createUIComponent(){
        JFrame frame = new JFrame("Server");
        sgui = new ServerGui();
        frame.setContentPane(sgui.pane);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        frame.setSize(600, 300);
    }
}

