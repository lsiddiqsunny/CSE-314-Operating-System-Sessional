package Stuffing;

public class Frame {
    byte kind_of_frame;
    byte sec_no;
    byte ack_no;
    byte[] payload;
    byte checksum;

    public Frame(byte kind_of_frame, byte sec_no, byte ack_no, byte[] payload) {
        this.kind_of_frame = kind_of_frame;
        this.sec_no = sec_no;
        this.ack_no = ack_no;
        this.payload = payload;
        this.checksum = (byte)(kind_of_frame ^ (byte)(sec_no ^ ack_no));
        for (int i = 0; i < payload.length; i++)
            this.checksum = (byte) (this.checksum ^ payload[i]);
    }

    public byte getChecksum() {

        return checksum;
    }

    public void setChecksum(byte checksum) {
        this.checksum = checksum;
    }

    public byte[] getPayload() {

        return payload;
    }

    public void setPayload(byte[] payload) {
        this.payload = payload;
    }

    public byte getSec_no() {

        return sec_no;
    }

    public void setSec_no(byte sec_no) {
        this.sec_no = sec_no;
    }

    public byte getKind_of_frame() {
        return kind_of_frame;
    }

    public void setKind_of_frame(byte kind_of_frame) {
        this.kind_of_frame = kind_of_frame;
    }

    public byte getAck_no() {

        return ack_no;
    }

    public void setAck_no(byte ack_no) {
        this.ack_no = ack_no;
    }
}
