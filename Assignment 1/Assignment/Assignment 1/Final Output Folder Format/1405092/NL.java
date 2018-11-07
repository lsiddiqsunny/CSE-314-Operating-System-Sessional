import java.util.*;
import java.net.*;
import java.io.*;

//+++++++++++++++++Class: NetworkLayer+++++++++++++++++++++++++++++++++
class NL extends Thread{
	public DataLinkLayer dll;	
	public Buffer<Packet> packetBuffer;
	BufferedReader br;
	
	boolean enable;
	//=======================================================
	public static void main(String args[]) throws Exception{	
		int port=0;
		int timeOut=7;
		
		int ac=args.length;
		if (ac>0)port=Integer.parseInt(args[0]);
		if (ac>1)timeOut=Integer.parseInt(args[1]);
		
		
		NetworkLayer nl=new NetworkLayer(port, timeOut);
		networkLayer.join();
	}
	//=======================================================
	/*public NetworkLayer(int swPort, int timeOut){
		br= new BufferedReader(new InputStreamReader(System.in));					
		dll=new DataLinkLayer(timeOut,this);	
		packetBuffer=new Buffer<Packet>("NL Packet", 1);
		enable=false;		
		start();
	}*/
	//=======================================================	
	public void run(){ 
		try{			
			while(true){				
				String in=br.readLine();	
				
				int grt=in.indexOf('>');
				String it=in.substring(0, grt);
				
				
				 if(it.equals("file") == true)
				{
					String filename=in.substring(grt+1);
					
					String filepath=""+filename;
					 //create file object
                                    int startByte;
				    File fi; 
                                      fi= new File(filepath);
				    long filesize=file.length();
				    int startByte;
				    int chunkSize=512;
				    
				  /*  for(int i=0;i<filesize;i=i+chunkSize)
				    {
				    	startByte=i;
				    	
				    	FileInputStream in=new FileInputStream(file);
				    	byte[] bytes=new byte[512];
				    	
				    	if(((int)filesize-startByte)<512)
				    	{
				    		chunkSize=((int)filesize-startByte);
				    	}
				    	in.read(bytes, startByte, chunkSize);
				    	
				    	String temp=new String("file"+" "+filename+" "+filesize+" "+startByte+" "+chunkSize+"-"+bytes.toString());
				    	Packet p=new Packet(temp.getBytes());
						storePacket(p);					
						while(true){if (getEnable()==true) break;}
						dll.addNetworkLayerReadyEvent();		
				    }
				}
*/
                                  else if(it.equals("msg") == true)
				{
				int colon=input.indexOf(':');				
				int not=Integer.parseInt(input.substring(grt+1,colon));			
				String text=input.substring(colon+1);
				
				}
				
			}			
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	//=================Call From others====================
	/*Packet getPacket(){//from DLL
		Packet p=null;
		try{
			synchronized(packetBuffer){
				p=packetBuffer.get();
				enable=false; //do not allow to create next event
				packetBuffer.notify();
			}	
		}
		catch(Exception e){}
		return p;
	}
	//======================================================
	Packet storePacket(Packet p){//from DLL		
		try{
			synchronized(packetBuffer){
				if(packetBuffer.full()) packetBuffer.wait();
				packetBuffer.store(p);
				//packetBuffer.notify();
			}	
		}
		catch(Exception e){}
		return p;
	}
*/
	//======================================================
	
	//======================================================
	synchronized void setEnable(){
		enable=true;
	}
	//======================================================
	synchronized void disable(){
		enable=false;
	}
synchronized boolean getEnable(){
		return enable;
	}
	//======================================================
	/*public void to_network_layer(Packet b){
		System.out.println("\tShowing Packet: "+new String(b.getHeader())+" "+new String(b.getPayload())+"\n");
	}*/
}
