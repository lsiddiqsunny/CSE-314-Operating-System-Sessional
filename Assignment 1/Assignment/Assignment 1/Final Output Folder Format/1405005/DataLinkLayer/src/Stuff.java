/**
 * Created by Nayeem Hasan on 28-Oct-17.
 */
public class Stuff {

    public String stuffBits(String bitString){

        String stuffedString = new String();

        int cnt = 0;
        for (int i=0; i < bitString.length(); i++) {

            if (bitString.charAt(i) == '1') {
                stuffedString += bitString.charAt(i);
                cnt++;
            } else if (bitString.charAt(i) == '0') {
                stuffedString += bitString.charAt(i);
                cnt = 0;
            }

            if (cnt == 5) {
                stuffedString += '0';
                cnt = 0;
            }
        }

        stuffedString = "01111110"+stuffedString+"01111110";
        return stuffedString;
    }

    public String destuffBits(String inpString){

        if (inpString.substring(0,8).equals("01111110") &&
                inpString.substring(inpString.length()-8,inpString.length()).equals("01111110")) {

            String res = new String();

            int cnt = 0;

            String bitString = inpString.substring(8,inpString.length()-8);

            for (int i=0; i < bitString.length(); i++){
                if (bitString.charAt(i) == '1'){
                    res += bitString.charAt(i);
                    cnt++;
                }
                if (bitString.charAt(i) == '0'){
                    if (cnt != 5) res += bitString.charAt(i);
                    cnt = 0;
                }
            }
            return res;
        }
        else return null;
    }


    public String calculateCheckSum(String inpString){
        int count = 0;
        for (int i=0; i<inpString.length(); i++){
            if (inpString.charAt(i) == '1') count++;
        }

        return String.format("%8s",Integer.toBinaryString(count % 32) ).replace(' ', '0');
    }

    public boolean hasNoChecksumError(String inpString, String checkString){
        int count = 0;
        for (int i=0; i<inpString.length(); i++){
            if (inpString.charAt(i) == '1') count++;
        }
        int val = Integer.parseInt(checkString, 2);

        if ((count - val) % 32 == 0) return true;
        else return false;
    }


    public String makeAckFrame(int type, int seqNo){
        String typeStr, seqStr;
        typeStr = String.format("%8s",Integer.toBinaryString(type) ).replace(' ', '0');
        seqStr = String.format("%8s",Integer.toBinaryString(seqNo) ).replace(' ', '0');

        return typeStr+seqStr;
    }

    public String makeFrame(int type,int seqNo, byte[] arr){
        String frame = new String();
        String payload = new String();

        String typeStr, seqStr;
        typeStr = String.format("%8s",Integer.toBinaryString(type) ).replace(' ', '0');
        seqStr = String.format("%8s",Integer.toBinaryString(seqNo) ).replace(' ', '0');

        frame += typeStr;
        frame += seqStr;

        for (byte b : arr){
            payload += Integer.toBinaryString(b & 255 | 256).substring(1);
        }
        frame += payload;

        String checkStr = calculateCheckSum(payload);
        frame += checkStr;


        System.out.println("Frame no :"+seqNo+" : "+typeStr+"|"+seqStr+"|"+payload+"|"+checkStr);

        String stuffedFrame = stuffBits(frame);
        System.out.println("Stuffed frame no :"+seqNo+" : " + stuffedFrame);
        System.out.println();
        return stuffedFrame;
    }


}
