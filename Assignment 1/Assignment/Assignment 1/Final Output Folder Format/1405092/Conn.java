import java.io.*;
import java.net.*;
import java.util.concurrent.atomic.AtomicLong;
class Conn
{
	
	public static double ERROR_RATE;// = 0.01;
public static double SWITCH_PACKET_MISSING_RATE;
	
	public static double DropRate(){return SWITCH_PACKET_MISSING_RATE;}
	public static double ErrorRate(){return ERROR_RATE;}
	
	public static void main(String args[]) throws Exception
	{		
		double dropRate=0;
		double errorRate=0;
		int ac=args.length;
				
		if (ac>0)dropRate=Double.parseDouble(args[0]);
		if (ac>1)errorRate=Double.parseDouble(args[1]);
		
		Conn.SWITCH_PACKET_MISSING_RATE=dropRate;
		Conn.ERROR_RATE=errorRate;
		
		ServerSocket welcomeSocket= new ServerSocket(5008);
		Socket connectionSocket[]=new Socket[2];
		DataInputStream[] inFromClient=new DataInputStream[2];	    
		DataOutputStream outToClient[]=new DataOutputStream[2];
		//while (true)
		/*for (int i=0;i<2;i++)
		{
			connectionSocket[i]=welcomeSocket.accept();
			inFromClient[i]=new DataInputStream(connectionSocket[i].getInputStream());
			outToClient[i]=new DataOutputStream(connectionSocket[i].getOutputStream());
		}
		SThread A2B=new SThread("A","B",inFromClient[0],outToClient[1]);
        SThread B2A=new SThread("B","A",inFromClient[1],outToClient[0]);
        A2B.join();
        B2A.join();
        //close the sockets here		
	}*/	
}

