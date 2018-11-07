class ABC
{

    int mask=0x01;
    boolean mode;
    int bit_ind=0;
    int number_bits=0;
    int checksum=0;
    byte ack_data=0;
    byte seq=0;
    byte ack=0;


    public ABC()
    {
        mode=false;
    }

    byte[] stuff_bits(byte[] a)
    {
        byte[] input;
        byte[] output;
        input=new byte[a.length+1];
        output=new byte[a.length*3];
        input[a.length]=0;
        System.arraycopy(a,0,input,0,a.length);


        System.out.println("checksum : "+ checksum);
        input[input.length-1] = (byte) checksum;
        System.out.println("input[input.length-1] : "+ input[input.length-1]);

        System.out.println("Frame: ");
        print(input);
        for(int j=0;j<input.length;j++) {
            get_bit(input[j]);
        }

        int ones=0;
        int x=0;

        for(int j=0;j<input.length*8;j++){
            if(ones==5){
                ones=0;
                set(output,x,0);
                x++;
            }
            int p=get(input,j);
            set(output,x,p);
            x++;
            if(p!=0){
                ones++;
            }
            else ones=0;

        }
        set(output,x,0);
        x++;
        for(int l=0;l<6;l++) {
            set(output, x, 1);
            x++;
        }
        set(output,x,0);
        x++;
        number_bits=x;
        System.out.println("numbits "+x);
        if(x%8==0) bit_ind=(x/8);
        else{
            bit_ind=(x/8)+1;
        }

        byte[] temp=new byte[bit_ind+4];
        temp[0] = (byte) 126;
        temp[1]=ack_data;
        temp[2]=seq;
        temp[3]=ack;   //data/ack field,seq number field,ack number field

        System.arraycopy(output,0,temp,4,bit_ind);
        return temp;
    }

    void setAck_data(int val){
        ack_data=(byte) val;
    }


    int get_sum(byte[] o,int len)
    {
        checksum=0;
        for(int j=0;j<len*8;j++)
        {
            if(get(o,j)!=0) checksum++;
        }
        return checksum;
    }

    int get(byte[] in,int n)
    {
        return in[n/8] & mask<<(7-(n%8));

    }

    void set(byte[] out,int n,int val)
    {
        if(val!=0) {
            out[n / 8] = (byte) (out[n / 8] | mask<<(7-(n%8)));
        }
        else{
            out[n/8]= (byte) (out[n/8] & (~(out[n/8] & mask<<(7-(n%8)))));
        }
    }

    byte[] destuff_bits(byte[] a)
    {
        int ones=0;
        int x=0;
        byte[] des=new byte[a.length];
        for(int j=8*4;j<(a.length)*8;j++)
        {
            if(ones==5){
                ones=0;
                if(get(a,j)!=0){
                    x=x-6;
                    break;
                }
                j++;

            }

            int p=get(a,j);
            if(p!=0) ones++;
            else ones=0;


            set(des,x,p);
            x++;

        }
        if(x%8==0) bit_ind=(x/8);
        else{
            bit_ind=(x/8)+1;
        }

        int ok=get_sum(des,bit_ind-1);
        System.out.println("destuff er sum: " + ok);
        if((byte) ok==des[bit_ind-1]) {
            System.out.println("Successful");
        }
        else{
            System.out.println("Retransmit");
            mode=true;
        }
        byte[] temp=new byte[bit_ind-1];
        System.arraycopy(des,0,temp,0,bit_ind-1);

        return temp;
    }

    void print(byte[] a)
    {
        System.out.println("  "+a.length+"  ");
        for(int i=0;i<a.length;i++) {
            System.out.print(a[i] +"  ");
        }
        System.out.println();
    }

    void get_bit(byte v) {
        for (int i = 7; i >=0; i--) {
            int b=(v & (0x1<<i))>>i;
            System.out.print(b + "  ");
        }
        System.out.println();

    }


}
