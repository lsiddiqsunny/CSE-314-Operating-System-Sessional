
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author User
 */
public class test {
    
    
    static int checkSum = 0;
    public static void main(String[] args) throws FileNotFoundException, IOException {
        
        String location = "G:\\DS topics.txt";
        byte[] data = Files.readAllBytes(Paths.get(location));
        
        String bitStuffedString = bitStuff(data);
        int dataLen = data.length;
        
        bitDestuff(bitStuffedString, new FileOutputStream("G:\\YOMACHI.txt"));
        
        
        
                
        
        
        
        
    }
    
    
    
    
    public static String bitStuff(byte[] data){
        String bigString = "";
        
        int dataLen = data.length;
        for(int k = 0 ; k < dataLen; k++){
            byte mask = (byte) (1 << 7);
            byte temp = data[k];
            String bitPattern = "";
            for(int i = 0 ; i  < 8; i ++){
                if((mask & temp) != 0){
                    bitPattern += '1';
                    checkSum++;
                }
                else bitPattern += '0';
                temp <<= 1 ;
            }
            bigString+= bitPattern;
        }
        
        
        String bitStuffedString = "";
        int len = bigString.length();
      
        int count = 0;
        for(int i = 0 ; i < len ; i++){
            if(bigString.charAt(i) == '0'){
                bitStuffedString += '0';
                count = 0;
            }
            else{
                count++;
                bitStuffedString += '1';
                if(count == 5){
                    bitStuffedString += '0';
                    count = 0;
                }
            }
        }
        
        
        return bitStuffedString;
    }
    
    public static void bitDestuff(String bitStuffedString, FileOutputStream fos){
        String bigString = "";  
        
        int count = 0;
        int bitLen = bitStuffedString.length();

        
        for(int i = 0 ; i < bitLen; i++){
            if(bitStuffedString.charAt(i) == '0'){
                bigString += '0';
                count = 0;
            }
            else{
                count++;
                bigString += '1';
                if(count == 5){
                    i++;
                    count = 0;
                }
            }
        }
        
        
        int bigLen = bigString.length();
        byte[] frameData = new byte[bigLen >> 3];
        int k = 0;
        for(int i = 0 ; i < bigLen; i+=8){
            byte x = (byte)Integer.parseInt(bigString.substring(i, i + 8) , 2);
            frameData[k] = x;
            k++;
        }
        
        try{
            
            fos.write(frameData);

            fos.close();
        }catch(Exception e){
            
        }
    }
    
    
    
}
