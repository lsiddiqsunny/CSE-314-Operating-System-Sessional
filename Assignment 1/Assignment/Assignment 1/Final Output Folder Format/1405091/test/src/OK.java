import com.sun.deploy.util.StringUtils;
import com.sun.scenario.effect.impl.sw.sse.SSEBlend_SRC_OUTPeer;

import javax.xml.bind.SchemaOutputResolver;
import java.io.*;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.Scanner;

import static sun.util.calendar.CalendarUtils.mod;

/**
 * Created by Asus on 9/29/2017.
 */
public class OK {
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");
    private static   String s="";
    private static String cng=null;

    public static int power(int a, int b)
    {
        int power = 1;
        for(int c=0;c<b;c++)
            power*=a;
        return power;
    }
    //String s = "";


    public static String changeCharInPosition(int position, char ch, String str){
        char[] charArray = str.toCharArray();
        charArray[position] = ch;
        return new String(charArray);

    }

    public static String intToString(int number) {
        StringBuilder result = new StringBuilder();

        for(int i = 7; i >= 0 ; i--) {
            int mask = 1 << i;
            result.append((number & mask) != 0 ? "1" : "0");

            //if (i % groupSize == 0)
                //result.append(" ");
        }
        //result.replace(result.length() - 1, result.length(), "");

        return result.toString();
    }

    public  static int stringToInt(String s)
    {
        int res=0;

        for (int i = s.length()-1; i>=0 ; i--) {
            if(s.charAt(i)=='1') res+=power(2,s.length()-i-1);

        }
        return res;
    }

    public static int chesksum(String st)
    {
        int i = 0;
        int count=0;
        while(i!=st.length())
        {
            if(st.charAt(i)=='1') count+=1;

            i++;
        }


        return count%256;
    }
    public static String byteToString(byte[] cmp)
    {
        String asas="";
        for (int i = 0; i <cmp.length ; i++) {
            //System.out.println((char)cmp[i]);
            //System.out.println((int) cmp[i]);
            //System.out.println(Integer.toBinaryString( (int) component[i]));
            //s+=Integer.toBinaryString( (int) component[i]);
            //String o;
            //System.out.println(o=intToString( (int) cmp[i]));
            asas+=intToString((int) cmp[i]);

        }
        return asas;

    }




    public static void decompressed(byte[] cmpd) throws IOException {

        String frame = byteToString(cmpd);

        System.out.println("Frame len: " + frame.length() + "   " + frame);
        int cut = frame.indexOf("01111110", 8);
        System.out.println(cut);
        String playLoad = frame.substring(8, cut);
        String destuffed = playLoad.replaceAll("111110", "11111");
        System.out.println("Destuffed len: " + destuffed.length() + "   " + destuffed);
        System.out.println(destuffed);
        String ac_load = destuffed.substring(8, destuffed.length() - 8);
        System.out.println(ac_load);
        String checkBit = destuffed.substring(destuffed.length() - 8 + 1, destuffed.length());
        System.out.println(checkBit);
        int got = stringToInt(checkBit);
        System.out.println("CheckSum of sender checkbit==> " + got);
        int chkload = chesksum(ac_load);
        System.out.println("CheckSum of sender load==> " + chkload);


            //this.dos.writeUTF("0");
            byte[] cmp = new byte[ac_load.length() / 8];
            int m = 0;
            for (int i = 0; i < ac_load.length() / 8; i++) {

                int k = stringToInt(ac_load.substring(m, m + 8));
                cmp[i] = (byte) k;
                m += 8;}





//        String frame=byteToString(cmpd);
//        System.out.println("frame-->"+frame+" len ="+frame.length());
//        int cut=frame.indexOf("01111110",8);
//        System.out.println("kate"+cut);
//        String s=frame.substring(8,cut-8);
//        String checkBit=frame.substring(cut-8+1,cut);
//        System.out.println("checkBIt value==> "+stringToInt(checkBit));
//        System.out.println("ext payload==> "+s+"   len="+s.length());
//
//        String destuffed=s.replaceAll("111110","11111");
//        System.out.println("destuffed==> "+destuffed+" len= "+destuffed.length());
//
//        int chk=chesksum(destuffed);
//        System.out.println("destfd chksum= "+chk);
//
//        byte[] cmp=new byte[destuffed.length()/8];
//        int m=0;
//        for (int i = 0; i <destuffed.length()/8 ; i++) {
//
//            int k=stringToInt(destuffed.substring(m,m+8));
//            cmp[i]= (byte) k;
//            m+=8;
//        }
//
//
        String extrct=byteToString(cmp);


        System.out.println("final "+extrct);


        File fnew=new File("F:\\J\\FIle-Transport\\test\\src\\abc2.txt");
        FileOutputStream fos=new FileOutputStream(fnew);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        bos.write(cmp,0,cmp.length);
        bos.flush();
        bos.close();
        fos.close();




    }

