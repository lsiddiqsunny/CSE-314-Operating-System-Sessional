/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DataPack;

/**
 *
 * @author USER
 */
public class FrameCreator 
{
    public boolean isError;
    public String payLoad;
    public String frameDeStuff;
    public String frameBitStuff;
    public String[] stringByte;

    public FrameCreator() 
    {
        isError=false;
        stringByte=new String[1000];
        payLoad="";
        frameDeStuff="";
        frameBitStuff="";
    }
    
    public String getBits(byte bin)
    {
        StringBuilder sBits=new StringBuilder();
        for(int i=7;i>=0;i--)
        {
            if(((bin>>>i) & 1)==1)
            {
                sBits.append('1');
            }
            else
            {
                sBits.append('0');
            }
        }
        //System.out.println("getBits(): "+sBits.toString());
        return sBits.toString();
    }
    public String frameType(int f)
    {
        
        return new String(getBits((byte) f));
    }
    
    public String ackType(int a)
    {
        return new String(getBits((byte) a));
    }
    public String checkSumCalc(String[] sArr,int index)
    {
        String checkSum="";
        for(int i=0;i<8;i++)
        {
            int cnt=0;
            for(int j=0;j<index;j++)
            {
                if(sArr[j].charAt(i)=='1')
                {
                    cnt++;
                }
            }
            if(cnt%2!=0)
            {
                checkSum+="1";
            }
            else
            {
                checkSum+="0";
            }
        }
        System.out.println("CheckSumCalc(): "+checkSum);
        return checkSum;
    }
    public String setPayLoad(byte[] bin)
    {
        String str="";
        for(byte b: bin)
        {
            str+=getBits(b);
        }
        return str;
    }
    public String bitStuffing(String[] sByte,int index)
    {
        String payLoad="";
        int cnt=0;
        for(int j=0;j<index;j++)
        {
            for(int i=0;i<8;i++)
            {
                if(sByte[j].charAt(i)=='0')
                {
                    payLoad+="0";
                    cnt=0;
                }
                else
                {
                    payLoad+="1";
                    cnt++;
                    if(cnt==5)
                    {
                        payLoad+="0";
                        cnt=0;
                    }
                }
            }
        }
        return payLoad;
    }
    public String bitStuffing(String f)
    {
        String bs="";
        int cnt=0;
        for(int i=0;i<f.length();i++)
        {
                if(f.charAt(i)=='0')
                {
                    bs+="0";
                    cnt=0;
                }
                else
                {
                    bs+="1";
                    cnt++;
                    if(cnt==5)
                    {
                        bs+="0";
                        cnt=0;
                    }
                }
            }
        
        return bs;
    }
    public String printByte(String[] sByte,int index)
    {
        String str="";
        for(int i=0;i<index;i++)
        {
            str+=sByte[i]+" ";
        }
        str+="\n";
        return str;
    }
    public String printString(String ss)
    {
        String str="";
        for(int i=0;i<ss.length();i++)
        {
            str+=ss.charAt(i);
            if((i+1)%8==0)
            {
                str+=" ";
            }
        }
        str+="\n";
        return str;
    }
    public byte[] sendPacket(byte[] bin,int seq,int f,int a,boolean ie) throws InterruptedException
    {
        isError=ie;
        String packet="";
        //String sByte[]=new String[1000];
        String mainData="";
        String checkSum="";
        String sequence="";
        //String payLoad="";
        int index=0;
        String typeFrame="";
        typeFrame=frameType(f);
        String ack="";
        ack=ackType(a);
        
        //textPrev.setText("");
       // textAfter.setText("");
        
        for(byte b:bin)
        {
            //System.out.println("Index: "+(index+1));
            stringByte[index]=getBits(b);
            //textPrev.appendText("Index: "+(index+1)+"\n");
            //textPrev.appendText(sByte[index]);
            index++;
        }
        //textPrev.appendText("\n");
        payLoad=setPayLoad(bin);
        
        sequence=getBits((byte) seq);
        //System.out.println("Entering checkSumCalc(sByte)");
        checkSum=checkSumCalc(stringByte,index);
        frameDeStuff=typeFrame+sequence+ack+payLoad+checkSum;
        frameBitStuff=bitStuffing(frameDeStuff);
        
        if(isError)
        {
            char ch=frameBitStuff.charAt(25);
            if(ch=='0')
            {
                frameBitStuff=frameBitStuff.substring(0,25)+"1"+frameBitStuff.substring(26);
            }
            else if(ch=='1')
            {
                frameBitStuff=frameBitStuff.substring(0,25)+"0"+frameBitStuff.substring(26);
            }
            isError=false;
        }
        //System.out.println("SEQ: "+sequence);
        packet="01111110"+frameBitStuff+"01111110";
        
        //textAfter.appendText(payLoad+"\n");
        System.out.println("Frame to send:");
        System.out.println(printString(frameDeStuff));
        System.out.println("Frame after Stuffing:");
        System.out.println(printString(frameBitStuff));
        
        System.out.println("Packet in String: "+printString(packet));
        
        return packet.getBytes();
    }
    
