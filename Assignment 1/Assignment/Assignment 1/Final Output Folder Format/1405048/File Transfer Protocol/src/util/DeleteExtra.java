package util;

import java.io.File;
import java.io.IOException;

/**
 * @author ANTU on 29-Sep-17.
 * @project Socket
 */
public class DeleteExtra {
    public static void main(String[] args){
        mainDeleteExtra();
//        checkoutFrameManager();
//        sampleCheck();
    }

    private static void sampleCheck() {
        String temp = "hekjbjkbjjjjllo World.txt";
        byte[] take = temp.getBytes();
        FrameManager frameManager = new FrameManager(temp,0,1,0);
        byte[] tempBytes = frameManager.getMainFrame();
        frameManager = new FrameManager(tempBytes);
        String chk = frameManager.getFileName();
        System.out.println(chk);
    }

    private static void mainDeleteExtra() {
        File senderDirectory = new File("Client File/");
        File receiverDirectory = new File("Receiver File/");

        deleteFiles(senderDirectory,false);
        deleteFiles(receiverDirectory,true);
        System.out.println("Delete Successful!");
        if(senderDirectory.mkdirs()) System.out.println("Client File folder created.");
        if(receiverDirectory.mkdirs()) System.out.println("Receiver File folder created.");
    }

    private static void checkoutFrameManager() {
        File sampleFile = new File("D:\\Google Drive\\Study\\L3T2\\Networking Lab\\Sample.txt");
        if(sampleFile.exists()) System.out.println("Sample file Ok");
        int kind = 0, seqNo = 1, ackNo = 0;
        FrameManager senderSide = new FrameManager(sampleFile,kind,seqNo,ackNo);
//        String mainFrame = senderSide.getMainFrame();

        byte[] mainFrameBytes = senderSide.getMainFrame();

        System.out.println("In deleteExtra : stuffedByteLength : "+ mainFrameBytes.length);
        System.out.println("Sender Side Complete. Now Server Side");

        String mainFrameFromBytes = new String(mainFrameBytes);

        System.out.println("In DeleteExtra : "+mainFrameFromBytes);

        FrameManager serverSide = new FrameManager(mainFrameBytes);




        File checkOutFile = new File("D:\\Google Drive\\Study\\L3T2\\Networking Lab\\checkout.txt");
        try {
            checkOutFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
//        serverSide.writePayloadToFile(checkOutFile);


        System.out.println("Check");

/*
        checkOutFile = new File("D:\\Google Drive\\Study\\L3T2\\Networking Lab\\checkThisout.txt");
        try {
            checkOutFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            FileOutputStream fos = new FileOutputStream(checkOutFile);
            fos.write(sampleBytes);
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        */
    }

    public static void deleteFiles(File directory,boolean skip){
        //if(directory.isFile()) return;
        File[] listOfFiles = directory.listFiles();
        for(File file: listOfFiles){
            if(file.isDirectory()) deleteFiles(file,skip);
            else{
                if(!skip && (file.getName().equals("Hello.txt") || file.getName().equals("PDF.pdf"))) continue;
                file.delete();
            }
        }
        directory.delete(); //ghapla
    }

}
