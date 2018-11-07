
import java.io.*;
import java.io.IOException;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author ASUS
 */
public class makeFile {
    public static void main(String []args) throws IOException
    {
        File file=new File("test3.txt");
      //  file.createNewFile();
        FileOutputStream fos=new FileOutputStream(file);
        int a=9;
        byte []b=new byte[1];
        b="a".getBytes();
        for(int i=0;i<1000000;i++)
        {
            fos.write(b);
        }
     //   System.out.println(file.getTotalSpace());
    }
    
}
