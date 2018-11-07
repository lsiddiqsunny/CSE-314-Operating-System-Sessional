/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spserver;

import byteArrayStuffs.ByteArrayBitIterable;

/**
 *
 * @author Farhan
 */
public class BitStuffer {
    private final byte[] toStuff;
    private final int seqNumber;
    private final int type;
   
    
    
    public BitStuffer(byte[] input,int typ,int seqNo)
    {
        toStuff=input;
        type=typ;
        seqNumber=seqNo;
    }
    
    private byte calcChecksum(byte[] input)
    {
        byte ret=0;
        ByteArrayBitIterable iter=new ByteArrayBitIterable(input);
        for (boolean b:iter)    {
            if(b==true)    ret++;     
        }
        return ret;
    }
    
    
    private byte[] seal(byte[] input)
    {   
        int len=input.length;
        ByteArrayBitIterable iter=new ByteArrayBitIterable(input);
        byte[] ret=new byte[2*len];
        
        ret[0]=0b1111110;
        
        byte mask=0;
        byte lepa=-128;
        int bitCnt=0;
        int onCnt=0;
        int pos=1;
        
        for (boolean b:iter)    {
            if(b==true)    {
                onCnt++;
                mask|=lepa;
            }
            else    {
                onCnt=0;
            }
            lepa>>=1;
            lepa&=127;
            bitCnt++;
            if(bitCnt==8)    {
                ret[pos]=mask;
                pos++;
                bitCnt=0;
                mask=0;
                lepa=-128;
            }
            
            
            if(onCnt==5)    {
                onCnt=0;
                lepa>>=1;
                lepa&=127;
                bitCnt++;
                if(bitCnt==8)    {
                    ret[pos]=mask;
                    pos++;
                    bitCnt=0;
                    mask=0;
                    lepa=-128;
                }
            }
        }
        
        byte[] tail=new byte[1];
        tail[0]=0b1111110;
        ByteArrayBitIterable tailIt=new ByteArrayBitIterable(tail);
        for(boolean b:tailIt)    {
            if(b==true)    {
                onCnt++;
                mask|=lepa;
            }
            lepa>>=1;
            lepa&=127;
            bitCnt++;
            if(bitCnt==8)    {
                ret[pos]=mask;
                pos++;
                bitCnt=0;
                mask=0;
                lepa=-128;
            }
        }
        
        if(bitCnt!=0)    ret[pos]=mask;
        
        
        return ret;
    }
    
    
    public byte[] stuff()
    {
        int len=toStuff.length;
        byte[] preStuff=new byte[len+4];
        preStuff[0]=(byte)type;
        preStuff[1]=(byte)(seqNumber/128);
        preStuff[2]=(byte)(seqNumber%128);
        System.arraycopy(toStuff,0,preStuff,3,len);
        preStuff[len+3]=calcChecksum(toStuff);
        byte[] ret=seal(preStuff);
        return ret;
    }
    
    
}
