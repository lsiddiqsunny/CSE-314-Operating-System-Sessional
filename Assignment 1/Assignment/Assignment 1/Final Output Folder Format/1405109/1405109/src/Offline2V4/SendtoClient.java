/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Offline2V4;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JDialog;
import javax.swing.JOptionPane;

/**
 *
 * @author ASUS
 */
public class SendtoClient extends Thread {

    private Socket socket;

    SendtoClient(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        FileOutputStream fos = null;
        try {
            byte[] contents = new byte[40920];

            fos = new FileOutputStream("E:\\1405109\\routine.txt");
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            InputStream is = null;
            if (socket.getInputStream() != null) {
                JDialog.setDefaultLookAndFeelDecorated(true);
                int response = JOptionPane.showConfirmDialog(null, "Do you want to recieve File?", "Confirm",
                        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (response == JOptionPane.NO_OPTION) {
                    System.out.println("No button clicked");
                } else if (response == JOptionPane.YES_OPTION) {
                    is = socket.getInputStream();

                    int bytesRead = 0;

                    while ((bytesRead = is.read(contents)) != -1) {
                        System.out.println("reading..");
                        
                         System.out.println(new String(contents));
                         System.out.println(bytesRead);
                        bos.write(contents, 0, bytesRead);
                         bos.flush();
                         bos.close();
                    }
                    
                   
                } else if (response == JOptionPane.CLOSED_OPTION) {
                    System.out.println("JOptionPane closed");
                }

                System.out.println("File saved successfully!");
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(SendtoClient.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(SendtoClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
