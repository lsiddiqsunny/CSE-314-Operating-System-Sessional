package Model;

import javax.swing.*;
import java.awt.*;
import java.io.Serializable;
import java.util.ArrayList;

import static Client.Client.cgui;

public class Frame implements Serializable,Cloneable{
    byte []in;
    ArrayList<Boolean>out;
    byte checkSum;
    byte seqNum;
    byte []destuffed;

    boolean status=false;

    public Frame( byte in[] ,byte seqNum){

        this.in=in;
        out=new ArrayList<>();
        doStuffing(seqNum);

    }

    public Frame(ArrayList <Boolean>list){
        doDeStuffing(list);
    }

    private void doStuffing(byte seqNum) {
        int count=0;
        int cSum=0;

        for(int i=0;i<in.length;i++){
            for(int j=0;j<8;j++){
                if(getBitAt(in[i],j)==0){
                    count=0;
                    out.add(false);
                }
                else {
                    count++;
                    cSum=(cSum+1)%127;

                    out.add(true);

                    if(count==5){
                        count=0;
                       out.add(false);
                    }
                }
            }
        }


        //add seqNum
        for(int i=0;i<8;i++){
            if(getBitAt(seqNum,i)==0){
                out.add(false);
            }
            else {
                out.add(true);
            }
        }


        //add  checksum

        for(int i=0;i<8;i++){
            if(getBitAt((byte) cSum, i)==0){
                out.add(false);
            }
            else {
                out.add(true);
            }
        }


        //System.out.println("CS: "+ cSum);
    }



    public void   doDeStuffing(ArrayList<Boolean>list){

        ArrayList<Boolean>temp=new ArrayList<>();
        int count=0;


        //get seqNum
        int k=0;

        for(int i=list.size()-16;i<list.size()-8;i++){
            if(list.get(i).equals(false)){
                seqNum=setBitAt(seqNum,k,0);
            }
            else {
                seqNum=setBitAt(seqNum,k,1);
            }

            k++;
        }



        //shift to boolean array

        for(int i=0;i<list.size()-16;i++){

            if(count==5 && list.get(i).equals(false)){
                count=0;
            }
            else if(count==5 && list.get(i).equals(true)){
                status=false;
                return;
            }

            else if(list.get(i).equals(false)){
                temp.add(false);
                count=0;
            }
            else if (list.get(i).equals(true)){
                temp.add(true);
                count++;
            }
        }





        //get actual byte[]

        int cSum=0;
        destuffed=new byte[temp.size()/8];
        int l=0;

        for(int i=0;i<destuffed.length;i++){
            for(int j=0;j<8;j++){
                if(temp.get(l).equals(true)){
                    destuffed[i]=setBitAt(destuffed[i],j,1);
                    cSum=(cSum +1)%127;
                }
                else {
                    destuffed[i]=setBitAt(destuffed[i],j,0);
                }

                l++;

            }
        }





        //get checkSum
        int m=0;
        for(int i=list.size()-8;i<list.size();i++){
            if(list.get(i).equals(false)){
                checkSum=setBitAt(checkSum,m,0);

            }
            else {
                checkSum=setBitAt(checkSum,m,1);

            }

            m++;
        }

       if((byte)cSum==checkSum){
            status=true;

       }
       else {
            status=false;
        }

    }




    int getBitAt(byte b,int i){
        return (b>>i) & 0x01;
    }

    byte setBitAt(byte b,int i, int value){
        byte temp=(byte)0;
        temp= (byte) (temp | (value << i));
        b= (byte) (b|temp);
        return b;
    }

    public void printBit(byte []bytes, JTextArea console){
        for (int i=0;i<bytes.length;i++){
            for(int j=0;j<8;j++){
                //System.out.print(getBitAt(bytes[i],j));
                console.append(String.valueOf(getBitAt(bytes[i],j)));
            }
        }
       // System.out.println();
    }

    public void printBitFromArray(ArrayList list,JTextArea console){
        for (int i = 0; i <list.size() ; i++) {
            if(list.get(i).equals(true)){
                //System.out.print(1);
                console.append("1");
            }
            else {
               // System.out.print(0);
                console.append("0");
            }
        }
       // System.out.println();
    }


    @Override
    public Object clone() throws CloneNotSupportedException{
        return super.clone();
    }

    public synchronized ArrayList<Boolean> getStuffedBit() {
        return out;
    }

    public synchronized byte[] getDeStuffedBit() {
        return destuffed;
    }

    public byte getSeqNum() {
        return seqNum;
    }
    public boolean getStatus(){
        return status;
    }

    public byte getCheckSum() {
        return checkSum;
    }
}
