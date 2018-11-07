package ByteCalculation;

/**
 * Created by user on 10/20/2017.
 */
public class Bytes {




    public static byte get(byte b,int pos){
        int a = (b & (1<<pos));
        if(a==0)return 0;
        return 1;
    }

    public static void printByte(byte b){
        for(int i=7;i>-1;i--){
            byte a = get(b,i);
            System.out.print(a);
        }
    }

    public static void print(byte[] b){
        int size = b.length;
        for(int i=0;i<size;i++){
            printByte(b[i]);
        }
        System.out.println("");
    }
    public static void print(byte[] b,int len){
        //int size = b.length;
        for(int i=0;i<len;i++){
            printByte(b[i]);
        }
        System.out.println("");
    }
}
