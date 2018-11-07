package prototype.commons;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;

public class AuxiliaryMethods {
    public static ArrayList<OnlineUsers> onlineSenders;
    public static ArrayList<OnlineUsers> onlineReceivers;
    public static ArrayList<ReceiverInfo> receiverInfos;
    public AuxiliaryMethods(){ }

    public long getFolderSize(File f){
        long size =0;
        File[] file = f.listFiles();
        for(int i=0; i<file.length; i++){
            size += file[i].length();
        }
        return size;
    }

    public void createFolder(String newFolder){
        File tempDir = new File(newFolder);

        if (!tempDir.exists()) {
            System.out.println("creating directory: " + tempDir.getName());
            boolean b = false;
            try{
                tempDir.mkdir();
                b = true;
            }
            catch(Exception x){  x.toString();    }
            if(b) {    System.out.println("DIR created"); }
        }
    }


    public long myRandomNum(){
        long n, randomNum,max, min, i;
        Random rn = new Random();
        max = 10240; // 10Kb
        min = 1024; // 1Kb
        n = max - min;
        i = (long) ((rn.nextInt() + Math.sin(rn.nextDouble())*Math.log(rn.nextDouble()) )% n);
        randomNum =  min + i;

        return Math.abs(512*randomNum);
    }

    public static boolean senderIsAlreadySignedIn(int sid){
        boolean b = false;
        for(int i = 0; i< AuxiliaryMethods.onlineSenders.size(); i++){
            if( AuxiliaryMethods.onlineSenders.get(i).sid == sid){
                b = true; break;
            }
        }
        return b;
    }

    public static boolean receiverIsAlreadySignedIn(int sid){
        boolean b = false;
        for(int i = 0; i< AuxiliaryMethods.onlineReceivers.size(); i++){
            if( AuxiliaryMethods.onlineReceivers.get(i).sid == sid){
                b = true; break;
            }
        }
        return b;
    }


}





