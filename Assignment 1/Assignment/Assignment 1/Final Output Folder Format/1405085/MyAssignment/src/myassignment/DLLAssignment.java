/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package myassignment;


import java.util.Arrays;


public class DLLAssignment {

    public static StringBuffer bitStuffedString(StringBuffer buffer) {
        int length = 0;
        StringBuffer stuffedMsg = new StringBuffer();
        int firstIndex = 0, lastIndex = buffer.substring(firstIndex).indexOf("11111");
        while (lastIndex != -1) {

            stuffedMsg.append(buffer.substring(firstIndex, lastIndex));
            stuffedMsg.append("111110");
            firstIndex = lastIndex + 5;
            if (buffer.substring(firstIndex).indexOf("11111") == -1) {
                break;
            }
            lastIndex = firstIndex + buffer.substring(firstIndex).indexOf("11111");

        }
        stuffedMsg.append(buffer.substring(firstIndex));

        return stuffedMsg;

    }

    public static StringBuffer rtnWithCheckSum(StringBuffer buffer) {
        int counter = 0;

        for (int i = 0; i < buffer.length(); i++) {
            if (buffer.charAt(i) == '1') {
                counter += 1;
            }

        }
        String binaryString = Integer.toBinaryString(counter);
        char[] charArray = new char[8 - binaryString.length()];
        Arrays.fill(charArray, '0');
        String str = new String(charArray);
        buffer.append(str + binaryString);
        //System.out.println(str + binaryString);
        //System.out.println(counter);

        return buffer;

    }

    public static StringBuffer intToBinString(int x) {
        String binaryString = Integer.toBinaryString(x);
        char[] charArray = new char[8 - binaryString.length()];
        Arrays.fill(charArray, '0');
        String cntStr = new String(charArray);
        cntStr += binaryString;
        return new StringBuffer(cntStr);

    }

    public static boolean hasCheckSumError(StringBuffer buffer) {
        int counter = 0;

        String str = buffer.substring(buffer.length() - 8);

        for (int i = 0; i < buffer.length() - 8; i++) {
            if (buffer.charAt(i) == '1') {
                counter++;
            }

        }
        counter = 0xff & counter;
        String binaryString = Integer.toBinaryString(counter);
        char[] charArray = new char[8 - binaryString.length()];
        Arrays.fill(charArray, '0');
        String cntStr = new String(charArray);
        cntStr += binaryString;
       
        if (cntStr.equals(str)) {
            System.out.println("No error");
            return false;

        }
        System.out.println("Error");
        return true;

    }
    public static StringBuffer destuffingBits(StringBuffer buffer) {
        StringBuffer destuffedMsg = new StringBuffer();
        int firstIndex = 0, lastIndex = buffer.substring(firstIndex).indexOf("111110");
        while (lastIndex != -1) {

            destuffedMsg.append(buffer.substring(firstIndex, lastIndex));
            destuffedMsg.append("11111");
            firstIndex = lastIndex + 6;
            if (buffer.substring(firstIndex).indexOf("111110") == -1) {
                break;
            }
            lastIndex = firstIndex + buffer.substring(firstIndex).indexOf("111110");

        }
        destuffedMsg.append(buffer.substring(firstIndex));

        return destuffedMsg;

    }

    public static byte[] stringToByte(String str) {

        byte[] arrayToBeSent = new byte[(int) (str.length() / 8)];
        byte cvtByte = 0;

        for (int i = 0; i < (int) (str.length() / 8); i++) {
            for (int j = 0; j < 8; j++) {
                if (str.charAt(i * 8 + j) == '1') {
                    cvtByte += (byte) Math.pow(2, 8 - j - 1);
                    //System.out.println("cvt :" + cvtByte);

                }

            }
            cvtByte = (byte) cvtByte;
            arrayToBeSent[i] = cvtByte;
            cvtByte = 0;

        }

        return arrayToBeSent;
    }
    
    public static StringBuffer toggleBits(StringBuffer buffer){
      StringBuffer buf = new StringBuffer();
      for(int j = 0;j < buffer.length();j++){
          if(buffer.charAt(j) == '0'){
              buf.append("1");
              
          } 
          else{
              buf.append("0");
          
          }
          
      }
      return buf;
    
    }

    /*public static void main(String[] args) {
        char[] charArray;

        StringBuffer buf = new StringBuffer("0100110000011111");
        buf = bitStuffedString(buf);
        System.out.println("buf length :" + buf.length());

        if (buf.length() % 8 != 0) {
            charArray = new char[8 - (buf.length() % 8)];
            Arrays.fill(charArray, '0');
            String strToAlign = new String(charArray);
            buf.append(strToAlign);

        }
        System.out.println(buf);
        System.out.println(buf.length());
        byte[] array = stringToByte(buf.toString());
        StringBuffer strBuilder = new StringBuffer();

        for (int i = 0; i < array.length; i++) {
            int number = (int) (array[i] & 0xff);
            String binaryString = Integer.toBinaryString(number);
            charArray = new char[8 - binaryString.length()];
            Arrays.fill(charArray, '0');
            String str = new String(charArray);
            strBuilder.append(str + binaryString);

        }

        System.out.println("After Convert to string : " + strBuilder);

        strBuilder = destuffingBits(strBuilder);

        System.out.println(strBuilder.length());

        if (strBuilder.length() % 8 != 0) {
            String result = strBuilder.substring(0, strBuilder.length() - (strBuilder.length() % 8));
            System.out.println(result.length());
            System.out.println(result);

        } else {
            System.out.println(strBuilder);

        }

        
    }*/
    public static void main(String[] args){
         StringBuffer buffer = new StringBuffer("0111111110");
         StringBuffer buf = toggleBits(buffer);
         System.out.println(buf.toString());
    
    }
    
}
