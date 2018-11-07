import java.io.File;
import java.util.Random;

/**
 * Created by ksyp on 9/29/17.
 */
public class myFile {
    public String fileSender;
    public String fileReceiver;
    public String fileName;
    public long fileSize;
    public String filePath =null;

    public int fileID;
    public int chank;
    public int chankNo;

    public myFile(){
        fileSender=null;
        fileReceiver=null;
        fileName=null;
        fileSize=0;
    }
    public myFile(String fileSender,String fileReceiver, String fileName, long fileSize){
        this.fileSender=fileSender;
        this.fileReceiver=fileReceiver;
        this.fileName=fileName;
        this.fileSize=fileSize;
        this.fileID = myHashFunction();
        this.chank = randomChankSize();
        if((fileSize%this.chank)==0){
            this.chankNo= (int) (fileSize/this.chank);
        }
        else{
            this.chankNo=(int)(fileSize/this.chank) +1;
        }
        System.out.println(fileID);
        System.out.println("Myfile created with values Name : "+fileName+" Size : "+fileSize+" Sender : "+fileSender+" Receiver : "+ fileReceiver+" fileID "+fileID+" Chank : "+this.chank+" chankNo : "+this.chankNo);
    }
    public int myHashFunction(){
        String temp = this.fileSender+this.fileName+this.fileReceiver;
        int hashValue=0;
        int key1 = 1;
        for(int i=0;i<temp.length();i++){
            key1=349*key1;
           // System.out.println(temp.charAt(i));
            //System.out.println((int)temp.charAt(i));
            hashValue = (hashValue + ((int)temp.charAt(i))*key1)%7901;
            //System.out.println(hashValue);
        }
        return Math.abs(hashValue%7901);

    }
     public int randomChankSize(){
         Random myRn = new Random();
         int x = (int)(fileSize/15) + (myRn.nextInt() % ((int)((fileSize/3)-(fileSize/15))));
         x=3;
         System.out.println(x);
         return Math.abs(x);

     }

}
