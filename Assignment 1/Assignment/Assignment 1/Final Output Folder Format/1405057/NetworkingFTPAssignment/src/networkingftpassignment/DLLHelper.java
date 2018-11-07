package networkingftpassignment;

/**
 *
 * @author HP
 */

/// | flag | type | seqNo | size | payload | checksum | flag |
// type = 1 ---- data
///type = 2 ---- ack
public class DLLHelper {
    
    public static String createFrame(byte[] payLoad, int seq, int size, int type){
        String frame = "";
        String flag = "01111110";
        
        //convert payload to string
        String s = byteToBinString(payLoad);
        String checkSum =  getChecksum(s);
        
        //convert seq and size to binary String
        String loadSize = intToBinString(size);
        String seqNo = intToBinString(seq);
        String frameType = intToBinString(type);
        
        System.out.println("\nSeq :" + seqNo + "\nFrame Type :" + frameType + "\nPayload: " + s + "\nCheckSum: " + checkSum +"\nLoad Size: " + loadSize );
        
        //add all of them
        frame += frameType + seqNo + loadSize + s + checkSum;
        System.out.println("Raw Frame: \t"+frame);
        
        //bitstuffing
        frame = bitStuffing(frame);
        System.out.println("Bit Stuffed Frame: "+frame);
        
        //finale frame
        frame = flag + frame + flag;
        System.out.println("Complete Frame: "+frame);
       
        return frame;
    }
    
    public static void printFrameDetails(String frame){
        System.out.println("Frame: "+frame);
        //testing all the functions
        String sss = removeOverhead(frame);
        int sz = getSize(sss);
        int typ = getType(sss);
        int seqq = getSeq(sss);
        String cs = extCheckSum(sss);
        byte[] payload = getPayload(sss);
        
        System.out.println("Size: "+sz+"\nSeq: "+seqq+"\nType: "+typ+"\nCheckSum: "+cs+"\nPayload: "+new String(payload));
        
    }
    
    public static String removeOverhead(String frame){
        String ans = frame.substring(8, frame.length()-8);
        ans = bitDeStuffing(ans);
        //System.out.println(ans);
        return ans;
    }
    
    public static int getSize(String frame){
        int n = 0;
        String sz = frame.substring(16, 24);
        n = binStringToInt(sz);
        
        return n;
    }

    public static int getType(String frame){
        int n = 0;
        String sz = frame.substring(0, 8);
        n = binStringToInt(sz);
        
        return n;
    }

    public static int getSeq(String frame){
        int n = 0;
        String sz = frame.substring(8, 16);
        n = binStringToInt(sz);
        
        return n;
    }
    
    public static String extCheckSum(String frame){
        String ans = frame.substring(frame.length()-8);
        
        return ans;
    }

    public static byte[] getPayload(String frame){
        String ans = frame.substring(24, frame.length()-8);
        byte[] payLoad = binStringToByte(ans);
        
        return payLoad;
    }

    public static String getChecksum(String payload){
        String checkSum = "";
        char[] generator = "10011".toCharArray();
        char[] quotient = new char[5];
        String given = payload + "0000";
        int len = given.length();
        
        quotient = given.substring(0, 5).toCharArray();
        for(int i=0; i<len-4; i++){
            
            if(quotient[0] == '1'){
                for(int j=1; j<5; j++){
                    if(quotient[j] == generator[j]){
                        quotient[j-1] = '0';
                    }else{
                        quotient[j-1] = '1';
                    }
                }
            }else{
                for(int k=0; k<4; k++){
                    quotient[k] = quotient[k+1];
                }
                
            }
            
            if(i+5==len){
                for(int p=4; p>0; p--){
                    quotient[p] = quotient[p-1];
                }
                quotient[0] = '0';
                //System.out.println(String.valueOf(quotient));
                break;
            }
            
            quotient[4] = given.charAt(i+5);
            
            //System.out.println(String.valueOf(quotient));
        }
        
        checkSum = "000"+String.valueOf(quotient);
        //System.out.println(checkSum);
        return checkSum;
    }
    
    public static String bitStuffing(String payload){
        String stuffedString = "";
        
        int counter = 0;
        for(int i=0; i<payload.length(); i++){
            
            char c = payload.charAt(i);
            stuffedString += c;
            if(c=='1'){
                counter++;
            }else{
                counter = 0;
            }
            
            if(counter==5){
                stuffedString += '0';
                counter = 0;
            }
        }
        
        //System.out.println(stuffedString);
        
        return stuffedString;
    }
    
    public static String bitDeStuffing(String payload){
        String deStuffedString = "";
        
        int counter = 0;
        for(int i=0; i<payload.length(); i++){
            
            char c = payload.charAt(i);
            deStuffedString += c;
            if(c=='1'){
                counter++;
            }else{
                counter = 0;
            }
            
            if(counter==5){
                i++;
                counter = 0;
            }
        }
        
        //System.out.println(deStuffedString);
        
        return deStuffedString;
    }
    
    public static String errorGenerator(String string){
        String eString = "";
        eString = string.replaceFirst("101", "010");
        return eString;
    }
    
    public static boolean errorDetector(byte[] buff, String checkSum){
        boolean result = false;
        String payload = byteToBinString(buff);
        String checkS = getChecksum(payload);
        
        if(checkS.equals(checkSum)){
            System.out.println("Checksum Match Verified\n");
            return true;
        }
        
        System.out.println("Checksum Miss Matched. Requesting resend.\n");
        return result;
    }
    
    public static String byteToBinString(byte[] buff){
        String convertedString = "";
        
        int mask = 0b10000000;
        for(byte b: buff){
            for(int i=0; i<8; i++){
                if((b & mask)!=0){
                    convertedString += "1";
                }else{
                    convertedString += "0";
                }
                mask >>= 1;
            }
            mask = 0b10000000;
        }
        
        //System.out.println(convertedString);
        
        return convertedString;
    }
    
    public static byte[] binStringToByte(String str){
        int len = str.length()/8;
        byte[] ans = new byte[len];
        
        byte temp = 0;
        for(int i=0; i<str.length(); i+=8){
            int mask = 0b10000000;
            for(int j=0; j<8; j++){
                if(str.charAt(i+j)=='1'){
                    temp = (byte)(temp | mask);
                }
                mask >>= 1;
            }
            ans[i/8] = temp;
            temp = 0;
        }
        
        //System.out.println(new String(ans));
        
        return ans;
    }
    
    public static String intToBinString(int n){
        String s = "";
        int mask = 0b10000000;
        
        for(int i=0; i<8; i++){
            if((n & mask)!=0){
                s += "1";
            }else{
                s += "0";
            }
            mask >>= 1;
        }
        return s;
    }
    
    public static int binStringToInt(String s){
        int n = Integer.parseInt(s, 2);
        
        return n;
    }
    
}
