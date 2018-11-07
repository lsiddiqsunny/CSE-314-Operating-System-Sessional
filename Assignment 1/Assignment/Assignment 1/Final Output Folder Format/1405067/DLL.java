
package file.transmission;

public class DLL {
    private final String flag="01111110";
    private String s;
    private byte[] framebody=null;
    public final byte data=0x0f;
    public final byte acknowledge=0x00;
    
    public boolean checkError(String s)
    {
        this.s=s;
        if(!checkFrameFlag())
        {
            System.out.println("flag missMatched");
            return false;
        }
        deStuff(s);
        framebody = convertToByteArray(s.substring(0,s.length()-8));
        int temp = Integer.parseInt(s.substring(s.length()-8),2);
        byte checksum = (byte)temp;
        if(!verifyCheckSum(framebody,checksum))
        {
            System.out.println("checkSum missMatched");
            return false;
        }
        return true;
    }
    public String makeFrame(byte type,byte seq,byte[] payload)
    {
        byte checksum=0;
        if(payload != null)checksum = calculateCheckSum(payload);
        checksum ^= type;
        checksum ^= seq;
        
        s = byteToBitString(type)+byteToBitString(seq);
        if(payload != null)s += convertToBitString(payload);
        s += byteToBitString(checksum);
        
        stuff(s);
        addFrameFlag();
        
        return s;
    }
    public String stuff(String s)
    {
        String stuffed = new String("");
        int count=0;
        for(int i=0;i<s.length();i++)
        {
            stuffed += s.charAt(i);
            if(s.charAt(i)=='1')count++;
            else count=0;
            
            if(count==5)
            {
                stuffed += '0';
                count=0;
            }
                
        }
        s=stuffed;
        //System.out.println("stuffed: "+s);
        return s;
    }
    public String deStuff(String s)
    {
        String deStuffed = new String("");
        int count=0;
        for(int i=0;i<s.length();i++)
        {
            if(s.charAt(i) == '1')
            {
                count++;
                deStuffed += s.charAt(i);
            }
            else 
            {
                if( count != 5 )
                {
                    deStuffed += s.charAt(i);
                }
                count = 0;
            }

        }
        s=deStuffed;
        //System.out.println("deStuffed: "+s);
        return s;
    }
    public byte[] getPayload()
    {
        return convertToByteArray(s.substring(16, s.length()-8));
    }
    
    public byte calculateCheckSum(byte[] data)
    {
        byte checksum = 0;
        for(int i=0;i<data.length;i++)
        {
            checksum ^= data[i];
        }
        
        return checksum;
    }
    public boolean verifyCheckSum(byte[] payload,byte code)
    {
        byte checksum = calculateCheckSum(payload);
        if(code == checksum)return true;
        return false;
    }
    public void addFrameFlag()
    {
        s=flag+s+flag;
    }
    public boolean checkFrameFlag ()
    {
        String h = s.substring(0,8);
        String t = s.substring(s.length()-8);
        if(h.equals(t) && h.equals(flag))
        {
            s=s.substring(8,s.length()-8);
            return true;
        }
        else return false;
    }
    public String convertToBitString(byte[] data)
    {
        String s=new String("");
        for(int i=0;i<data.length;i++)
            s += byteToBitString(data[i]);
        return s;
    }
    public String byteToBitString(byte data)
    {
        String s=new String("");
        for(int j=7;j>=0;j--)
        {
            s += (((data>>>j)& 1) != 0) ? '1' : '0';
        }
        return s;
    }
    public byte[] convertToByteArray(String s)
    {
        byte[] data = new byte[s.length()/8];
        for(int i=0 ; i<s.length()/8 ; i++)
        {
            int a = Integer.parseInt(s.substring(i*8, (i+1)*8),2);
            data[i] = (byte)a;
        }
        return data;
    }
    public boolean isData()
    {
        byte t = (byte)Integer.parseInt(s.substring(0,8),2);
        if( t == this.data )return true;
        else return false;
    }
    public int getSeqNo()
    {
        return Integer.parseInt(s.substring(8,16),2);
    }
    
   

}
