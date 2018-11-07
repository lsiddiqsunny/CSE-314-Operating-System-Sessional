package ServerPackage;

import Utility.FrameInfo;
import Utility.clientUtility;
import Utility.messageObject;
import Utility.recievedFileInfo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.Socket;
import java.util.Random;
import java.util.Vector;

/**
 * Created by Asus on 9/24/2017.
 */
class clientThread implements Runnable {
  private Socket clientSocket;
  private Server theServer;
  private clientUtility cu;
  private int clientId;
  private Vector<Long>partial;
  private byte[] dummy;
  Thread t;

  public clientThread(Server s, Socket clientSocket) {
    this.clientSocket = clientSocket;
    theServer = s;
    clientId = 0;
    cu = new clientUtility(this.clientSocket);
    t = new Thread(this);
    dummy=new byte[1];
    dummy[0]=0;
    t.start();

  }

  public void run() {

    while (true) {
      try {
        messageObject message = cu.read();
        if (message != null) {
          if (message.getType() == 1) {
            int clientId = message.getClientId();
            if (theServer.inf.hasIP(clientId) == false) {
              theServer.inf.putClientIP(clientId, clientSocket.getRemoteSocketAddress().toString());
              theServer.inf.putClientUtility(clientId, cu);
              partial = new Vector<>();
              this.clientId = clientId;
              messageObject wm = new messageObject(2);
              wm.setMessage("Success");
              cu.write(wm);
              System.out.println("Client " + clientId + " Successfully logged in.");
            } else {
              messageObject wm = new messageObject(2);
              wm.setMessage("Failure");
              cu.write(wm);
            }
          } else if (message.getType() == 3) {
            if (theServer.inf.getClientUtility(message.getReciever()) == null) {
              messageObject m = new messageObject(4);
              m.setMessage("Failiure-recieverOffline");
              cu.write(m);
            } else if (theServer.inspectRembuf(message.getFilesize()) == false) {
              messageObject m = new messageObject(4);
              m.setMessage("Failiure-bufferOverflow");
              cu.write(m);
            } else {

              Random rand = new Random();
              long sz = message.getFilesize();
              int maxchunksize;

       /*      if (sz > 256) {
                maxchunksize = (rand.nextInt(128) + 128);
              } else {
                maxchunksize = rand.nextInt((int) sz) + 1;
              } */


              maxchunksize=8;


              long fileId = theServer.getFileIdgen();
              FileInfo ff = new FileInfo(fileId, message.getFilename(), sz,
                      message.getSender(), message.getReciever(), maxchunksize);
              theServer.inf.putFileInfo(fileId, ff);
              partial.addElement(fileId);
              messageObject m = new messageObject(4);
              m.setMessage("success");
              m.setFileId(fileId);
              m.setMaxchunksize(maxchunksize);
              cu.write(m);
            }
          } else if (message.getType() == 5) {

            FileInfo ff = theServer.inf.getFileInfo(message.getFileId());
            FrameInfo fr = cu.bitDestuff(message.getFilebytes());


            System.out.println("\n\nRecieved Stuffed Frame: ");
            cu.printByteArray(message.getFilebytes());
            System.out.println("After Destuff: ");
            System.out.println(fr);
            System.out.println("Payload: ");
            cu.printByteArray(fr.payload);


            if(fr.hasFrameError==false && fr.seq==ff.currSeqNo){
              ff.currSeqNo=(ff.currSeqNo+1)%8;
              FileOutputStream fos = new FileOutputStream(ff.fileInServer, true);
              if (ff.getStoredsize() + fr.payload.length < ff.fileSize) {
                fos.write(fr.payload);
                ff.setStoredsize(ff.getStoredsize() + fr.payload.length);

                messageObject m = new messageObject(6);
                m.setFileId(message.getFileId());
            //    m.setMessage("acknowledgement");
                byte[] ackframe=cu.bitstuff(dummy,2,fr.seq,1,dummy.length);
                m.setFilebytes(ackframe);
                cu.write(m);
              } else {
                int rem = (int) (ff.fileSize - ff.getStoredsize());

                fos.write(fr.payload);
                ff.setStoredsize(ff.getStoredsize() + rem);

                messageObject m = new messageObject(6);
                m.setFileId(message.getFileId());
           //     m.setMessage("acknowledgement_last_chunk");
                byte[] ackframe=cu.bitstuff(dummy,2,fr.seq,2,dummy.length);
                m.setFilebytes(ackframe);
                cu.write(m);
                System.out.println("File "+ff.fileName+" with ID "+ff.fileId+" Successfully received");
              }

              fos.close();
            }
            else {

              if(fr.hasCerror==true){
                System.out.println("Checksum Error , No acknowledgement sent.");
              }
              else if(fr.seq!=ff.currSeqNo){
                System.out.println("Expected Sequence No. Mismatch, No acknowledgement sent.");
              }
              else{
                System.out.println("Error in frame, no acknowledgement sent.");
              }

            }

          } else if (message.getType() == 7) {
            FileInfo ff = theServer.inf.getFileInfo(message.getFileId());
            if (ff.getFileSize() == ff.getStoredsize()) {
              theServer.inf.putRecievedFileInfo(ff.getRecieverId(), ff.fileId);
              partial.removeElement(ff.fileId);
              messageObject m = new messageObject(8);
              m.setFileId(message.getFileId());
              m.setMessage("success");
              cu.write(m);
            } else {
              messageObject m = new messageObject(8);
              m.setFileId(message.getFileId());
              m.setMessage("failiure");
              cu.write(m);

              File file = new File(ff.fileInServer);
              try {
                file.delete();
              } catch (Exception e) {
         //       e.printStackTrace();
              }
              theServer.incRembuf(ff.getFileSize());
              partial.removeElement(ff.fileId);
              theServer.inf.removeFileInfo(message.getFileId());
            }
          } else if (message.getType() == 9) {
            System.out.println("timeout!");
            FileInfo ff = theServer.inf.getFileInfo(message.getFileId());
            File file = new File(ff.fileInServer);
            try {
              file.delete();
            } catch (Exception e) {
         //     e.printStackTrace();
            }
            theServer.incRembuf(ff.getFileSize());
            partial.removeElement(ff.fileId);
            theServer.inf.removeFileInfo(message.getFileId());

          } else if (message.getType() == 10) {

            Vector<recievedFileInfo> r = new Vector<>();
            Vector<Long> recievedFileIds = theServer.inf.getRecievedFileInfo(clientId);

            for (int i = 0; i < recievedFileIds.size(); i++) {
              long fileId = recievedFileIds.elementAt(i);
              FileInfo ff = theServer.inf.getFileInfo(fileId);
              recievedFileInfo rr = new recievedFileInfo(fileId, ff.getFileName(), ff.getFileSize(), ff.getSenderId());
              r.addElement(rr);
            }

            messageObject m = new messageObject(11);
            m.setR(r);

            cu.write(m);

          } else if (message.getType() == 12) {

            long fileId = message.getFileId();
            FileInfo ff = theServer.inf.getFileInfo(fileId);
            int mx = 65536;

            try(FileInputStream fis = new FileInputStream(ff.fileInServer)) {
              int ava = fis.available();

              while (ava > 0) {

                byte[] b = new byte[mx];

                if (b.length < ava) {
                  fis.read(b);
                  messageObject m = new messageObject(13);
                  m.setFileId(fileId);
                  m.setFilebytes(b);
                  m.setFilesize(b.length);

                  boolean bl = cu.write(m);
                  if(bl==false){
                    throw new Exception("Cannot write");
                  }
                } else if (b.length >= ava) {
                  fis.read(b, 0, ava);
                  messageObject m = new messageObject(13);
                  m.setFilesize(ava);
                  m.setFilebytes(b);

                  boolean bl = cu.write(m);
                  if(bl==false){
                    throw new Exception("Cannot write");
                  }

                  messageObject m2 = new messageObject(14);
                  m2.setMessage("Complete");

                  boolean b2 = cu.write(m2);
                  if(b2==false){
                    throw new Exception("Cannot write");
                  }

                  break;

                } else {
                  break;
                }

                ava = fis.available();

              }
            }catch (Exception e){
     //         e.printStackTrace();
            }

          }

          else if(message.getType()==15){
            if(message.getMessage().equals("success")){
              long fileId = message.getFileId();
              FileInfo ff=theServer.inf.getFileInfo(fileId);
              File f=new File(ff.fileInServer);
              try {
                f.delete();
              }
              catch (Exception e){
           //     e.printStackTrace();
              }
              theServer.incRembuf(ff.getFileSize());
              theServer.inf.removeFileInfo(fileId);
              theServer.inf.removeRecievedFileInfo(clientId,fileId);
            }
          }

          else if(message.getType()==16){
            long fileId = message.getFileId();
            FileInfo ff=theServer.inf.getFileInfo(fileId);
            File f=new File(ff.fileInServer);
            try {
              f.delete();
            }
            catch (Exception e){
      //        e.printStackTrace();
            }
            theServer.incRembuf(ff.getFileSize());
            theServer.inf.removeFileInfo(fileId);
            theServer.inf.removeRecievedFileInfo(clientId,fileId);

            messageObject m = new messageObject(17);
            m.setMessage("deleted");
            cu.write(m);
          }
          else if(message.getType()==18){
            throw new Exception("Logout");
          }

        }

        else {

          throw new Exception("Error");
        }
      } catch (Exception e) {
    //    e.printStackTrace();
        System.out.println("Closing connections for " + clientId);
        for(int i=0;i<partial.size();i++){
          FileInfo ff = theServer.inf.getFileInfo(partial.elementAt(i));
          File f = new File(ff.fileInServer);

          try{
            f.delete();
          }catch (Exception e2){
     //       e.printStackTrace();
          }
          theServer.incRembuf(ff.getFileSize());
          theServer.inf.removeFileInfo(partial.elementAt(i));
        }
        cu.closeConnection();
        try {
          clientSocket.close();
        } catch (Exception e1) {
    //      e.printStackTrace();
          System.out.println("Error closing sockets.");
        }
        break;
      }
    }
    theServer.inf.removeClient(clientId);
  }
}