    public static void main(String[] args) throws IOException {

        System.out.println(stringToInt("01111101") +"     "+ stringToInt("00001111"));

                //method 1

        /*Scanner scn=new Scanner(System.in);
        Integer i=scn.nextInt();

        Random r = new Random();

        Integer Low = i/6;
        Integer High = i/4;
        for (Integer j = 0; j <8 ; j++) {
            Integer Result = r.nextInt(High-Low) + Low;
            System.out.println(Result);
        }
*/
        //int checkerstudent_no=mod(1*5+mod(i,1000)+60,122)+1;

        //int exercise_no=mod(1*20+mod(i,1000),26)+1;
        //System.out.println(checkerstudent_no+"\n"+exercise_no);

                //format timestamp
                //System.out.println(sdf.format(timestamp));
        //System.out.println(Integer.toBinaryString(65));
        //String p= intToString(65);
        //System.out.println(p+" len"+p.length());

        File fnew=new File("F:\\J\\FIle-Transport\\test\\src\\abc.txt");
        FileInputStream fin=new FileInputStream(fnew);
        BufferedInputStream bis=new BufferedInputStream(fin);
        byte[] component;
        //int chunkSize=(int)fnew.length();
        //System.out.println(fnew.length());
        int chunkSize=3;
        component=new byte[chunkSize];
        bis.read(component,0,chunkSize);


/*
        for (int i = 0; i <chunkSize ; i++) {
            System.out.println((char)component[i]);
            System.out.println((int) component[i]);
            //System.out.println(Integer.toBinaryString( (int) component[i]));
            //s+=Integer.toBinaryString( (int) component[i]);
            String o;
            System.out.println(o=intToString( (int) component[i]));
            s+=intToString((int) component[i]);

        }*/
        s = byteToString(component);
        System.out.println(s);
        System.out.println(s.length());
        //System.out.println("There r 8 bytes , do u want to Change?[reply 'y'/'n' or to continue 'c' or to break 'b'");


        //String rply=scn.nextLine();;
        cng=s;
        int j=-1;
        while (j<0) {

            System.out.println("ase");
            System.out.println("There r"+cng.length()+" bits , do u want to Change?[reply 'y'/'n'");
            Scanner scn=new Scanner(System.in);
            String rply=scn.nextLine();
            if (rply.equals("y")) {
                System.out.println("Put bit no");
                int bit = scn.nextInt();

                if (cng.charAt(bit) == '0') {
                    cng = changeCharInPosition(bit,'1',cng);
                    //cng = su;
                    System.out.println(cng);
                    System.out.println(cng.length());


                }
                else {
                    cng = changeCharInPosition(bit,'0',cng);
                    //cng = su;
                    System.out.println(cng);
                    System.out.println(cng.length());

                }

            }
            else  break;


        }


        //s+="1111010101010101111";
        System.out.println("originl bit==> "+s+"   len="+s.length());
        System.out.println("Changed bit==> "+cng+" len="+cng.length());
        String fr="01111110";
        //String stuff=fr+s.replaceAll("11111","11110");
        //System.out.println(stuff);
        //System.out.println(stuff.length());
        int ch=chesksum(s);
        System.out.println("checksum==> "+ch);
        String chcksum=intToString(ch);
        String load="00000001"+cng+chcksum;

        String frame=fr+load.replaceAll("11111","111110")+fr;
        System.out.println("farme    ==> "+frame+"   len="+frame.length());

        if((frame.length()%8)!=0)
        {
            System.out.println("chole");
            for(int i=(frame.length()%8)+1;i<=8;i++)
            {
                frame+="0";
            }
        }
        System.out.println("final: "+ frame);
        System.out.println(frame.length());

        //byte[] bytes = frame.getBytes();
        //System.out.println(bytes.length);
        System.out.println("===================extracting===============");
        //String cheku="";
        byte[] cmpd=new byte[frame.length()/8];
        int m=0;
        for (int i = 0; i <frame.length()/8 ; i++) {
            int k;
                String s=frame.substring(m,m+8);
            //System.out.println(i+"==>"+s);
                //cheku+=s;
                k=stringToInt(s);

           // System.out.println(k);
            cmpd[i]= (byte) k;
            m+=8;
        }
        //System.out.println("cheku   "+cheku);
        //System.out.println("checcckkk=="+ frame.compareTo(cheku));
       // for (int i = 0; i <cmpd.length ; i++) {
            //System.out.println((char)cmp[i]);
         //   System.out.println((int) cmpd[i]);}

        //System.out.println("function theke");
        //String frameTosend=byteToString(cmpd);

        //System.out.println("pathai  "+frameTosend);
        //System.out.println("checcckkk=="+ frameTosend.compareTo(cheku));
        decompressed(cmpd);

    }



    }


