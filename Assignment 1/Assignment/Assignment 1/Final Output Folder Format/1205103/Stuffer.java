/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package datalinklayer;

import java.util.BitSet;

/**
 *
 * @author rafs
 */
public class Stuffer {
    public static void main(String[] args){
        byte[] frame1={0b00111111,0b00101011};
        System.out.println("Stuffing");
        byte[] frame2= bitStuff(frame1);
        //for(int i=0;i<frame2.length;i++) System.out.println(i+":"+frame2[i]);
        System.out.println("Destuffing");
        byte[] frame3=bitDestuff(frame2);
        //for(int i=0;i<frame3.length;i++) System.out.println(i+":"+frame3[i]);
    }
    
    
    public static byte[] bitStuff(byte[] frame){
        //System.out.println(frame.length);
        BitSet realBits=new BitSet();
        //System.out.println(frame.length*8);
        for(int i=0;i<frame.length*8;i++){
            byte b=frame[i/8];
            byte mask=(byte)(1<<(7-i%8));
            if((b&mask)>0) realBits.set(i);
            else realBits.clear(i);
        }
        //System.out.println(realBits.length());
        System.out.println("Before framing:");
        for(int i=0;i<frame.length*8;i++) {
            if(realBits.get(i)) System.out.print(1);
            else System.out.print(0);
            if(i%8==7) System.out.print(" ");
        }
        System.out.println();          
        
        BitSet stuffedBits=new BitSet();
        stuffedBits.clear(0);
        stuffedBits.clear(7);
        for(int i=1;i<7;i++) stuffedBits.set(i);
        int j=8;
        int run=0;
        for(int i=0;i<frame.length*8;i++){
            if(realBits.get(i)){
                stuffedBits.set(j);
                j++;
                run++;
                if(run>4){
                    run=0;
                    stuffedBits.clear(j);
                    j++;
                }
            }
            else{
                run=0;
                stuffedBits.clear(j);
                j++;
                
            }
        }
        stuffedBits.clear(j);
        stuffedBits.clear(j+7);
        for(int i=j+1;i<j+7;i++) stuffedBits.set(i);        
        int size=j+8;
        if(size%8!=0){
            int extra=8-size%8;
            for(int i=0;i<extra;i++){
                stuffedBits.clear(size+i);
            }
            
            size+=extra;
            
        }
        System.out.println("After framing:");
        for(int i=0;i<size;i++) {
            if(stuffedBits.get(i)) System.out.print(1);
            else System.out.print(0);
            if(i%8==7) System.out.print(" ");
        }
        System.out.println();       
        
        byte[] stuffedFrame=new byte[size/8];
        for(int i=0;i<stuffedFrame.length;i++){
            
            int sum=0;
            for(j=0;j<8;j++){
                if(stuffedBits.get((i*8+j))) sum+=(1<<(7-j%8));
            }
            stuffedFrame[i]=(byte)sum;
        }
        
       
        return stuffedFrame;
    }
    
    public static byte[] bitDestuff(byte[] stuffedFrame){
        //System.out.println(frame.length);
        BitSet stuffedBits=new BitSet();
        //System.out.println(stuffedFrame.length*8);
        for(int i=0;i<stuffedFrame.length*8;i++){
            byte b=stuffedFrame[i/8];
            byte mask=(byte)(1<<(7-i%8));
            if((b&mask)>0 ||(b<0 && i%8==0)) stuffedBits.set(i); //the or condition is crucial
            else stuffedBits.clear(i);
          
        }
        
        System.out.println("Received Frame:");
        for(int i=0;i<stuffedFrame.length*8;i++) {
            if(stuffedBits.get(i)) System.out.print(1);
            else System.out.print(0);
            if(i%8==7) System.out.print(" ");
        }
        System.out.println();
        
        int startFlag=-1,endFlag=-1;
        int run=0,lastZero=-1;
        for(int i=0;i<stuffedFrame.length*8;i++){
            if(stuffedBits.get(i)){
                run++;
            }
            else {
                if(run==6){
                    if(startFlag==-1) startFlag=lastZero;
                    else endFlag=lastZero;
                }
                run=0;
                lastZero=i;
            }
        
        }
        
        if(startFlag<0 || endFlag<0) System.out.println("Flags not found");
        //else System.out.println("start: "+startFlag+" end: "+endFlag);
        
        System.out.println("After removing flags:");
        for(int i=startFlag+8;i<endFlag;i++) {
            if(stuffedBits.get(i)) System.out.print(1);
            else System.out.print(0);
            if(i%8==7) System.out.print(" ");
        }        
        System.out.println();
        
        BitSet realBits=new BitSet();
        int size=endFlag-startFlag-8;
        int k=0;
        for(int j=startFlag+8;j<endFlag;j++,k++){
            if(stuffedBits.get(j)){
                realBits.set(k);
                run++;
                if(run==5){
                    run=0;
                    size--;
                    j++;
                }                
            }
            else{
                run=0;
                realBits.clear(k);
                
            }
        }
        
        if(size%8!=0) System.out.println("Error in destuffing");
  
        System.out.println("After destuffing:");
        for(int i=0;i<size;i++) {
            if(realBits.get(i)) System.out.print(1);
            else System.out.print(0);
            if(i%8==7) System.out.print(" ");
        }
        System.out.println();
        
        //System.out.println("size: "+size);
        byte[] destuffedFrame=new byte[size/8];
        for(int i=0;i<destuffedFrame.length;i++){
            
            int sum=0;
            for(int j=0;j<8;j++){
                if(realBits.get((i*8+j))) sum+=(1<<(7-j%8));
            }
            destuffedFrame[i]=(byte)sum;
        }
        
        return destuffedFrame;       
        
        
        
    }        
    
        
    
    
    
    
}
