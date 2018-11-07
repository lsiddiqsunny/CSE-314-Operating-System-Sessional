package Client;

import java.util.ArrayList;

public class test {
    public static void main(String[] args) {
        ArrayList<byte[]>list = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            byte[] b = new byte[1];
            b[0] = (byte)i;
            list.add(i,b);
        }
        for (int i = 0; i < 10 ; i++)
            System.out.println(list.get(i)[0]);
    }
}
