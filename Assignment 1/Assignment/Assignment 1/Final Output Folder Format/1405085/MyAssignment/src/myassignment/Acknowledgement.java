package myassignment;

import java.io.Serializable;
import java.util.Arrays;

public class Acknowledgement implements Serializable{

    int ackNo;
    int seqNo;
    byte[] ackArray;
    int error;
    public Acknowledgement(int ackNo, int seqNo,int error) {
        this.ackNo = ackNo;
        this.seqNo = seqNo;
        this.ackArray = new byte[48];
        this.error = error;
    }

    public byte[] createAcknowledgement() {
        
        StringBuffer strBuilder = new StringBuffer();
        char[] charArray;
        String binaryString = Integer.toBinaryString(seqNo);
        charArray = new char[8 - binaryString.length()];
        Arrays.fill(charArray, '0');
        String str = new String(charArray);
        strBuilder.append("01111110" + "00000010" + str + binaryString);
        if(error == 0){
          strBuilder.append("00000000");
        }
        else{
           strBuilder.append("00000001");
        }
        
        binaryString = Integer.toBinaryString(ackNo);
        charArray = new char[8 - binaryString.length()];
        Arrays.fill(charArray, '0');
        str = new String(charArray);
        strBuilder.append(str + binaryString + "01111110");
                
        ackArray = DLLAssignment.stringToByte(strBuilder.toString());
        for(int a = 0; a < 6;a++){
            System.out.println("array " + ackArray[a]); 
        }
        
        return ackArray;

    }
    
    
}
