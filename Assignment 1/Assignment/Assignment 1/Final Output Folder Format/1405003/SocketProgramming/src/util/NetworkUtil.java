package util;

import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class NetworkUtil
{
    public Socket socket;
    private ObjectOutputStream oos;
    private OutputStream os;
    private ObjectInputStream ois;
    private InputStream is;
    public int ID;

    public NetworkUtil(String s, int port,int id) {
        try {
            this.socket=new Socket(s,port);
            is = socket.getInputStream();
            os = socket.getOutputStream();
            oos=new ObjectOutputStream(os);
            ois=new ObjectInputStream(is);
            //System.out.println("Debug");

            this.ID = id;

        } catch (Exception e) {
            System.out.println("In NetworkUtil : " + e.toString());
        }
    }

    public NetworkUtil(Socket s) {
        try {
            this.socket = s;
            is = socket.getInputStream();
            os = socket.getOutputStream();
            oos=new ObjectOutputStream(socket.getOutputStream());
            ois=new ObjectInputStream(s.getInputStream());

            //System.out.println("Debug");





        } catch (Exception e) {
            System.out.println("In NetworkUtil : " + e.toString());
        }
    }

    /*public int read(int a){
        try{
            int c = ois.read();
            return c;
        }
        catch (IOException e){
            return -1;
        }
    }*/

    public Object read() {
        Object o = null;
        try {
            o=ois.readObject();
        }
        catch (SocketTimeoutException e){return null;}
        catch (Exception e) {
            //System.out.println("Reading Error in network : " + e.toString());
        }
        return o;
    }

    public int write(Object o) {
        try {
            oos.writeObject(o);
            //oos.flush();
            return 0;
        } catch (IOException e) {
            System.out.println("Writing  Error in network : " + e.toString());
            return -10;
        }
    }

    public int writeByte(byte[] bytes,int size){

        try {
            os.write(bytes,0,size);
            //System.out.println(size);
            //os.flush();
            //System.out.println(size);
            return 0;
        } catch (IOException e) {
            System.out.println("Writing  Error in network : " + e.toString());
            return -10;
        }
    }

    public int readByte(byte[] buf){
        try {
            //System.out.println("Allah");
            //System.out.println("kochu");
            int count = is.read(buf);
            //is.reset();
            //System.out.println(count);
            //System.out.println("kochuAgain");
            //System.out.println(count);
            /*try{
                ois.readObject();
            }
            catch (Exception e){

            }*/
            return count;

            //for(int i=off;i<off+len;i++)System.out.println(bytes[i]);
        }
        catch (SocketTimeoutException e){return -5;}
        catch(Exception e){
            System.out.println(e.toString());
            return -10;
        }

    }

    /*public String readByte(){
        try {
            String str = (String)ois.readObject();
            return str;
        }
        catch(Exception e){
            return null;
        }
    }*/

    public void closeConnection() {
        try {
            is.close();
            os.close();
            ois.close();
            oos.close();
        } catch (Exception e) {
            System.out.println("Closing Error in network : "  + e.toString());
        }
    }

    public void set(int id){

        ID = id;
    }

    public int getID(){return ID;}
}

