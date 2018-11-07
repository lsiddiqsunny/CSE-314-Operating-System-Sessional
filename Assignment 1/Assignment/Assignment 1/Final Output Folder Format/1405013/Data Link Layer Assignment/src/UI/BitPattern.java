package UI;

public class BitPattern {
    public static void printBits(byte[] arr, LogWindow logWindow) {
        for(byte bt : arr) {
            StringBuilder pattern = new StringBuilder("");
            int btInt = (int)bt;
            for(int j = 0; j < 8; ++j) {
                int bit = btInt & 0x80;
                bit >>= 7;
                btInt <<= 1;
                pattern.append(bit);
            }
            logWindow.appendToFrameLog(pattern.toString());
        }
    }
}
