/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spclient;

import byteArrayStuffs.ByteArrayBitIterable;
import java.io.IOException;


/**
 *
 * @author Farhan
 */



public final class DeStuffer {
    private final byte[] toDeStuff;
    private int seqNumber;
    private int type;
    public boolean isValid;
    
    
    
    public DeStuffer(byte[] input) throws IOException, ClassNotFoundException
    {
        toDeStuff=input;
        type=0;
        seqNumber=0;
        isValid=true;
    }
    
  
    
    public byte[] deSeal(byte[] input)
    {
        int len=input.length;
        ByteArrayBitIterable iter=new ByteArrayBitIterable(input);
        byte[] ret=new byte[len];
        
        byte mask=0; 
        byte lepa=-128;
        int bitCnt=0;
        int onCnt=0;
        int pos=0;
        int lock=0;
        int startEndBitCnt=0;
        
        
        for (boolean b:iter)    {
            if(lock==0)    {
                if(b==true)    startEndBitCnt++;
                if(startEndBitCnt==6)    {
                    lock=1;
                    startEndBitCnt=0;
                }
            }
            else if(lock==1)    lock=2;
            else    {
                if(b==true)    {
                    onCnt++;
                    mask|=lepa;
                    lepa>>=1;
                    lepa&=127;
                    bitCnt++;

                }
                else    {
                    if(onCnt==6)    break;
                    else if(onCnt<5)    {
                        lepa>>=1;
                        lepa&=127;
                        bitCnt++;
                    }

                    onCnt=0;
                }

                if(bitCnt==8)    {
                    ret[pos]=mask;
                    pos++;
                    bitCnt=0;
                    mask=0;
                    lepa=-128;
                } 
            }
        }
        
        
        byte[] finalRet=new byte[pos];
        
        System.arraycopy(ret,0,finalRet,0,pos);
        
        return finalRet;
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
    
    
    public byte[] deStuff()
    {
        if(toDeStuff.length<7)    isValid=false;
        
        
        byte[] actual=deSeal(toDeStuff);
         
        type=(int)actual[0];
        seqNumber=((int)actual[1])*128+(int)actual[2];
        int len=actual.length;
        byte[] ret=new byte[len-4];
        for(int i=3;i<len-1;i++)    {
            ret[i-3]=actual[i];
        }
        byte checkSum=calcChecksum(ret);
        if(checkSum!=actual[len-1])    isValid=false;
        
        
        return ret;
    }
    
    public int getSeqNo()
    {
        return seqNumber;
    }
    
    public int getType()
    {
        return type;
    }
    
    
}
