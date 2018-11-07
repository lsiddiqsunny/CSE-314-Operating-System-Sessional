/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.nio.ByteBuffer;

/**
 *
 * @author ASUS
 */
public class serverProcessByte {
    serverProcessByte()
    {
        System.out.println("Server process class created");
    }
    
    public byte[] addFlag(byte[]nb)
    {
      byte []fbyte=new byte[nb.length+2];
      fbyte[0]=0b01111110;
      for(int i=1;i<=nb.length;i++)
      {
          fbyte[i]=nb[i-1];
      }
      fbyte[nb.length+1]=0b01111110;
      return fbyte;
    }
    
    public byte[] extractFlag(byte[] nb)
    {
        byte [] res=new byte[nb.length-2];
        int length = nb.length;
        for(int i=1;i<length-1;i++)
        {
            res[i-1]=nb[i];
        }
        return res;
    }
    
    
    public byte[] addSeq(int se,byte[] payload)
    {
         ByteBuffer bf=ByteBuffer.allocate(4);//bits of checksum == 4*8 = 32
        bf.putInt(se);
        byte []seq = bf.array();
        int s=seq.length;
        int p=payload.length;
        int length=s+p;
        byte [] sbyte=new byte[length];
        for(int i=0;i<s;i++)
        {
            sbyte[i]=seq[i];
        }
        for(int i=0;i<p;i++)
        {
            sbyte[i+s]=payload[i];
        }
        return sbyte;
        
    }
    
    
    public int getSeq(byte[] nb)
    {
        int seq=0;
        byte [] sbyte=new byte[4];
        for(int i=0;i<4;i++)
        {
            sbyte[i]=nb[i];
        }
        ByteBuffer wr= ByteBuffer.wrap(sbyte);
        seq=wr.getInt();
        return seq;
    }
    
    public byte[] extractSeq(byte [] nb)
    {
        byte []edbyte=new byte [nb.length-4];
        int length=nb.length;
        for(int i=4;i<length;i++)
        {
            edbyte[i-4]=nb[i];
        }
        return edbyte;
    }
    
    
    
    public int getCheckSum(byte[] nb)
    {
        int sum=0;
        byte [] sbyte=new byte[4];
        int length=nb.length-4;
        for(int i=0;i<4;i++)
        {
            sbyte[i]=nb[length+i];
        }
        ByteBuffer wr= ByteBuffer.wrap(sbyte);
        sum=wr.getInt();
        return sum;
    }
    
    
    public byte[] extractCheck(byte [] nb)
    {
        byte []edbyte=new byte [nb.length-4];
        int length=nb.length;
        for(int i=0;i<length-4;i++)
        {
            edbyte[i]=nb[i];
        }
        return edbyte;
    }

    public int countOne(byte [] nb)
    {
        int length =nb.length;
        int mask = 0b10000000;
        int flag=0;
        byte fbyte=0b00000000;
        byte x;
        int spc=0;//space count
        int sum=0;
        for(int i=0;i<length;i++)
        {
           fbyte=0b00000000;
           x=nb[i];
           for(int j=0;j<8;j++)
           {
               flag = mask & x;
               fbyte=(byte) (fbyte<<1);
             
                if( flag != 0)
                {
                    
                    sum++;
                }
                    x=(byte) (x<<1);
            }
           
        
    }
        return sum;   
    }
    
    
    
    public byte[] addCheckSum(int checkSum,byte[] nb)
    {
      
        
        ByteBuffer bf=ByteBuffer.allocate(4);//bits of checksum == 4*8 = 32
        bf.putInt(checkSum);
        byte []sumByte=bf.array();
        int length=nb.length;
        int clength= length+sumByte.length;
        byte []cbyte = new byte[clength];
        for(int i=0;i<length;i++)
        {
            cbyte[i]=nb[i];
        }
        
        for(int i=length;i<clength;i++)
        {
            cbyte[i]=sumByte[i-length];
        }
        return cbyte;
        
    }
    public byte[] stuffing(byte[] ba)
    {
        byte[] nb = ba;
        
        int length =nb.length;
        int mask = 0b10000000;
        int flag=0;
        int ovbit=consecutive1(ba);
        int ovbyte = (ovbit/8) + 1;
        byte []bn=new byte[nb.length+ovbyte];
        int stfl=0;//index of stuffed byte array
        byte fbyte=0b00000000;
        int c1=0;//number of 1s
        int cc1=0;//how many consecutive 5 1s
        int sc=0;//number of left shifts
        for(int i=0;i<length;i++)
        {
           for(int j=0;j<8;j++)
           {
               flag = mask & nb[i];
               fbyte=(byte) (fbyte<<1);
               sc++;
                if( flag != 0)
                {
                    fbyte=(byte) (fbyte | 1);
                    c1++;
                  //  System.out.println("1 caught fbyte is ");
                  //  printBit(fbyte);
                    
                    if(c1 == 5)
                    {
                        cc1++;
                        c1=0;
                        if( sc == 8)
                        {
                            bn[stfl]=fbyte;
                            stfl++;
                            fbyte=0b00000000;
                            sc=0;
                        }
                        fbyte=(byte) (fbyte<<1);
                      //  System.out.println("1 stuffed fbyte");
                   // printBit(fbyte);
                        sc++;
                    }
                }
                else
                {
                    c1=0;
                }
                if(sc == 8)
                {
                    bn[stfl]=fbyte;
                    stfl++;
                   // System.out.println("2 stuffed fbyte");
                   // printBit(fbyte);
                    fbyte=0b00000000;
                    sc=0;
                }
                    nb[i]=(byte) (nb[i]<<1);
                   // System.out.println("nb is ");
                   // printBit(nb[i]);
            }
         
           if( i == length-1)
           {
               bn[stfl]=(byte) (fbyte<<(8-sc));
           }
        }
      //  System.out.println("\nconsecutive 1 total : 0"+cc1);
        return bn;
       }
    
