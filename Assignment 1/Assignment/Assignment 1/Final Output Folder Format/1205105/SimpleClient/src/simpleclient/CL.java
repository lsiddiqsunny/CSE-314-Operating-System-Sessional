/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package simpleclient;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.Socket;
import java.util.Scanner;


/**
 *
 * @author User
 */
public class CL extends javax.swing.JFrame {

    private static Socket s = null;
    String message;
    String strSend = "v";
    String q = "";
    String username="";
    int h = 0;
    String aar;
    private static BufferedReader br = null;
    private static PrintWriter pr = null;
    int chunksize;
    int fileid;
    int size;

    /**
     * Creates new form CL
     */
    public CL() {
        super("Client Window");
        initComponents();
        setVisible(true);
        setSize(700, 500); // set size of window
        setVisible(true);
        jButton3.setVisible(false);
        jButton4.setVisible(false);
        jButton5.setVisible(false);
        jTextField3.setVisible(false);
        jTextField4.setVisible(false);
        jLabel3.setVisible(false);
        jLabel4.setVisible(false);
    }

    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTextField1 = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jTextField2 = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jTextField3 = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jTextField4 = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jTextField1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField1ActionPerformed(evt);
            }
        });

        jLabel1.setText("NAME");

        jLabel2.setText("Ip Address");

        jTextField2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField2ActionPerformed(evt);
            }
        });

        jButton1.setText("LOGIN");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setText("LogOut");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jButton3.setText("OK");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jButton4.setText("GetList");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        jButton5.setText("GetFile");
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });

        jTextField3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField3ActionPerformed(evt);
            }
        });

        jLabel3.setText("Wrong Information. TRY AGAIN");

        jLabel4.setText("File Size");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 112, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTextField1)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jTextField3)
                            .addComponent(jTextField2, javax.swing.GroupLayout.DEFAULT_SIZE, 128, Short.MAX_VALUE))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 144, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jTextField4)
                                .addGap(22, 22, 22)))
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(52, 52, 52)
                                .addComponent(jButton2))
                            .addComponent(jButton3)
                            .addComponent(jButton4)
                            .addComponent(jButton5)
                            .addComponent(jButton1))
                        .addGap(33, 33, 33)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton2)
                    .addComponent(jLabel3))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(24, 24, 24)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel2)
                    .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(13, 13, 13)
                        .addComponent(jButton1))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(2, 2, 2)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel4)
                            .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButton5)
                .addGap(21, 21, 21))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:

        //strSend="log";
        pr.println("log");
        pr.flush();
        String n = jTextField1.getText();
        username=n;
        n = n + "," + jTextField2.getText();
        pr.println(n);
        pr.flush();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jTextField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        // TODO add your handling code here:
        pr.println("BYE");
        pr.flush();
        pr.println(username);
        pr.flush();
        
        System.out.println("Client wishes to terminate the connection. Exiting main.");
        setVisible(false);
        cleanUp();
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed

        pr.println("fileinfo");
        pr.flush();
        aar = jTextField1.getText();
        pr.println(aar);
        pr.flush();
        String rec=jTextField2.getText();
        pr.println(rec);
        pr.flush();
        String sizee=jTextField4.getText();
        size=Integer.parseInt(sizee);
       pr.println(sizee);
        pr.flush();
        
        pr.println("done");
        pr.flush();


        // TODO add your handling code here:
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        // TODO add your handling code here:
        pr.println("file");
        pr.flush();
        String aa = jTextField2.getText();
        System.out.println(aa);
        pr.println(aa);
        pr.flush();
        aa = jTextField3.getText();
        System.out.println(aa);
        pr.println(aa);
        pr.flush();
    }//GEN-LAST:event_jButton5ActionPerformed

    private void jTextField3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField3ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField3ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        // TODO add your handling code here:
        jTextField1.setVisible(true);
        jTextField3.setVisible(true);
        jTextField1.setText("");
        //jTextField1.setVisible(true);
        jButton5.setVisible(true);
        pr.println("list");
        pr.flush();
        h = 1;
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jTextField2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField2ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField2ActionPerformed

    /**
     * @param args the command line arguments
     */
    public void runClient() {
        try {
            s = new Socket("localhost", 2222);

            br = new BufferedReader(new InputStreamReader(s.getInputStream()));
            pr = new PrintWriter(s.getOutputStream());
        } catch (Exception e) {
            System.err.println("Problem in connecting with the server. Exiting main.");
            System.exit(1);
        }

        Scanner input = new Scanner(System.in);
        String strRecv = null;

        try {
            strRecv = br.readLine();//client theke ja ase 
            if (strRecv != null) {
                System.out.println("Server says: " + strRecv);
            } else {
                System.err.println("Error in reading from the socket. Exiting main.");
                //cleanUp();
                System.exit(0);
            }
        } catch (Exception e) {
            System.err.println("Error in reading from the socket. Exiting main.");
            cleanUp();
            System.exit(0);
        }
        //processing the  what server sends
        do {
            try // read message and display it
            {
                message = br.readLine(); // read new message
               // System.out.println(message);

                
                if (message.equals("hasID")) {
                    jTextField1.setText("");
                    jButton3.setVisible(true);
                    jTextField2.setText("");
                    jButton1.setVisible(false);
                    jLabel1.setText("File Name:");
                    jLabel4.setVisible(true);
                    jLabel4.setText("File Size:");
                    jLabel2.setText("Receiver Name:");
                    jLabel3.setVisible(false);
                    jTextField4.setVisible(true);
                }
                if (message.equals("alreadyloged")) {
                    jTextField1.setVisible(false);
                    jLabel3.setVisible(true);
                    jLabel3.setText("Already Logged In ");
                    jButton3.setVisible(false);
                    jButton2.setVisible(false);
                    jTextField2.setVisible(false);
                    jButton1.setVisible(false);
                     //jButton2.setVisible(true);
                    jLabel1.setVisible(false);
                    jLabel2.setVisible(false);
                    //setVisible(false);

                }
                if (message.equals("log in not successful.")) {
                    jLabel3.setVisible(true);
                }
                if (message.equals("overflow")) {
                    jLabel3.setVisible(true);
                    jLabel3.setText("file size exceed");
                }
                if (message.equals("notlogged")) {
                    jLabel3.setVisible(true);
                    jLabel3.setText("recever not online");
                }
                if (message.equals("correct")) {
                    String temp=br.readLine();
                    fileid=Integer.parseInt(temp);
                    //System.out.println(fileid);
                    temp=br.readLine();
                    chunksize=Integer.parseInt(temp);
                    System.out.println("cf " +chunksize);
                    
                    jLabel3.setVisible(false);
                    jTextField1.setVisible(false);
                    jButton3.setVisible(false);
                    jTextField2.setVisible(false);
                    jButton1.setVisible(false);
                    jButton4.setVisible(false);
                    jLabel1.setVisible(false);
                    jLabel2.setVisible(false);
                 
                    File files = new File(aar+".txt");
                  //  System.out.println("hoi"+ aar);
                    FileInputStream fiss = new FileInputStream(files);
                  //  System.out.println("hoi");
                    BufferedInputStream bis = new BufferedInputStream(fiss);
                  //  System.out.println("hoi");
                    OutputStream os = s.getOutputStream();
                 //   System.out.println("hoi");
                    byte[] contents;
                    int current = 0;
                                       
                    long start = System.nanoTime();
                    //contents = new byte[size];
    
                    String data="";
                    int dot=0;
                    while (current != size) 
                    {
                        int sz=chunksize;
                        if (size - current >= sz) 
                        {
                            current += sz;
                        } 
                        else 
                        {
                            sz = (int) (size - current);
                            current = size;
                            dot=1;
                        }
                        contents = new byte[sz];
                        int y = bis.read(contents, 0, sz);
                        System.out.println("+ "+y);
                        
                        byte[] new_content=new byte[sz+4];
                        new_content=bitStuff(contents);
                        
                        String s = new String(new_content);
                        
                        if(dot!=1){
                            data=data+s+"xox";
                            //System.out.println("reading file "+data);
                        }
                        else{
                            data=data+s;
                        }
                    }//while done
                     System.out.println("reading file "+data);
                     String [] info =data.split("xox");
                     //System.out.println(info.length);
                     pr.println("file giving");
                     pr.flush();
                     int tout=0;
                     for(int i=0;i<info.length;i++)
                     {
                         pr.println(info[i]);
                         pr.flush();
                         long thisTime = System.currentTimeMillis();
                         while(true){
                             String ack=br.readLine();
                             if(ack.equals("ack"))break;
                             long thist = System.currentTimeMillis();
                             if(thist-thisTime>20){
                                 pr.println("timeout");
                                 pr.flush();
                                 tout=1;
                                 break;
                             }
                         }
                         if(tout==1)break;
                     }
                     if(tout!=1){
                        pr.println("comp");
                        pr.flush();
                    }
                    
                }
                if (message.equals("error")){
                    jLabel3.setVisible(true);
                    jLabel3.setText("error in sending");
                }
                
            } catch (Exception e) {
                //System.out.println("exp");
            }

        } while (!message.equals("TERMINATE"));

        cleanUp();
    }

    private void cleanUp() {
        try {
            br.close();
            pr.close();
            s.close();
        } catch (Exception e) {

        }
    }
    
    public static byte[] bitStuff(byte[] a){
		//write code for bitstuffing here
             //String aaa = new String(a);
       // System.out.println(aaa);
        
        //System.out.println(a);
        //System.out.println(a.length);
        String s="";
        for(int i=0;i<a.length;i++)
        {
        s +=("0000000" + Integer.toBinaryString(0xFF & a[i])).replaceAll(".*(.{8})$", "$1");
       
        }
         //System.out.println(s);
        
        String n=s.replaceAll("11111", "111110");
         //System.out.println(n);
         
         int ii=n.length()%8;
         while(ii!=8)
         {
             n+="0";
             ii++;
         }
         //System.out.println(n+" "+n.length());
         byte[] bval = new BigInteger(n, 2).toByteArray();
         //System.out.println(bval);
         //System.out.println(bval.length);
         return bval;
                
                
		//return b;
               
                
     
	}


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField jTextField3;
    private javax.swing.JTextField jTextField4;
    // End of variables declaration//GEN-END:variables
}
