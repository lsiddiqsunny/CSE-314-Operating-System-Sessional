package client;

import java.awt.Color;
import java.math.*;
import java.io.*;
import static java.lang.Thread.sleep;
import java.net.Socket;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

public class SendingFileThread implements Runnable {

    protected Socket socket;
    private DataOutputStream dos;
    protected SendFile form;
    protected String file;
    protected String receiver;
    protected String sender;
    private final int BUFFER_SIZE = 25;
    MainForm mf;

    public SendingFileThread(Socket soc, String file, String receiver, String sender, SendFile frm, MainForm mf) {
        this.socket = soc;
        this.file = file;
        this.receiver = receiver;
        this.sender = sender;
        this.form = frm;
        this.mf = mf;
    }

    @Override
    public void run() {
        try {
            form.disableGUI(true);
            mf.appendMessage("Sending File..!", "", Color.yellow, Color.yellow);
            dos = new DataOutputStream(socket.getOutputStream());
            /**
             * Write filename, recipient, username *
             */
            //  Format: CMD_SENDFILE [Filename] [Size] [Recipient] [Sender]
            File filename = new File(file);
            int len = (int) filename.length();
            int filesize = (int) Math.ceil(len / BUFFER_SIZE); // get the file size
            String clean_filename = filename.getName();
            dos.writeUTF("CMD_SENDFILE " + clean_filename.replace(" ", "_") + " " + filesize + " " + receiver + " " + sender);
            mf.appendMessage("From: " + sender, "", Color.yellow, Color.yellow);
            mf.appendMessage("To: " + receiver, "", Color.yellow, Color.yellow);
            /**
             * Create an stream *
             */
            InputStream input = new FileInputStream(filename);
            OutputStream output = socket.getOutputStream();

            /**
             * Read file **
             */
            BufferedInputStream bis = new BufferedInputStream(input);
            /**
             * Create a temporary file storage *
             */

            byte[] readbuffer = new byte[3];
            byte[] writebuffer = new byte[BUFFER_SIZE];
            int nRead;

            for (int i = 0; (nRead = bis.read(readbuffer)) > 0; i++) {
                //bitStuffing

                String s1 = "";//data before stuffed
                for (int j = 0; j < nRead; j++) {
                    String s2 = String.format("%8s", Integer.toBinaryString(readbuffer[j] & 0xFF)).replace(' ', '0');
                    s1 = s1.concat(s2);

                }
                mf.appendMessage("Data before bit stuffing ", "", Color.yellow, Color.yellow);
                mf.appendMessage(s1, "", Color.yellow, Color.yellow);
                //checksum
                byte sum = (byte) 0;
                for (int j = 0; j < nRead; j++) {
                    sum = (byte) (sum + readbuffer[j]);
                }
                byte checksum = (byte) ~sum;

                int seqNo = i;//packet no
                byte b = (byte) seqNo;
                String seqNostr = String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0');
                mf.appendMessage("Sequence no " + seqNostr, "", Color.yellow, Color.yellow);
                String checksumstr = String.format("%8s", Integer.toBinaryString(checksum & 0xFF)).replace(' ', '0');
                mf.appendMessage("Checksum " + checksumstr, "", Color.yellow, Color.yellow);
                int ack = 0;//acknowledge
                byte d = (byte) ack;
                String ackstr = String.format("%8s", Integer.toBinaryString(d & 0xFF)).replace(' ', '0');
                mf.appendMessage("Acknowlegement no " + ackstr, "", Color.yellow, Color.yellow);
                int payload = nRead;
                byte e = (byte) payload;
                String payloadstr = String.format("%8s", Integer.toBinaryString(e & 0xFF)).replace(' ', '0');
                mf.appendMessage("Payload " + payloadstr, "", Color.yellow, Color.yellow);
                s1 = s1 + seqNostr + ackstr + payloadstr + checksumstr;
                mf.appendMessage("data + seqNo + ackno + payload + checksum", "", Color.yellow, Color.yellow);
                mf.appendMessage(s1, "", Color.yellow, Color.yellow);
                //data after stuffed
                String s3 = "";
                int count1 = 0;
                int s1Length = s1.length();
                for (int j = 0; j < s1Length; j++) {
                    if (s1.charAt(j) == '0') {
                        count1 = 0;
                        s3 = s3 + '0';
                    }
                    if (s1.charAt(j) == '1') {
                        ++count1;
                        s3 = s3 + '1';
                    }
                    if (count1 == 5) {
                        count1 = 0;
                        s3 = s3 + '0';
                    }
                }
                mf.appendMessage("Data after bit stuffing ", "", Color.yellow, Color.yellow);
                mf.appendMessage(s3, "", Color.yellow, Color.yellow);
                s3 = "01111110" + s3 + "01111110";
                mf.appendMessage("Data after bit stuffing with header-tailer ", "", Color.yellow, Color.yellow);
                mf.appendMessage(s3 + "\n", "", Color.yellow, Color.yellow);
                writebuffer = new BigInteger(s3, 2).toByteArray();
                output.write(writebuffer, 0, writebuffer.length);
                mf.appendMessage("Packet no " + seqNo + " is sent ", "", Color.yellow, Color.yellow);
                sleep(2000);

                long start_Time = System.currentTimeMillis();
                long elapsed_Time = 0L;
                while (elapsed_Time < 5 * 1000) {
                    //
                    elapsed_Time = (new Date()).getTime() - start_Time;
                }
            }
            /* Update AttachmentForm GUI */
            form.setMyTitle("File was sent.!");
            form.updateAttachment(false); //  Update Attachment 
            JOptionPane.showMessageDialog(form, "File successfully sent.!", "Sucess", JOptionPane.INFORMATION_MESSAGE);
            form.closeThis();
            /* Close Streams */
            output.flush();
            output.close();
            mf.appendMessage("File was sent..!", "", Color.yellow, Color.yellow);
        } catch (IOException e) {
            form.updateAttachment(false); //  Update Attachment
            System.out.println("[SendFile]: " + e.getMessage());
        } catch (InterruptedException ex) {
            Logger.getLogger(SendingFileThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