    //SERVER //***************************************************************
    public String removeHeadTail(String sBytes)
    {
        int cnt=0;
        String removeHead="";
        for(int i=0;i<sBytes.length();i++)
        {
            if(sBytes.charAt(i)=='1')
            {
                cnt++;
            }
            else
            {
                cnt=0;
            }
            if(cnt==6)
            {
                removeHead=sBytes.substring(i+2);
                break;
            }
        }
        
        cnt=0;
        String removeHeadTail="";
        for(int i=0;i<removeHead.length();i++)
        {
            if(removeHead.charAt(i)=='1')
            {
                cnt++;
            }
            else
            {
                cnt=0;
            }
            if(cnt==6)
            {
                removeHeadTail=removeHead.substring(0,i-6);
                break;
            }
        }
        System.out.println("removeHeadTail(): "+printString(removeHeadTail));
        return removeHeadTail;
    }
    
    public String deStuffing(String sBytes)
    {
        String str=new String(sBytes);
        String deStuff="";
        int cnt=0;
        for(int i=0;i<str.length();i++)
        {
            if(str.charAt(i)=='0')
            {
                deStuff+="0";
                cnt=0;
            }
            else
            {
                deStuff+="1";
                cnt++;
            }
            if(cnt==5)
            {
                i++;
                cnt=0;
            }
        }
        System.out.println("deStuffing(): "+printString(deStuff));
        return deStuff;
    }
    
    public int getSequence(String sBytes)
    {
        String str=new String(sBytes);
        str=str.substring(8,16);
        int seq=Integer.parseInt(str, 2);
        System.out.println("SEQ: "+seq);
        return seq;
    }
    public int getAck(String sBytes)
    {
        String str=new String(sBytes);
        str=str.substring(16,24);
        int ack=Integer.parseInt(str, 2);
        System.out.println("Ack: "+ack);
        return ack;
    }
    public String getPayLoad(String sBytes)
    {
        String str=new String(sBytes);
        return str.substring(24,sBytes.length()-8);
    }
    public String getCheckSum(String sBytes)
    {
        String checkSum=sBytes.substring(sBytes.length()-8);
        System.out.println("getCheckSum(): "+checkSum);
        return checkSum;
    }
    
    public boolean checkSumVerification(String str,String checkSum)
    {
        String sByte=new String(str);
        String [] veriArr=new String[sByte.length()/8];
        for(int i=0;i<veriArr.length;i++)
        {
            veriArr[i]=sByte.substring(0,8);
            if(i!=veriArr.length-1)
            {
                sByte=sByte.substring(8);
            }
        }
        for(int i=0;i<8;i++)
        {
            int cnt=0;
            for(int j=0;j<veriArr.length;j++)
            {
                if(veriArr[j].charAt(i)=='1')
                {
                    cnt++;
                }
            }
            cnt=cnt%2;
            if((cnt!=0 && checkSum.charAt(i)=='0') || (cnt!=1 && checkSum.charAt(i)=='1'))
            {
                return false;
            }
        }
        return true;
    }
    
    public byte[] stringToByte(String sByte)
    {
        int k=0;
        byte[] bArr = new byte[sByte.length()/8];
        for(int i=0;i<sByte.length()/8;i++)
        {
            bArr[i] = 0x00;
            for (int j = 0; j <= 7; j++) 
            {
                if (sByte.charAt(k) == '1')
                {
                    bArr[i] = (byte) (bArr[i] | (1 << (7 - j)));
                }
                k++;
            }
        }
        //System.out.println("WRITING on File: " + getBits(bArr[sByte.length()/8-1]));
        return bArr;
    }
    
}
