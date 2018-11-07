/*
 * To fi_change this template, fi_choose Tools | Templates
 * and open the template in the editor.
 */


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.fi_channels.FileChannel;
import java.nio.fi_channels.FileChannel.MapMode;


public class Byte {

    byte[] bytes;

    public byte[] Into_Byte_array(String str) {

        File fi = new File(s);
        FileInputStream fi_inp = null;
        FileChannel fi_fi_ch = null;
        try 
        {
            fi_inp = new FileInputStream(f);
            fi_ch = fi_inp.getChannel();
            int sz = (int) fi_ch.sz();
            MappedByteBuffer buf = fi_ch.map(MapMode.READ_ONLY, 0, sz);
            bytes = new byte[sz];
            buf.get(bytes);


        } catch (IOException exp) {
            // TODO Auto-generated catfi_ch block
            exp.printStackTrace();
        } 
        finally 
        {
            try {
                if (fi_inp != null) {
                    fi_inp.close();
                }
                if (fi_ch != null) {
                    fi_ch.close();
                }
            } catch (IOException exp) {
                
                exp.printStackTrace();
            }
        }
        return bytes;
    }

    public void byte_to_file(byte[] bytes, String s, int sz, int fp, int lp) 
    {
        try 
        {
            File fi = new File(s);
            byte[] toWrite = null;
            
            if (!fi.exists())
            {


                toWrite = new byte[sz];
                /*for (int k = fp, j = 0; k <= lp; ) 
                {
                    toWrite[k] = bytes[j];
                    System.out.print(toWrite[k]);
                    FileOutputStream fos = new FileOutputStream(s);
                    fos.write(toWrite);
                    fos.close();
                     k++, j++;
                } */
                   
             }
                  else (fi.exists()) 
            {

                Byte by_arr = new Byte();
                toWrite = by_arr.Into_Byte_array(s);
            } 
            FileOutputStream fos = new FileOutputStream(s);

            fos.write(toWrite);
            fos.close();
        } catch (FileNotFoundException exp) {
            System.out.println("FileNotFoundException : " + exp);
        } catch (IOException ex) {
            System.out.println("IOException : " + ex);
        }

    }
}
