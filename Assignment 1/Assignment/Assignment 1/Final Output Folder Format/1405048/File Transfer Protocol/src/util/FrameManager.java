package util;

import java.io.File;
import java.io.FileInputStream;

/**
 * @author ANTU on 28-Oct-17.
 * @project 1405048
 */
public class FrameManager {
    private File file;
    private String seqNo,ackNo,kind,mainFrame,payload;
    private byte[] fileBytes, withoutStuffedBytes, stuffedBytes,payloadBytes;
    private byte checksumByte, ackNoByte, kindByte,seqNoByte;
    private int index,arrayLength;
    public String header = "01111110", withStuffedBit = "", withoutStuffedBit = "";

    //sender
    public FrameManager(File file, int kind, int seqNo, int ackNo) {
        this.file = file;
        setBytes(kind,seqNo,ackNo);
        fileBytes = new byte[(int) file.length()];
        withoutStuffedBytes = new byte[(int) (file.length() + 4)];

        showLine("In Constructor : witout er size : "+file.length()+4);
        index = 0;
        try (FileInputStream fis = new FileInputStream(file)) {
            fis.read(fileBytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        arrayLength = (int) file.length();
    }
    public FrameManager(String fileName, int kind, int seqNo, int ackNo){
        fileBytes = fileName.getBytes();
        arrayLength = fileBytes.length;
        setBytes(kind,seqNo,ackNo);
        withoutStuffedBytes = new byte[(int) (arrayLength + 4)];
    }
    //server
    public FrameManager(byte[] totalFrame){
        stuffedBytes = totalFrame;

        showLine("In Server constructor stuffed byte Length : "+stuffedBytes.length);
//        mainFrame = totalFrame;
        calculatePayloadBytes();
    }

    private void showLine(String line){
        //System.out.println(line);
    }

    private String getModifiedString(int zeroCount, String mainString){
        String fakeBit = "";
        for(int i=0;i <zeroCount ; i++){
            fakeBit+= "0";
        }
        String modifiedString = fakeBit + mainString;
        return modifiedString;
    }

    private byte getByteValue(String byteVal){
        Integer temp = Integer.parseInt(byteVal,2);
        return temp.byteValue();
    }

    private void setBytes(int kind, int seqNo, int ackNo){
        String kindString = Integer.toBinaryString(kind);
        int zeroCount = 0;
        if(kindString.length() != 8){
            zeroCount = 8 - (kindString.length() % 8);
            kindString = getModifiedString(zeroCount,kindString);
        }
        kindByte = getByteValue(kindString);

        String seqString = Integer.toBinaryString(seqNo);

        if(seqString.length() != 8){
            zeroCount = 8 - (seqString.length() % 8);
            seqString = getModifiedString(zeroCount,seqString);
        }
        seqNoByte = getByteValue(seqString);

        String ackString = Integer.toBinaryString(ackNo);
        if(ackString.length() != 8){
            zeroCount = 8 - (ackString.length() % 8);
            ackString = getModifiedString(zeroCount,ackString);
        }
        ackNoByte = getByteValue(ackString);
    }

    public byte[] getMainFrame(){
        calculateCheckSum();
        setStuffedBytes();
        showLine("In getMainFrame() stuffedBytes length : "+stuffedBytes.length);

        return stuffedBytes;
    }

    public String getFileName() {
        return new String(payloadBytes);
    }
    public byte[] getPayloadBytes(){
        return payloadBytes;
    }
    public int getSeqNo(){
        return seqNoByte;
    }
    public int getAckNo(){
        return ackNoByte;
    }

    public boolean hasCheckSumError(){
        byte calculatedCheckSum  = (byte)0 ;
        for(int i=0;i<payloadBytes.length;i++){
            calculatedCheckSum = (byte) (calculatedCheckSum + payloadBytes[i]);
        }
        calculatedCheckSum = (byte) ~calculatedCheckSum;
        if(calculatedCheckSum == checksumByte){
            return false;
        }
        return true;
    }

    private void calculateCheckSum(){
        byte checkSum  = (byte)0 ;

        withoutStuffedBytes[0] = kindByte;
        withoutStuffedBytes[1] = seqNoByte;
        withoutStuffedBytes[2] = ackNoByte;


        showLine("In caculateChkSm payload size : "+fileBytes.length);

        for(int i=0;i<fileBytes.length;i++){
            checkSum = (byte) (checkSum + fileBytes[i]);
            withoutStuffedBytes[i + 3] = fileBytes[i];
        }
        checkSum = (byte) ~checkSum;

        showLine("In caculateChkSm Checksum : "+checkSum);
        withoutStuffedBytes[fileBytes.length + 3] = checkSum;

        showLine("in caculateChkSm without er last Index : "+(fileBytes.length + 3));
    }

    private void setStuffedBytes(){
        int _count = 0, extraBitCount = 0,k = 0;
        String byteString = "";
        index = 0;
        byte headerByte = getByteValue(header);
        int bitStuffingCount = 0;
        withStuffedBit = header+"||";
        withoutStuffedBit = header+"||";

        for (byte frameByte :
                withoutStuffedBytes) {
            for (int i=7 ; i>=0; i--) {
                if (getCurrentBit(frameByte,i) == 1){
                    _count++;
                    byteString+= "1";
                    withStuffedBit+= "1";
                    withoutStuffedBit+="1";
                    bitStuffingCount++;
                    if((bitStuffingCount % 8) == 0){
                        withStuffedBit+= "||";
                    }
                }
                else{
                    byteString+= "0";
                    withStuffedBit+= "0";
                    withoutStuffedBit+= "0";
                    bitStuffingCount++;
                    if((bitStuffingCount % 8) == 0){
                        withStuffedBit+= "||";
                    }
                    _count = 0;
                }
                if(_count == 5){
                    _count = 0;
                    extraBitCount++;
                    byteString+="0";
                    withStuffedBit+= "0";
                    bitStuffingCount++;
                    if((bitStuffingCount % 8) == 0){
                        withStuffedBit+= "||";
                    }
                }
            }
            withoutStuffedBit+= "||";
        }

        //putting fake bits
        int fakeBitCount = 8 - (byteString.length() % 8);
        if(fakeBitCount == 8) fakeBitCount = 0;

        showLine("In setStuffedBytes fakeBit : "+fakeBitCount + " Extra bit : "+extraBitCount);
        extraBitCount+= fakeBitCount;
        extraBitCount/=8;

        showLine("in setStuffedBytes extrabitCount : "+extraBitCount);

        stuffedBytes = new byte[(int) (arrayLength + 4 +extraBitCount + 2)];
        int x = (int) (arrayLength + 4 +extraBitCount + 2);

        showLine(x + " In setStuffedBytes stuffedBytes length at create : "+stuffedBytes.length);
        String fakeBit = "";
        for (int i = 0; i < fakeBitCount; i++) {
            fakeBit= fakeBit + "0";
            bitStuffingCount++;
        }

        showLine("Without header");

        showLine("In setStuffedBytes \n"+byteString);


        byteString =  byteString + fakeBit;
        withStuffedBit+= fakeBit;
        if(fakeBitCount > 0){
            if((bitStuffingCount % 8) == 0){
                withStuffedBit+= "||";
            }
        }

        withStuffedBit+= header;
        withoutStuffedBit+= header;

//        System.out.println("Frame without bit stuffing : \n"+withoutBitStuffing);
//        System.out.println("Frame with bit stuffing : \n"+withBitStuffing);

        showLine("byteString Length : "+byteString.length());
        showLine("In setStuffedBytes with fake\n"+byteString);

        stuffedBytes[index++] = headerByte;
        //actual bit stuffing
        for (int i = 0; i < byteString.length(); i+=8) {
            int start = i , end = i+8;
            String byteVal = byteString.substring(start,end);
            byte fByte = getByteValue(byteVal);
            stuffedBytes[index++] = fByte;
        }

        stuffedBytes[index++] = headerByte;

        showLine("In setStuffedBytes stuffedByte Last Length : "+ index);
    }



    //server side
    private void calculatePayloadBytes(){
        int  _count = 0,stuffedBitCount = 0;
        boolean skipThisBit = false;
        String byteString = "";
        //deStuffing
        byte[] temp = stuffedBytes;

        showLine("With header and all In calculatePayloadBytes mainFrameByte/stuffedByte length : "+temp.length);


        int k = 0;
        for(int i=0; i < temp.length-1; i++){
            byte partByte = temp[i];
            for(int j=7; j>= 0 ; j--) {
                if (skipThisBit) {
                    withStuffedBit += "0";
                    stuffedBitCount++;
                    if(stuffedBitCount%8 == 0 && i != temp.length-2){
                        withStuffedBit += "||";
                    }
                    skipThisBit = false;
                    continue;
                }
                if (getCurrentBit(partByte,j) == 1) {
                    if(i != 0 || i != temp.length - 1)_count++;
                    byteString += "1";
                    withStuffedBit += "1";
                    stuffedBitCount++;
                    if(stuffedBitCount%8 == 0 && i != temp.length-2){
                        withStuffedBit += "||";
                    }
                } else {
                    _count = 0;
                    byteString += "0";
                    withStuffedBit += "0";
                    stuffedBitCount++;
                    if(stuffedBitCount%8 == 0 && i != temp.length-2){
                        withStuffedBit += "||";
                    }
                }
                if(i == 0 || i == temp.length - 1) continue;    //header
                if (_count == 5) {
                    _count = 0;
                    skipThisBit = true;
                }
            }

        }
        //how much data is valid

        showLine("************BIT DESTUFFED***********");

        showLine("In calculatePayloadBytes with Header byteString : \n"+byteString);
        showLine("With header byteString Length : "+byteString.length());
        int fakeBitCount = byteString.length() % 8;
        int endIntex = byteString.length() - fakeBitCount;
        /*for(int i=0;i< fakeBitCount;i++){
            withStuffedBit += "0";
        }*/
        withStuffedBit+= "||"+header;
        String validFrame = byteString.substring(0,endIntex);
        validFrame+=header;
//        withoutStuffedBit = validFrame;
//        System.out.println("VALID FRAME mod 8 : "+(validFrame.length()%8));
        for(int i=0;i<validFrame.length();i+=8){
            withoutStuffedBit+= validFrame.substring(i,i+8) ;
            if(i+8 != validFrame.length()){
                withoutStuffedBit+= "||";
            }
        }

        showLine("With Header without fakebits\n"+validFrame);
        showLine("same length : "+validFrame.length());

        payloadBytes = new byte[validFrame.length()/8 - 6];
        index = 0;

        showLine("Divisble by 8 : "+ (validFrame.length() % 8)+ " payloadSize =  "+payloadBytes.length);
        int head1,head2,kind,ack,seq,chek;
        head1 = 0;
        kind = head1 + 8;
        seq = kind + 8;
        ack = seq + 8;
        head2 = validFrame.length() - 8;
        chek = head2 - 8;

        //separate different portions
        for(int i=0; i < validFrame.length() ; i+=8){
            String frameByteString = validFrame.substring(i, i + 8);
            byte frameByte = getByteValue(frameByteString);

            if(i == head1 || i == head2) continue;
            if( i == kind){
                kindByte = frameByte;
                continue;
            }
            else if(i == seq){
                seqNoByte = frameByte;
                continue;
            }
            else if(i == ack){
                ackNoByte = frameByte;
                continue;
            }
            else if (i == chek){
                checksumByte = frameByte;
                continue;
            }
            payloadBytes[index++] = frameByte;
        }

        showLine("Server Side calculatePayloadBytes : Checksum : "+ checksumByte);

        showLine("payload length : "+payloadBytes.length);

        showLine("index length : "+index);
    }

    private int getCurrentBit(byte frameByte, int pos){
        return ((frameByte >> pos) & 1);
    }
}
