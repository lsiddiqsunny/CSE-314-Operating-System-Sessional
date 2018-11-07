/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package UnUsed;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Farhan
 */
public class Chunker implements Runnable
{
    private final String recipientID;
    private final String fileLocation;
    private final Long chunkSize;
    private final String fileName;
    public Thread thread;
    
    public Chunker(String recipientID,String fileLocation,Long chunkSize)
    {
        this.recipientID=recipientID;
        this.fileLocation=fileLocation;
        this.chunkSize=chunkSize;
        File file=new File(fileLocation);
        fileName=file.getName();
        thread=new Thread(this);
        thread.start();
    }
    
    @Override
    public void run()
    {
        int chunkId=0;
        FileReader fr;
        BufferedReader br;
        FileWriter fw;
        BufferedWriter bw;
        
        try {
            fr=new FileReader(fileLocation);
            br=new BufferedReader(fr);
            String unfinished="";
            while(true)    {
                Long curSize=0L;
                String destination="src/Chunks/"+recipientID+"#"+fileName+"#"+Integer.toString(chunkId)+".txt";
                fw=new FileWriter(destination,true);
                bw=new BufferedWriter(fw);
                boolean finished=false; 
                if(!unfinished.equals(""))    bw.write(unfinished+"\n");
                
                while(true)    {
                    String line=br.readLine();
                    if(line==null)    {
                       finished=true;
                       break;
                    }
                    
                    if(curSize+line.length()>chunkSize)    {
                        unfinished="";
                        int it;
                        for(it=0;it<chunkSize-curSize;it++)    {
                            bw.write(line.charAt(it));
                        }
                        bw.write("\n");
                        for(;it<line.length();it++)    {
                            unfinished+=line.charAt(it);
                        }
                        break;
                    }
                    curSize+=line.length();
                    bw.write(line);
                }
                
                fr.close();
                br.close();
                fw.close();
                bw.close();
                
                if(finished)    break;
                
                chunkId++;
            }
            
            
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Chunker.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Chunker.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
