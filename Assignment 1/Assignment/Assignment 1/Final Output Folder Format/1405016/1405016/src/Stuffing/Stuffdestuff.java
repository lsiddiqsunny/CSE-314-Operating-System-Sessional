package Stuffing;

public class Stuffdestuff {

    public void byteFrame(Frame f, byte frame[]){
        frame[0] = f.getKind_of_frame();
        frame[1] = f.getSec_no();
        frame[2] = f.getAck_no();
        for (int i = 0; i < f.getPayload().length; i++)
            frame[3+i] = f.getPayload()[i];
        frame[f.getPayload().length+3] = f.getChecksum();
    }
    public Frame getFrame(byte frame[]){
        byte kind = frame[0];
        byte sec = frame[1];
        byte ack = frame[2];
        byte[] payload = new byte[frame.length-4];
        for (int i = 3; i < frame.length - 1; i++)
            payload[i-3] = frame[i];
        return new Frame(kind,sec,ack,payload);
    }
    public void stuffBit(byte tobeStuffed[], byte stuffed[]) {
        int bitCount = 0;
        String s = "01111110";
        String s1 = s;
        int prev = -1;
        for (int i = 0; i < tobeStuffed.length; i++) {
            int b1 = tobeStuffed[i];
            for (int j = 0; j < 8; j++) {
                int x = b1 >> (7 - j) & 1;
                if (x == 1) {
                    if (x == prev) bitCount++;
                    s += "1";
                    s1 += "1";
                    prev = 1;
                    if (bitCount == 4){
                        s += "0";
                        bitCount = 0;
                        s1 += " ";
                        prev = -1;
                    }
                }
                else{
                    bitCount = 0;
                    s += "0";
                    s1 += "0";
                    prev = 0;
                }
            }
        }

        s += "01111110";
        s1 += "01111110";
        System.out.println("Frame before stuffing :   "+s1);
        System.out.println("Frame after stuffing :    "+s);
        int k = 0;
        for (int i = 0; i < s.length();){
            byte bit = 0;
            int x = 128;
            for (int j = i; j < Math.min(i + 8, s.length()); j++) {
                bit += x * (byte)(s.charAt(j)-'0');
                x /= 2;
            }
            i += 8;
            stuffed[k++] = bit;
        }
        //return k;
    }

    public byte deStuffBit(byte[] stuffed, byte[] destuffed){
        int bitCount = 0;
        String s = "";
        String s1 = s;
        int prev = -1;
        if (stuffed[0] == 126)
            System.out.println("\nStarting destuffing : ");
        for (int i = 1; i < stuffed.length; i++) {
            int b1 = stuffed[i];
            for (int j = 0; j < 8; j++) {
                int x = b1 >> (7 - j) & 1;
                if (x == 1) {
                    if (x == prev) bitCount++;
                    s += "1";
                    s1 += "1";
                    if (bitCount == 5) {
                        StringBuilder st = new StringBuilder(s);
                        for (int z = s.length() - 1; z >= s.length() - 7; z--) {
                            st.deleteCharAt(z);
                        }
                        s = new String(st);
                        break;
                    }
                } else {
                    if (bitCount == 4) {
                        s1 += " ";
                        bitCount = 0;
                        prev = 0;
                        continue;
                    }
                    bitCount = 0;
                    s += "0";
                    s1 += "0";
                }
                prev = x;
            }
            if (bitCount == 5) break;
        }
        s1 += "0";
        System.out.println("Frame after destuffing :  01111110"+s1);
        int k = 0;
        for (int i = 0; i < s.length() - 8;){
            byte bit = 0;
            int x = 128;
            for (int j = i; j < Math.min(i + 8, s.length() - 8); j++) {
                bit += x * (byte)(s.charAt(j)-'0');
                x /= 2;
            }
            i += 8;
            //System.out.println(k);
            destuffed[k++] = bit;
        }
        byte bit = 0;
        int x = 128;
        for (int i = s.length() - 8; i < s.length(); i++){
            bit += x * (byte)(s.charAt(i)-'0');
            x /= 2;
        }
        return bit;
    }

    public boolean hasChecksumError(byte toCheck[], byte check){
        byte checksum = 0;
        for (int i = 0; i < toCheck.length; i++)
            checksum = (byte)(checksum ^ toCheck[i]);
        if (checksum == check)
            return false;
        else return true;
    }
}
