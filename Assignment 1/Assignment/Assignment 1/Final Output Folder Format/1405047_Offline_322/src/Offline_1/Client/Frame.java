package Offline_1.Client;

import java.util.Vector;

/**
 * Created by Shahriar Sazid on 29-Oct-17.
 */
public class Frame {
    public Frame() {}
    private void display(int number) {
        int mask = 1 << 7;

        for (int i = 7; i >= 0; i--) {
            if (((1 << i) & number) != 0)
                System.out.print(1);
            else
                System.out.print(0);
        }
    }

    public byte[] stuff_payload(byte[] chunk) {
        Vector<Byte> vect_payload = new Vector<>();
        int five_cnt = 0;
        int insert = 0;
        int k = 0;
        byte new_bt;
        for (int i = 0; i < chunk.length; i++) {
            int musk = 0b10000000;
            int bt = chunk[i];
            for (int j = 0; j < 8; j++) {
                if ((bt & musk) == 0) {
                    five_cnt = 0;
                    insert = insert << 1;
                    k++;
                } else {
                    five_cnt++;
                    insert = insert << 1;
                    k++;
                    insert++;
                    if (five_cnt == 5) {
                        if (k == 8) {
                            k = 0;
                            new_bt = (byte) insert;
                            vect_payload.add(new_bt);
                            if(new_bt == 32)
                            insert = 0;
                        }
                        five_cnt = 0;
                        insert = insert << 1;
                        k++;
                    }
                }
                musk >>= 1;
                if (k == 8) {
                    k = 0;
                    new_bt = (byte) insert;
                    vect_payload.add(new_bt);
                    insert = 0;
                } else if (j == 7 && i == chunk.length - 1) {
                    insert <<= 8 - k;
                    new_bt = (byte) insert;
                    vect_payload.add(new_bt);
                    insert = 0;
                }
            }
        }
        byte[] payload = new byte[vect_payload.size()];

        for (int i = 0; i < vect_payload.size(); i++) {
            payload[i] = vect_payload.get(i).byteValue();
        }
        return payload;
    }

    public byte calculate_checksum(byte[] bytes) {
        int cnt = 0;
        for (int i = 0; i < bytes.length; i++) {
            int m = 0b10000000;
            int int_byte = bytes[i];
            for (int j = 0; j < 8; j++) {
                if ((int_byte & m) != 0) {
                    cnt++;
                }
                m >>= 1;
            }
        }
        if (cnt % 2 == 0) return 0;
        else return 1;
    }

    public byte[] make_frame(byte[] chunk, int type, int sqnc, int trans) {
        byte[] pre_frame1 = new byte[chunk.length + 3];
        pre_frame1[0] = (byte) type;
        pre_frame1[1] = (byte) sqnc;
        pre_frame1[2] = (byte) trans;
        for (int i = 0; i < chunk.length; i++) {
            pre_frame1[3 + i] = chunk[i];
        }
        byte[] pre_frame = stuff_payload(pre_frame1);
        byte checksum = calculate_checksum(pre_frame);
        byte[] frame = new byte[pre_frame.length + 3];
        frame[0] = 0b01111110;
        for (int i = 0; i < pre_frame.length; i++) {
            frame[1 + i] = pre_frame[i];
        }
        frame[1 + pre_frame.length] = checksum;
        frame[frame.length - 1] = 0b01111110;
        return frame;
    }
}
