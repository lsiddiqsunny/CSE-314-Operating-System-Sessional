package filetransfer;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NetworkUtil
{
	private Socket socket;
        private InputStream is;
        private OutputStream os;
	private ObjectOutputStream oos;
	private ObjectInputStream ois;
        private String name;
	public NetworkUtil(String s, int port) {
		try {
			this.socket=new Socket(s,port);  
			oos=new ObjectOutputStream(socket.getOutputStream());
			ois=new ObjectInputStream(socket.getInputStream());
                        os =socket.getOutputStream();
                        is = socket.getInputStream();
		} catch (Exception e) {
			System.out.println("In NetworkUtil : " + e.toString());
		}
	}

	public NetworkUtil(Socket s) {
		try {
			this.socket = s;
			oos=new ObjectOutputStream(socket.getOutputStream());
			ois=new ObjectInputStream(socket.getInputStream());
                        os =socket.getOutputStream();
                        is = socket.getInputStream();
		} catch (Exception e) {
			System.out.println("In NetworkUtil : " + e.toString());
		}
	}

	public Object read() throws SocketTimeoutException {
		Object o = null;
		try {
			o=ois.readObject();
		} 
                catch (SocketTimeoutException s){
                    throw s;
                }
                catch (Exception e) {
		  //System.out.println("Reading Error in network : " + e.toString());
		}
                
		return o;
	}
        
        public int readByte(byte[] data,int off,int len){
            try{
                int c = is.read(data, off, len);
                return c;
            }
            catch (IOException e){
                return -1;
            }
        }
        
        public void writeByte(byte[] data){
            try{
                os.write(data);
                
            }
            catch (IOException e){
                //return -1;
            }
        }
	
	public void write(Object o) {
		try {
			oos.writeObject(o);                        
		} catch (IOException e) {
			System.out.println("Writing  Error in network : " + e.toString());
		}
	}
        public void setName(String name){
            this.name = name;
        
        }

	public void closeConnection() {
		try {
                        socket.close();
//			ois.close();
//			oos.close();
		} catch (Exception e) {
			System.out.println("Closing Error in network : "  + e.toString());
		}
	}

    public void setSoTimeout(int i) {
            try {
                socket.setSoTimeout(i);
                //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            } catch (SocketException ex) {
                Logger.getLogger(NetworkUtil.class.getName()).log(Level.SEVERE, null, ex);
            }
    }
}

