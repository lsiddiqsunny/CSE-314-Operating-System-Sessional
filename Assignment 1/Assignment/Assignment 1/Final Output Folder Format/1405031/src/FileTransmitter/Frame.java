/*ssss
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package FileTransmitter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.BitSet;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 *
 * @author saad
 */
public class Frame{   
    public  byte frametype ;
    public  byte seqOrAckno ;
    public  byte[] payload ;
    public Frame(byte Frametype, byte SeqOrAckno , byte [] Payload )
    {
        frametype= Frametype;
        payload =  Payload ;
        seqOrAckno = SeqOrAckno ;
    }
    public Frame(byte [] sbyteArray)
    {
        //System.err.println("before deStuffing");
        //printB(byteArray);
        byte [] byteArray = deStuffing(Arrays.copyOfRange(sbyteArray, 1, sbyteArray.length-1));
        //System.err.println("after deStuffing");
        //printB(byteArray);
        frametype = byteArray[0];
        seqOrAckno = byteArray[1];
        payload = Arrays.copyOfRange(byteArray, 2, byteArray.length-1);
    }
    public boolean isEOTframe()
    {
        byte eot = 0xe ;
        return ( eot == seqOrAckno );
    }
    public boolean isPositive()
    {
        byte all = ~0; 
        return ( all == payload[0] );
    }
    public boolean hasError(byte [] byteArray)
    {
        byte checksum = checkSum(deStuffing(byteArray));
        byte all = ~0 ;
        //System.err.println("checksum value is : ");
        //print(checksum);
        return (checksum!=all);
    }
    public byte [] synthesis()
    {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
        //System.err.println("before checksum");
        //print();
        byte checksum = add(frametype,seqOrAckno);
        checksum = add(checksum,checkSum(payload));
        byte all = ~0;
        checksum = (byte) (checksum ^ all) ;
        //System.err.println("after checksum");
        //print();
        
        try {
            outputStream.write( frametype );
            outputStream.write( seqOrAckno );
            outputStream.write( payload );
            outputStream.write( checksum );
        } catch (IOException ex) {
            //Logger.getLogger(Frame.class.getName()).log(Level.SEVERE, null, ex);
        }

        byte ret[] = outputStream.toByteArray( );
        //System.err.println("ret == ");
        //printB(ret);
        //System.err.println("before Stuffing");
        //printB(ret);
        ret = Stuffing(ret);
        //System.err.println("after Stuffing");
        
        //printB(ret);
        ByteArrayOutputStream outputStream2 = new ByteArrayOutputStream( );
        try {
            outputStream2.write( 0x7e );
            outputStream2.write( ret );
            outputStream2.write( 0x7e );
        } catch (IOException ex) {
            //Logger.getLogger(Frame.class.getName()).log(Level.SEVERE, null, ex);
        }
        byte [] ret2 = outputStream2.toByteArray();
        return ret2 ;      
    }
    
    
    public byte checkSum(byte [] byteArray)
    {
        //System.err.println("checkbytearray ");
        //printB(byteArray);
        byte ret = byteArray[0] ;
        for(int i = 1 ; i < byteArray.length ; i++ )ret=add(ret,byteArray[i]);
        return ret ;
    }
    public byte add(byte a,byte b)
    {
        //System.err.print("a = ");
        //print(a);
        //System.err.println("");
        //System.err.print("b = ");
        //print(b);
        //System.err.println("");
        byte ret = a ;
        byte all = ~0 ;
        byte baki = (byte) (all - ret) ;
        //System.err.print("baki = " );
        //print(baki);
        //System.err.println("");
        ret = (byte)(a+b);
        //System.err.print("ret = " );
        //print(ret);
        //System.err.println("");
        if(cmp(baki,b) )ret++;
        //System.err.print("ret = " );
        //print(ret);
        //System.err.println("");
        return ret ;
    }
    public static boolean cmp(byte a , byte b)
    {
        for(int i = 7 ; i > -1 ; i-- )
        {
            if((a&(1<<i))==0&&(b&(1<<i))!=0){return true ;}
            if((a&(1<<i))!=0&&(b&(1<<i))==0){return false ;}
        }
        return false ;
    }
    public static byte[] Stuffing(byte[] newByteArray) {
        //System.err.println("newByteArray");
        //printB(newByteArray);
        
        BitSet bPayload = new BitSet();
        BitSet bitset = new BitSet() ;
        for (int i = 0; i < newByteArray.length * 8; i++) {
            if ((newByteArray[i / 8 ] & (1 << (7-(i % 8)))) > 0) {
                    bitset.set(i,true);
            }
            else bitset.set(i,false);
        }
        int cnt = 0 , j = 0;
        //System.err.println("bitset ");
        //print(bitset);
        //System.err.println("bitset length = " + bitset.length() );
        int delim = 0 ;
        for(int i = 0; i < newByteArray.length * 8 ; i++)
        {
            if(bitset.get(i))
            {
                bPayload.set(j, true);
                cnt++;
                j++;
                if(cnt==5)
                {
                     bPayload.set(j,false);
                     cnt=0;
                     j++;
                     delim++;
                }
            }
            else
            {
                bPayload.set(j,false);
                cnt=0;
                j++;
            }
        }
        //System.err.println("bpayload ");
        //print(bPayload);
        //System.err.println("newByteArrayLength = "+ newByteArray.length);
        int payloadSize = newByteArray.length + (delim/8) ;
        if(delim%8>0)payloadSize++;
        //System.err.println("payloadSIze = "+ payloadSize);
        byte[] payload = new byte[payloadSize] ;
        int bPayloadSize = newByteArray.length * 8 + delim ;
        for(int i = 0; i < bPayloadSize;i++)
        {
            if(bPayload.get(i))payload[i/8] = (byte) (payload[i/8]|( 1 << (7-(i%8)) )) ; 
        }
        //System.err.println("payload");
        //printB(payload);
        return payload ;
    }
    public static byte[] deStuffing(byte[] newByteArray) {
       // System.err.println("newByteArray");
        //printB(newByteArray);
        
        BitSet bPayload = new BitSet();
        BitSet bitset = new BitSet() ;
        for (int i = 0; i < newByteArray.length * 8; i++) {
            if ((newByteArray[i / 8 ] & (1 << (7-(i % 8)))) > 0) {
                    bitset.set(i,true);
            }
            else bitset.set(i,false);
        }
        int cnt = 0 , j = 0, delim = 0 ;
        //System.err.println("bitset");
        //print(bitset);
        for(int i = 0; i < newByteArray.length * 8 ; i++)
        {
            if(bitset.get(i))
            {
                bPayload.set(j,true);
                cnt++;
                j++;
                if(cnt==5)
                {
                     i++;
                     cnt=0;
                     delim++;
                }
            }
            else
            {
                bPayload.set(j,false);
                cnt=0;
                j++;
            }
        }
        //System.err.println("bpayload ");
        //print(bPayload);
        int payloadSize =j/8 ;
        j = payloadSize*8;
        byte[] payload = new byte[payloadSize] ;
        for(int i = 0; i < j;i++)
        {
            if(bPayload.get(i))payload[i/8] = (byte) (payload[i/8]|( 1 << (7-(i%8)) )) ; 
        }
        //System.err.println("payload");
        //printB(payload);
        return payload ;
    }
    public void print()
    {
        System.out.println("frametype :> " );
        print(frametype);
        System.err.println("");
        System.out.println("seqOrAckno :> ");
        print(seqOrAckno);
        System.err.println("");
        System.out.println("payload :> ");
        printB(payload);     
        System.err.println("");
    }
     public void printB( byte [] byteArray)
    {
        for(int i=0;i < byteArray.length ; i++ )print(byteArray[i]);
        System.err.println("");
    }
    public void print( byte byt)
    {
        for(int i = 7 ; i > -1 ; i--)
        {
            if((byt&(1<<i))==0) { System.err.print("0");
            } else {
                System.err.print("1");
             }
        }
        //System.err.println("");
    }
    public void print(BitSet bitset )
    {
        for(int i = 0 ; i < bitset.size() ; i++)
        {
            if((bitset.get(i))) { System.err.print("1");
            } else {
                System.err.print("0");
             }
        }
        System.err.println("");
    }

    
}
