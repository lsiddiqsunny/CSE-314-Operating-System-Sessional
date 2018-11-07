/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Offline;

import java.util.ArrayList;
import java.util.zip.Checksum;

/**
 *
 * @author ASUS
 */

public class Stuffing {

    byte[] a;
    ArrayList<Boolean>output;
    int count=0;
    int seq;
    
    int checksum; 
    public Stuffing(){
       
    }
    
    public Stuffing(byte[] a,int seq) {
        this.a=a;
        this.seq=seq;
        output=new ArrayList<>();
    }
   
    public ArrayList<Boolean> performStuffing(){
        for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < 8; j++) {
                if((a[i]>>j & 1)==1){
                    checksum++;
                    output.add(true);
                    count++;
                    if (count>2) {
                        output.add(false);
                        count=0;
                    }
                }
                else{
                    output.add(false);
                    count=0;
                }
            }
 
        }
        for (int i = 0; i < 8; i++) {
            if ((seq>>i & 1) == 1) {
                output.add(true);
            }
            else output.add(false);
            
        }
        checksum=checksum%255;
        for (int i = 0; i < 8; i++) {
            if ((checksum>>i & 1) == 1) {
                output.add(true);
            }
            else output.add(false);
            
        }
        
        return output;
    }
    
    void printBit(byte[] a){
        for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < 8; j++) {
                if((a[i]>>j & 1)==1){
                    System.out.print("1");
                  
                }
                else{
                    System.out.print("0");
                }
            }
            System.out.print("  ");
 
        }
        
    }
    void printStaffedbits(){
        int i=0;
        while(i<output.size()){
            if (i % 8 == 0) {
                System.out.print("  ");
            }
            if(output.get(i)==true){
                System.out.print("1");
            }
            else{
                System.out.print("0");
            }
            
            i++;
        }
    }
    public byte[] performDestaffing(ArrayList<Boolean> output){
        
        byte a[]=new byte[1024];
        int k=0,count=0;
       for(int i=0;i<a.length;i++){
           int j=0;
           if (k>output.size()-16) {
               break;
           }
           while (j<8) {
               if (k>output.size()-16) {
                    break;
                   }
               if(output.get(k)==true){
                   //System.out.print("true ");
                   a[i]=(byte) (a[i] | (1<<j));
                   count++;
                   j++;
               }
               else{
                   //System.out.print("false ");
                   if(count!=3){
                   a[i]=(byte) (a[i] | (0<<j));
                   j++;
                   }
                   count=0;
               }
               k++;
              
           }
       }   
       
       return a;
        
        
    }
    public int getSeqNum(ArrayList<Boolean> output){
        byte b=(byte)0;
        int k=0;
        for (int i = output.size()-16; i < output.size()-8; i++) {
            if(output.get(i)==true){
                b=(byte)( b | (1<<k));
                //System.out.println("true ");
            }
            else{b=(byte)( b | (0<<k));
                //System.out.println("false ");
               }
            k++;
            
        }
        int a=(int) b;
        //System.out.println(a);
        return a;
    
    }
}
