package Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileStuff {
    public static void main(String[] args) throws IOException {
        int chunkSize = 100;
        FileStuff fileStuff = new FileStuff();
        File file = new File("./test/5.jpg");
        FileInputStream fileInputStream = new FileInputStream(file);
        int fileLength = (int) file.length();
        System.out.println(fileLength);
        int var = fileLength;
        byte[][] byteArray = new byte[fileLength/chunkSize+1][chunkSize];
        byte[] array = new byte[chunkSize];
        int i=0;
        while(var>chunkSize)
        {
            fileInputStream.read(array);
            byteArray[i]=array;
            i++;
            var -=chunkSize;
        }
        fileInputStream.read(byteArray[i],0,var);


        FileOutputStream fileOutputStream = new FileOutputStream(new File("./test/10.jpg"));
        var=fileLength;
        i=0;
        while(var>chunkSize)
        {
            fileOutputStream.write(byteArray[i]);
            i++;
            var -=chunkSize;
        }
        fileOutputStream.write(byteArray[i],0,var);
        fileOutputStream.close();
    }


}
