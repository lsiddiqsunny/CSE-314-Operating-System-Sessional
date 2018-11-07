package Utility;

import javafx.util.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.zip.Checksum;

public class ProcessBytes {
    static ArrayList<Boolean>Map=new ArrayList<>();
    static Boolean Flag[]={false,true,true,true,true,true,true,false};

    public static int byteArrayToInt(byte[] b){
        return  b[1] & 0xFF | (b[0] & 0xFF) << 8;
    }

    public static byte[] intToByteArray(int a){
        return new byte[] {
                (byte) ((a >> 8) & 0xFF),
                (byte) (a & 0xFF)
        };
    }

    public static byte[] convertToByteArray(ArrayList<Boolean> booleans) {
        byte[] result=new byte[booleans.size()/8];
        for (int i=0;i<result.length;i++) {
            int index = i*8;
            result[i] = (byte)(
                            (booleans.get(index+0) ? 1<<7 : 0) +
                            (booleans.get(index+1) ? 1<<6 : 0) +
                            (booleans.get(index+2) ? 1<<5 : 0) +
                            (booleans.get(index+3) ? 1<<4 : 0) +
                            (booleans.get(index+4) ? 1<<3 : 0) +
                            (booleans.get(index+5) ? 1<<2 : 0) +
                            (booleans.get(index+6) ? 1<<1 : 0) +
                            (booleans.get(index+7) ? 1 : 0));
        }
        return result;
    }

    public static  byte[] BitStuff(int SeqNo,byte[] Input,boolean Error){
        int CheckSum=0;
        for(byte now : Input) for(int i=7;i>=0;i--) CheckSum+=( (now & (1<<i))==0 ? 0 : 1);
        byte[] SeqBytes=intToByteArray(SeqNo);
        byte[] CheckSumBytes=intToByteArray(CheckSum);

        int indx=0;
        byte[] buffer=new byte [SeqBytes.length+Input.length+CheckSumBytes.length];
        for(byte now : SeqBytes)        buffer[indx++]=now;
        for(byte now : Input)           buffer[indx++]=now;
        for(byte now : CheckSumBytes)   buffer[indx++]=now;

        Map.clear();
        Map.addAll(Arrays.asList(Flag));
        int Cons=0;

        for(byte now: buffer){
            for(int i=7;i>=0;i--) {
                boolean cur = ((now & (1 << i)) != 0);
                if(cur) Cons++;
                Map.add(cur);
                if(Cons==5 && !Error){Cons=0; Map.add(false);}
            }
        }
        Map.addAll(Arrays.asList(Flag));
        while (Map.size()%8!=0) Map.add(false);

        System.out.println(SeqNo+" "+CheckSum);

        return convertToByteArray(Map);
    }

    public static Pair<Integer,byte[]> DeStuff(ArrayList<Boolean> Input){
        Map.clear();

        int Cons=0;
        for(boolean cur : Input){
            if(Cons==5){
                Cons=0;
                if(cur) return new Pair(-1,null);
                else continue;
            }
            Map.add(cur);
            if(cur) Cons++;
        }

        int SeqNo=0;
        for(int i=0;i<16;i++) if(Map.get(i)) SeqNo+=1<<(15-i);

        int CheckSum=0;
        ArrayList<Boolean>Final=new ArrayList<>();
        for(int i=16;i<Map.size()-16;i++) if(Map.get(i)) CheckSum++;
        for(int i=16;i<Map.size()-16;i++) Final.add(Map.get(i));

        int Sum=0;
        for(int i=Map.size()-16;i<Map.size();i++) if(Map.get(i)) Sum+=1<<(Map.size()-1-i);

        System.out.println(SeqNo+" "+Sum+" "+ CheckSum);

        if(CheckSum!=Sum) return new Pair(-1,null);
        return new Pair(SeqNo,convertToByteArray(Final));
    }
}

