package StuffingPackage;

/**
 * Created by Rupak on 10/28/2017.
 */
public class FrameInfo {
    byte kindOfFrame;

    public FrameInfo() {
    }

    byte seqNo;
    byte ackNo;
    byte checkSum;
    int frameSize;

    public int getFrameSize() {
        return frameSize;
    }

    public void setFrameSize(int frameSize) {
        this.frameSize = frameSize;
    }

    public byte getKindOfFrame() {
        return kindOfFrame;
    }

    public void setKindOfFrame(byte kindOfFrame) {
        this.kindOfFrame = kindOfFrame;
    }

    public byte getSeqNo() {
        return seqNo;
    }

    public void setSeqNo(byte seqNo) {
        this.seqNo = seqNo;
    }

    public byte getAckNo() {
        return ackNo;
    }

    public void setAckNo(byte ackNo) {
        this.ackNo = ackNo;
    }

    public byte getCheckSum() {
        return checkSum;
    }

    public void setCheckSum(byte checkSum) {
        this.checkSum = checkSum;
    }

    public FrameInfo(byte kindOfFrame, byte seqNo, byte ackNo) {

        this.kindOfFrame = kindOfFrame;
        this.seqNo = seqNo;
        this.ackNo = ackNo;
    }

    public FrameInfo(byte kindOfFrame, byte seqNo, byte ackNo, byte checkSum) {

        this.kindOfFrame = kindOfFrame;
        this.seqNo = seqNo;
        this.ackNo = ackNo;
        this.checkSum = checkSum;
    }
}
