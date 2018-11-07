package filetransmission;


public class DLL {
    

    
    public static String checkSum(byte[] contents){
        
        int sum = 0, fin = 0;
        
        for(int i=0;i<contents.length;i++){
            sum+=(int) contents[i];
        }
        
        while(sum != 0){
            Integer ab = new Integer(sum);
            byte bt = ab.byteValue();
            fin += (int) bt;
            sum = sum >> 8;
        }
        
        fin = 256 - 1 - fin;
        
        Integer abc = new Integer(fin);
        byte bte = abc.byteValue();
        
        String ans = String.format("%8s", Integer.toBinaryString(bte & 0xFF)).replace(' ', '0');
        
        return ans;
    }
    
    public static String bitstuff(int type, int seq_no, int ack_no, byte[] contents){
        String fin = "", result = "";
        
        if(type == 1) fin += "11111111"; //for data type
        if(type == 0) fin += "00000000"; //for ack type
        
        Integer n1 = new Integer(seq_no);       //add seq_no
        String n2 = String.format("%8s", Integer.toBinaryString(n1.byteValue() & 0xFF)).replace(' ', '0');
        fin += n2;
        
        n1 = new Integer(ack_no);              //add ack_no
        n2 = String.format("%8s", Integer.toBinaryString(n1.byteValue() & 0xFF)).replace(' ', '0');
        fin += n2;
        
        String n3 = "";                        //add payload
        for(int i=0;i<contents.length;i++) 
            n3 += String.format("%8s", Integer.toBinaryString(contents[i] & 0xFF)).replace(' ', '0');
        fin += n3;
        
        fin += checkSum(contents);             //add checkSum
        
        int count = 0;
        for(int i=0;i<fin.length();i++){
            if(fin.charAt(i) == '1'){
                count++;
            }
            else{
                count = 0;
            }
            result += fin.charAt(i);
            if(count == 5){
                result += "0";
                count = 0;
            }
        }
        
        result = "01111110" + result + "01111110";
        
        System.out.println("type: " + type);
        System.out.println("seq_no: " + seq_no);
        System.out.println("ack_no: " + ack_no);
        System.out.println("payload : " + n3);
        System.out.println("checksum : " + checkSum(contents));
        System.out.println("before bitstuffing : " + fin);
        System.out.println("after  bitstuffing : " + result);
        
        return result;
    }
    
    public static frame debitstuff(String s){
        frame fm = new frame();
        
        System.out.println("before bit destuffing : " + s);
        
        if(!(s.substring(0, 8).equals("01111110") && s.substring(s.length()-8, s.length()).equals("01111110"))){
            System.out.println("Frame error");
            return null;
        }
        
        String modified_part = s.substring(8, s.length()-8);
        String main_part = "";
        
        int count = 0;
        for(int i=0;i<modified_part.length();i++){
            if(modified_part.charAt(i) == '1'){
                count++;
            }
            else {
                count = 0;
            }
            main_part += modified_part.charAt(i);
            if(count == 5){
                count = 0;
                i++;
            } 
        }
        
        System.out.println("after bit destuffing : " + main_part);
        
        if(main_part.length()%8 != 0){
            System.out.println("Some data bits have been lost.");
            return null;
        }
        
        String n1 = main_part.substring(0,8);
        if(n1.equals("11111111")) fm.type = 1;
        else fm.type = 0;
        
        String n2 = main_part.substring(8,16);
        fm.seq_no = (int) Byte.parseByte(n2,2);
        
        String n3 = main_part.substring(16,24);
        fm.ack_no = (int) Byte.parseByte(n3,2);
        
        byte[] contents = new byte[(int)(main_part.length()/8 - 4)];
        int k = 0;
        for(int i=24;i<main_part.length()-8;i+=8){
            String n4 = main_part.substring(i,i+8);
            contents[k++] = Byte.parseByte(n4,2);
        }
        fm.contents = contents;
        fm.bytesread = k;
        
        String n5 = main_part.substring(main_part.length()-8,main_part.length());
        fm.checksum = n5;
        
        
        System.out.println("type : " + fm.type);
        System.out.println("seq_no : " + fm.seq_no);
        System.out.println("ack_no : " + fm.ack_no);
        System.out.println("payload : " + main_part.substring(24, main_part.length()-8));
        System.out.println("checksum : " + fm.checksum);
        
        return fm;
    }
    
    
    public static Boolean hasChecksumError(byte[] contents, String checksum){
        if(checkSum(contents).equals(checksum)){
            return false;
        }
        System.out.println("Checksum error");
        return true;
    }
}
