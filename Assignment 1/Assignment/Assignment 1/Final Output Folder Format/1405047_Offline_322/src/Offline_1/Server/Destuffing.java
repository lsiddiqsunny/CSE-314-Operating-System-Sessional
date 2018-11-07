package Offline_1.Server;

import java.util.Vector;

/**
 * Created by Shahriar Sazid on 29-Oct-17.
 */
public class Destuffing {
    public Destuffing(){}
    private void display( int number ){
        int musk = 1 << 7;
        for(int i=7; i>=0; i--) {
            if( ((1 << i)&number) != 0)
                System.out.print(1);
            else
                System.out.print(0);
        }
    }

    public byte[] destuuff_payload(byte[] payload){
        Vector<Byte> vect_chunk = new Vector<>();
        int five_cnt = 0;
        int insert = 0;
        int k = 0;
        byte new_bt;
        int flg= 0;
        for(int i =0; i<payload.length;i++){
            int musk = 0b10000000;
            int j = 0;
            if(flg == 1){
                flg = 0;
                musk = 0b01000000;
                j = 1;
            }
            for(; j<8;j++){
                int bt = payload[i];
                if((bt & musk) == 0){
                    five_cnt=0;
                    insert = insert << 1;
                    k++;
                } else {
                    five_cnt++;
                    insert = insert << 1;
                    insert++;
                    k++;
                    if(five_cnt==5){
                        five_cnt = 0;
                        if(j==7){
                            flg = 1;
                        } else{
                            musk>>=1;
                            j++;
                        }
                    }
                }
                musk>>=1;
                if(k==8){
                    k = 0;
                    new_bt = (byte)insert;
                    vect_chunk.add(new_bt);
                }
            }
        }
        byte[] chunk = new byte[vect_chunk.size()];
        for(int i = 0; i<vect_chunk.size(); i++){
            chunk[i] = vect_chunk.get(i).byteValue();
        }
        return chunk;
    }
    public void print(byte[] payload){
        byte[] chunk = destuuff_payload(payload);
        for(int i = 0; i<payload.length;i++){
            display(payload[i]);
            System.out.print(" ");
        }
        System.out.println();
        for(int i = 0; i<chunk.length;i++){
            display(chunk[i]);
            System.out.print(" ");
        }
    }
}
