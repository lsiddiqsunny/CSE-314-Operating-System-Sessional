package Frame;

/**
 * Created by ashiq on 10/16/17.
 */
public class Frame {
    public Bytes data;
    public byte seqno;
    public byte aqno;
    public  byte checksum;
    public byte [] array=null;
    public Frame() {
        data=new Bytes();
    }
    public String getString(byte b){
        /* from net */
        String string = String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0');
        return  string;
    }
    public void creatFrame(byte sqeno, byte aqno, byte[] array)
    {
        this.data.bytes=null;
        this.seqno=sqeno;
        this.aqno=aqno;
        this.array=array;
        StringBuilder string= new StringBuilder("");
        string.append(getString(sqeno));
        string.append(getString(aqno));
        StringBuilder pstring= new StringBuilder("");
        for(byte b: array)
        {
            pstring.append(getString(b));
        }
        this.checksum=getChecksum(pstring.toString()); /* check sum is calculated before bit staffing */
        string.append(pstring);
        string.append(getString(this.checksum));
        StringBuilder frame= new StringBuilder( string.toString().replaceAll("11111","111110"));
        frame.append("01111110");
        frame.insert(0,"01111110");
        /* creating bool array for transmission */
        data.bytes=new boolean[frame.length()];

        for( int i=0; i< frame.length();i++)
        {
            data.bytes[i]= frame.charAt(i)=='1' ? true : false;
        }

        return;
    }
    public boolean decodemsg(boolean [] bytes)
    {
        StringBuilder s=new StringBuilder("");

        for( boolean b : bytes)
        {
            s.append(b?"1":"0");
        }
        String string= s.toString();
        string=string.substring(8,string.length()-8);
        string=string.replaceAll("111110","11111");
        this.seqno=(byte) Integer.parseInt(string.substring(0,8),2);
        this.aqno= (byte) Integer.parseInt(string.substring(8,16),2);
        this.checksum=(byte) Integer.parseInt(string.substring(string.length()-8,string.length()),2);
        String payload= string.substring(16,string.length()-8);
       // System.out.println(payload);
       // payload=payload.replaceAll("111110","11111");

        //System.out.println(payload);

        if(payload.length()%8!=0)
        {
            System.out.println("nasty error "+payload.length());
            array=null;
            return false;
        }
        else {
            array=new byte[payload.length()/8];
            for (int i = 0; i <= payload.length() - 8; i += 8) {
                array[i/8]=(byte) Integer.parseInt(payload.substring(i,i+8),2);

            }
        }
        byte newcheksum=getChecksum(payload);
        if(this.checksum!=newcheksum){
            //System.out.println("Error in check sum "+ this.checksum+" "+ newcheksum);
            array=null;
            return false;
        }else{
            //System.out.println("ok in check sum "+ this.checksum+" "+ newcheksum);
        }
        for( byte bi : this.array)
        {
            //System.out.print((char)bi);
        }
       // System.out.println("----->");
        return true;
    }
    public byte getChecksum(String string)
    {
        byte count=0;
       for( char c: string.toCharArray()){
           if(c=='1') {
               count++;
           }
       }
       return (byte) (count%127);
    }
    public static void main(String[] args) {
        Frame frame= new Frame();
        byte b[]={ 126,1,126,33,22,121};
        System.out.println(frame.getString((byte)100));
        frame.creatFrame((byte)127,(byte)127,b);
        System.out.println(frame.data.bytes.toString());
        frame.data.bytes[30]=!frame.data.bytes[30];
        System.out.println(frame.decodemsg(frame.data.bytes));
        frame.data.bytes[30]=!frame.data.bytes[30];
        System.out.println(frame.decodemsg(frame.data.bytes));
        System.out.print(frame.seqno+" -- "+frame.aqno);
        for( byte bi : frame.array)
        {
            System.out.println(bi);
        }
       /* String s="555000";
        System.out.println(s.replaceAll("50",""));*/
    };
}
