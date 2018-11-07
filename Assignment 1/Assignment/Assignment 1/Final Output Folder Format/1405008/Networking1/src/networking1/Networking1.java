/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package networking1;

import java.io.IOException;
import java.util.Scanner;

/**
 *
 * @author Arup
 */
public class Networking1 {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        
        
        // TODO code application logic here
        Client ob=new Client();
        Thread t = new Thread(ob);
        t.start();
       //byte[] bytes = new byte[3];
//       byte[] bytes2 = new byte[3];
//       byte[] bytes3 = new byte[3];
//       bytes[1] = (byte)(bytes[1] | (1 << 7));
//       bytes2[1] = (byte)(bytes2[1] | (1 << 7));
//       bytes3[1] = (byte)(bytes3[1] | (1 << 7));
//       byte[] bytes4=Server.arrayConcatenate(bytes, bytes2, bytes3);
//        for (int i=0;i<bytes4.length;i++) {
//            System.out.println(Integer.toBinaryString(bytes4[i] & 255 | 256).substring(1));
//        }
//        
//        int x= Server.checkSum(bytes4);
        //int x=Server.getBit(bytes2[1], 0);
        //System.out.println(x);
//        int p=219;
//        byte b=(byte)p;
//        byte arr[]=new byte[6];
//        arr[0]=b;
//        arr[1]=b;
//        arr[2]=b;
//        arr[3]=b;
//        arr[4]=b;
//        arr[5]=b;
//        b=(byte)248;
//        arr[1]=b;
//        b=(byte)212;
//        arr[2]=b;
//        b=(byte)p;
//        arr[3]=b;
//        b=(byte)p;
//        arr[4]=b;
//        b=(byte)54;
//        arr[5]=b;
//        b=(byte)48;
//        arr[6]=b;
//        b=(byte)79;
//        arr[7]=b;
//        b=(byte)p;
//        arr[8]=b;
//        
//        byte[][] byte2=WorkerThread.ExtractData(arr);
//        System.out.println(byte2.length);
//        for(int m=0;m<byte2.length;m++){
//            for (int i=0;i<byte2[m].length;i++) {
//            System.out.println(Integer.toBinaryString(byte2[m][i] & 255 | 256).substring(1));
//            
//            }
//            System.out.println("\n");
//        }
//        
//        for (int i=0;i<arr.length;i++) {
//            System.out.println(Integer.toBinaryString(arr[i] & 255 | 256).substring(1));
//            
//        }
//        System.out.println("");
//        //System.out.println(Integer.toBinaryString(b & 255 | 256).substring(1));
//        byte by[]=WorkerThread.bitStaffing(arr);
//        for (int i=0;i<by.length;i++) {
//            System.out.println(Integer.toBinaryString(by[i] & 255 | 256).substring(1));
//            
//        }
//        int x= WorkerThread.checkSum(by);
//        System.out.println(x);
//        byte cy[]=WorkerThread.bitDeStaffing(arr);
//        //System.out.println(cy.length);
//        for (int i=0;i<cy.length;i++) {
//            System.out.println(Integer.toBinaryString(cy[i] & 255 | 256).substring(1));
//        }
    }
    
}
