/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package messenger;

import java.io.Serializable;

/**
 *
 * @author TIS
 */
public class Frame {
    
    public String frameKind ; 
    public String seqNo ;
    public String ackNo;
    public String payLoad ;
    public String checkSum ; 
    
    Frame(){
    }
    
    public String getFrameKind(){
        return frameKind ; 
    } 
    public String getSeqNo(){
        return seqNo ; 
    }
    public String getAckNo(){
        return ackNo ; 
    }
    public String getPayLoad(){
        return payLoad ; 
    }
    public String getCheckSum(){
        return checkSum ;
    }
    
    public void setFrameKind(int kind){
        this.frameKind = String.format("%8s", Integer.toBinaryString(kind)).replace(' ', '0'); 
    }
    public void setSeqNo( int seq ){
        this.seqNo=String.format("%8s", Integer.toBinaryString(seq)).replace(' ', '0');
    }
    public void setAckNo ( int ack ){
        this.ackNo = String.format("%8s", Integer.toBinaryString(ack)).replace(' ', '0');
    }
    public void setPayLoad(byte [] load){
        this.payLoad = binString(load);  
    }
    public void setCheckSum(){
         
        
        this.checkSum = String.format("%8s", Integer.toBinaryString(checkSum(this.payLoad))).replace(' ', '0');
    }
    
    public String getFrame(){
        
        String f = "01111110"+this.frameKind + this.seqNo + this.ackNo + this.payLoad + this.checkSum +"01111110";
        
        System.out.println("Frame to Send:");
        print(f);
        f= "01111110"+bitStuffing(this.frameKind + this.seqNo + this.ackNo + this.payLoad + this.checkSum )+"01111110";
        System.out.println("Frame after Stuffing:");
        print(f);
        return f;  //adding head and tail 
        
    }
    
    public  int checkSum(String str){
        int i,len,parity=0;
        len = str.length() ;

        for(i = 0 ;i<len ; i++){
            
          parity += str.charAt(i)-'0' ;

            
        }
        parity %=2 ;
        
       return parity ;

    }
    
    public  String binString(byte[] byt){
       // byte[] byt = (byte[])obj;
        String payload=new String();
        int i,len;
        len = byt.length ; 
        for ( i=0; i<len ; i++){
                String bstr = String.format("%8s", Integer.toBinaryString(byt[i] & 0xFF)).replace(' ', '0');
               // System.out.println(bstr);
               payload+=bstr ;
        }
        
        return payload ; 
        
    }
    
    public String bitStuffing(String str){
        
        int i , len , count = 0;
        String stuffed = new String () ; 
        len = str.length() ; 
        for( i =0 ;i<len ; i++ ){
            
            if ( '1'==str.charAt(i)){
                count++ ;
                stuffed+='1';
            }
            else {
                stuffed+='0';
                count = 0 ;
            }
            if ( count == 5 ){
                stuffed+='0' ;
                count = 0 ;
            }
            
        }
        return stuffed; 
    }
    
    public String bitDestufffing (String str ){
       
        int i , len , count = 0;
        String destuffed = new String () ; 
        len = str.length() ; 
        for( i =0 ;i<len ; i++ ){
            
            if ( '1'==str.charAt(i)){
                count++ ;
                destuffed+='1';
            }
            else {
                destuffed+='0';
                count = 0 ;
            }
            if ( count == 5 ){

                if(str.charAt(i+1)=='0'){
                    i=i+1;
                    count=0;
                }
                else{
                    destuffed+='1';
                    i=i+1;
                }

            }
            
        }
        return destuffed;
        
    }
    
    public void deFraming(String str){
        int len = str.length(),i ; 
        int oldSum, newSum ;
   
        this.frameKind =     str.substring(8,16);
        
        this.seqNo =         str.substring(16, 24);
        this.ackNo =         str.substring(24, 32);
        this.payLoad =       str.substring(32, len-16) ;
        this.checkSum =      str.substring(len-16, len-8); 
        
        System.out.println("RECEIVED:");
        print(this.frameKind+this.seqNo+this.ackNo+this.payLoad+this.checkSum);
        
    }
    

    
    
    
    public byte[] binStringToByte(String f){
            int i = 0 ;
            byte [] byt = new byte[f.length()];
            for(String str : f.split("(?<=\\G.{8})")){
                byt[i]=(byte)Integer.parseInt(str,2) ;
                i++;
            }
            return byt;
    }
    public String binStringToString(String in){
        
            
            String output = "";
            for(int i = 0; i <= in.length() - 8; i+=8)
            {
                int k = Integer.parseInt(in.substring(i, i+8), 2);
                output += (char) k;
            }  
             
             return output ;
     }

     public void print (String s){
         int i , len ;
         len = s.length();
         for( i = 0 ; i<len ; i++){
             if(i%8==0){ System.out.print(" ");System.out.print(s.charAt(i));}
             else System.out.print(s.charAt(i));
         }
         System.out.println();
     }
}