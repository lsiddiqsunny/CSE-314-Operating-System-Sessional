package server;

import ByteCalculation.Bytes;
import Helper.ByteAsObject;
import util.NetworkUtil;

import java.io.ByteArrayOutputStream;

/**
 * Created by user on 9/22/2017.
 */
public class readThreadServer implements Runnable{

    private NetworkUtil nc;
    private Thread thr;

    public readThreadServer(NetworkUtil nc){
        this.nc = nc;
        thr = new Thread(this);
        thr.start();
    }

    public void run(){
        int receiver=0;
        while(true){
            Object o = nc.read();

            if(o==null){
                nc.closeConnection();
                Main.remove(nc.getID());
                break;
            }
            if(o instanceof Integer) {
                receiver = (int) o;
                if(Main.findUser(receiver)==false)nc.write("u");
                else nc.write("z");
            }
            else if(o instanceof Long){
                long fileSize = (long) o;


                if(Main.checkSpace(fileSize)==false){
                    nc.write("u");
                    continue;
                }
                else nc.write("z");
                String fileName = (String) nc.read();
                int fileId = Main.getId();


                //generating random chunk size

                //long min = fileSize / 100;
                //long max = fileSize / 10;
                //long chunkSize = ThreadLocalRandom.current().nextLong(min, max + 1);
                long chunkSize = fileSize/100;
                if (chunkSize == 0) chunkSize++;

                nc.write(chunkSize);

                //chunkSize = 20000;
                //reading the file by chunks



                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                try {
                    //FileOutputStream fos = new FileOutputStream("E:\\mahina.pdf");

                    int count;
                    int temp = (int) (long) fileSize / (int) chunkSize;
                    if ((int) (long) fileSize % chunkSize != 0) temp++;
                    //System.out.println(temp);
                    int frameCount = 1;
                    while (true) {
                        //int size = (int)nc.read();
                        //byte[] buf = new byte[size];
                        /*byte[] result = new byte[4];
                        byte[] again = new byte[1];
                        nc.readByte(result);
                        Bytes.print(result);
                        nc.readByte(again);
                        ByteBuffer wrapped = ByteBuffer.wrap(result);
                        int num = wrapped.getInt(); // 1
                        int bit = 8-again[0];
                        //Bytes.print(result);
                        System.out.println(num);
                        buf = new byte[num];*/
                        ByteAsObject byteAsObject = (ByteAsObject)nc.read();
                        byte[] buf = byteAsObject.buf;
                        count = buf.length;
                        //count = nc.readByte(buf);
                        if(frameCount > buf[1] && buf[2]>1){
                            System.out.println("Frame again received");
                            byte[] bytes = new byte[4];
                            bytes[0] = 126;
                            bytes[3] = 126;
                            bytes[1] = 1;
                            bytes[2] = buf[1];
                            nc.writeByte(bytes,4);
                            continue;
                        }
                        else if(frameCount != buf[1]){
                            System.out.println("Frame received and ignored");
                            //System.out.println(frameCount + " " + (int)buf[1]);
                            continue;
                        }
                        System.out.println("Header , no of Frame , no of times sent , type of the frame , checkSum , payload , trailer");
                        Bytes.print(buf);
                        //System.out.println(buf[2]);
                        //System.out.println("Allah");

                        //System.out.println(buf[0]);
                        //fos.write(buf, 0, count);

                        /*byte[] payload = new byte[count-2];

                        for(int i=1;i<count-1;i++){
                            for(int j=7;j>=0;j--){
                                if(Bytes.get(buf[i],j)==1){
                                    payload[i-1] = (byte)(payload[i-1] | (1<<j));
                                }
                                else {
                                    payload[i-1] = (byte)(payload[i-1] & (~(1<<j)));
                                }
                            }
                        }*/
                        int oneCount = 0;
                        boolean miu = false , loop = true;
                        byte[] payload = new byte[(int)chunkSize + 1];
                        int currByte = 0 , currbit = 7;
                        for(int i=5;i<count && loop;i++){
                            for(int j=7;j>=0 && loop;j--){

                                if(Bytes.get(buf[i],j)==1){
                                    if(miu){
                                        loop=false;
                                        break;
                                    }
                                    payload[currByte] |= 1<<currbit;
                                    currbit--;
                                    if(currbit==-1){
                                        currbit = 7;
                                        currByte++;
                                        //if(currByte==chunkSize)loop=false;
                                    }
                                    oneCount++;
                                    if(oneCount==5){
                                        oneCount = 0 ;
                                        miu=true;
                                    }

                                }
                                else{
                                    oneCount=0;
                                    if(miu){
                                        miu = false;
                                    }
                                    else{
                                        payload[currByte] &= ~(1<<currbit);
                                        currbit--;
                                        if(currbit==-1){
                                            currbit = 7;
                                            currByte++;
                                            //if(currByte==chunkSize)loop=false;
                                        }
                                    }
                                }
                            }


                        }
                        byte check = checkSum(payload,currByte);
                        if(check!=buf[4]){
                            System.out.println("Checksum didn't match, so ignored");
                            continue;
                        }
                        frameCount++;
                        System.out.println("Granted");
                        Bytes.print(payload,payload.length-1);
                        //System.out.println(count);
                        /**/
                        temp--;
                        //Bytes.print(payload);

                        baos.write(payload, 0, currByte);
                        //System.out.println("before");
                        byte[] ack = new byte[4];
                        ack[0] = ack[3] = 126;
                        ack[1] = 1;
                        ack[2] = (byte)(frameCount - 1);
                        count = nc.writeByte(ack,4);
                        if(count==-10)
                        {
                            break;
                        }
                        //Helper oo = nc.read();
                        //if(oo==null){
                        //    count=-10;
                        //    break;
                       // }
                        //String foo = (String)oo;
                        //System.out.println(foo);

                        //Main.show();
                        //System.out.println("after");
                        if (temp == 0) break;
                        //System.out.println(temp);
                    }
                    //System.out.println("matha");
                    if(count==-10){
                        nc.closeConnection();
                        Main.remove(nc.getID());
                        break;
                    }
                    System.out.println(nc.read());

                        /*try {
                            fos.close();
                        }
                        catch(IOException e){
                            System.out.println("shesh");
                        }
                        finally {

                        }*/
                } catch (Exception e) {

                }

                byte[] data = baos.toByteArray();
                //System.out.println(data.length + " " + fileSize);
                if(data.length!=fileSize){
                    nc.write("Error while sending");
                }
                else if (Main.checkSpace(data.length) == false) {
                    nc.write("Not enough space");
                }
                else
                {
                    try{
                        Main.increase(data.length);
                        nc.write("Successfully transferred to server");
                        Main.process(nc.getID(),receiver,fileName,fileSize,fileId,data);
                    }
                    catch (NullPointerException e){
                        nc.closeConnection();
                        Main.remove(nc.getID());
                        break;
                    }
                }

                //sender,receiver,fileName,fileSize,fileId,byte[]
                //
            }
            else
            {
                //System.out.println((String)o + " yuyu");
                nc.closeConnection();
                Main.remove(nc.getID());
                break;
            }
        }
    }
    public byte checkSum(byte[] temp,int count){
        byte ret = 0;
        for(int i=0;i<count;i++){

            for(int j=7;j>=0;j--){
                if(Bytes.get(temp[i],j)==1)ret = (byte)(ret ^ (1<<j));
            }
        }
        return ret;
    }
}