    //de -stuffing
    
    
    
    public byte[] destuffing(byte []sb)
    {
        int tot=consecutive1(sb);
        byte []dsb;
       
        dsb=new byte[sb.length-(tot/8)-1]; 
      //  System.out.println(dsb.length +" "+sb.length+" "+tot);
        int length=sb.length;
        int dlen = dsb.length;
        int mask = 0b10000000;
        byte fbyte=0b00000000;
        int flag=0;
        int sc=0;//shift count
        int c1=0;//count consecutive 1
        int dstf=0;//index of destuffed array
        int getZ=0;
        for(int i=0;i<length;i++)
        {
            
            for(int j=0;j<8;j++)
            {
                flag=mask & sb[i];   
             //   System.out.println("newliy arrived byte fbyte");
             //       printBit(fbyte);
                   // System.out.println("flag printed");
                   // printBit((byte) flag);
                if(flag == 0)
                {
                    if( getZ != 1){
                    c1=0;
                    fbyte=(byte) (fbyte<<1);
                     sc++;
                   // System.out.println("zero caught");
                    }
                    else
                        getZ = 0;
                }
                else 
                {
                    fbyte=(byte) (fbyte<<1);
                     sc++;
                    fbyte = (byte) (fbyte |1);
                   // System.out.println("or done");
                    // printBit(fbyte);
                    c1++;
                    if(c1 == 5)
                    {
                      //  System.out.println("befor left shifting");
                      //  printBit(sb[i]);
                      //  System.out.println("after left shifting value of j "+j);
                      //  printBit(sb[i]);
                      //  System.out.println(j);
                        c1=0;
                        getZ=1;
                    }
                }
                if( sc == 8 && dstf < dlen)
                {
                  //  System.out.println("array index of destuffed array "+dstf);
                    dsb[dstf]=fbyte;
                    
                   // printBit(fbyte);
                    fbyte=0b00000000;
                    sc=0;
                    dstf++;
                }
                
                sb[i]=(byte) (sb[i]<<1);
               // System.out.println("final");
               // printBit(sb[i]);
                
            }
          
           
        }
        return dsb;
    }
    
    
    public int consecutive1(byte[] sb)
    {
        int length=sb.length;
        byte []nb=sb;
        int mask = 0b10000000;
        int flag=0;
        byte fbyte=0b00000000;
        byte x;
        int c1=0; //number of 1's serially
        int tot=0;//total number of consecutive 5 1's
        for(int i=0;i<length;i++)
        {
           fbyte=0b00000000;
           x=nb[i];
           for(int j=0;j<8;j++)
           {
               flag = mask & x;
               fbyte=(byte) (fbyte<<1);
             
                if( flag == 0)
                {
                    c1=0;
                }
                else
                {
                    c1++;
                    if(c1 == 5)
                    {
                        tot++;
                        c1=0;
                    }
                }
                    x=(byte) (x<<1);
            }
           
        
        
    }
        return tot;
    }
    
    public void printByteArray(byte[] nb)
    {
        //System.out.println("start print byte");
        int length =nb.length;
        int mask = 0b10000000;
        int flag=0;
        byte fbyte=0b00000000;
        byte x;
        int spc=0;//space count
        for(int i=0;i<length;i++)
        {
        
           fbyte=0b00000000;
           x=nb[i];
           for(int j=0;j<8;j++)
           {
               flag = mask & x;
               fbyte=(byte) (fbyte<<1);
             
                if( flag != 0)
                {
                    System.out.print("1");
                    spc++;
                }
                else
                {
                     System.out.print("0");
                     spc++;
                }
                    if(spc == 4)
                    {
                        System.out.print(" ");
                        spc=0;
                    }
                    x=(byte) (x<<1);
            }
    }
        System.out.println();
    }
        
        public void printByte(byte nb)
    {
        
        byte fbyte=0b00000000;
        int flag =0;
        int mask = 0b10000000;
        for(int j=0;j<8;j++)
        {
            flag = mask & nb;
            if( flag != 0)
            {
                System.out.print("1");
            }
            else
            {
                System.out.print("0");
            }
                nb=(byte) (nb<<1);
        }
           System.out.println();
        
    }
}
