package Test;

import StuffingPackage.Stuffing;

public class test {
    public static void main(String[] args) {
        byte b[] = new byte[256];
        Stuffing  s = new Stuffing();
        System.out.println(b);
        for (int i = 0; i < 256; i++) {
            b[i]= (byte) i;
            System.out.println(b[i]);
        }
        s.printArray(b,256);
    }
}
