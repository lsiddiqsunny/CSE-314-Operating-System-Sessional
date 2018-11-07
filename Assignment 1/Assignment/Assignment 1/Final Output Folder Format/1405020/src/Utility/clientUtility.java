package Utility;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Random;
import java.util.Vector;

public class clientUtility
{
	private Socket socket;
	private ObjectOutputStream outp;
	private ObjectInputStream inp;





	public clientUtility(Socket s) {
		try {
			this.socket = s;
			outp =new ObjectOutputStream(socket.getOutputStream());
			inp =new ObjectInputStream(socket.getInputStream());
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error creating inp outp");
		}
	}

	public messageObject read() {
		messageObject o = null;
		try {
			o=(messageObject) inp.readObject();
		} catch (Exception e) {
			System.out.println("No input read");
		}
		return o;
	}
	
	public boolean write(messageObject o) {
		try {
			outp.writeObject(o);
			outp.reset();
			return true;

		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error writing.");
			return false;
		}
	}

	public void closeConnection() {
		try {
			inp.close();
			outp.close();

		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error closing connections.");
		}
	}


	public  byte [] bitstuff(byte [] b,int type,int seq,int ack,int totbytes){
		int onecnt = 0;
		int inserted = 0;
		int [] intarr = new int[8];
		Vector<Byte> vb = new Vector<>();
		vb.addElement((byte)126);
		int checksum = 0;
		int [] hd ={type,seq,ack,checksum,126};
		for(int j=0;j<3;j++){
			for(int i=7;i>=0;i--){
				int mask=1<<i;
				int pr=(hd[j] & mask);
				pr=pr>>i;
				intarr[inserted]=pr;
				inserted++;
				if(inserted==8){
					int byteval=0;
					int wt=128;
					for(int k=0;k<8;k++){
						byteval=byteval+(intarr[k]*wt);
						wt=wt/2;
					}
					vb.addElement((byte)byteval);
					inserted=0;
				}
				if(pr==1){
					onecnt++;
					checksum=(checksum+1)%128;
				}
				else {
					onecnt=0;
				}
				if(onecnt==5){
					intarr[inserted]=0;
					inserted++;
					if(inserted==8){
						int byteval=0;
						int wt=128;
						for(int k=0;k<8;k++){
							byteval=byteval+(intarr[k]*wt);
							wt=wt/2;
						}
						vb.addElement((byte)byteval);
						inserted=0;
					}
					onecnt=0;
				}

			}
		}

		for(int i=0;i<totbytes;i++){
			for(int j=7;j>=0;j--){
				int mask=1<<j;
				int pr=(b[i] & mask);
				pr=pr>>j;
				intarr[inserted]=pr;
				inserted++;
				if(inserted==8){
					int byteval=0;
					int wt=128;
					for(int k=0;k<8;k++){
						byteval=byteval+(intarr[k]*wt);
						wt=wt/2;
					}
					vb.addElement((byte)byteval);
					inserted=0;
				}
				if(pr==1){
					onecnt++;
					checksum=(checksum+1)%128;
				}
				else {
					onecnt=0;
				}
				if(onecnt==5){
					intarr[inserted]=0;
					inserted++;
					if(inserted==8){
						int byteval=0;
						int wt=128;
						for(int k=0;k<8;k++){
							byteval=byteval+(intarr[k]*wt);
							wt=wt/2;
						}
						vb.addElement((byte)byteval);
						inserted=0;
					}
					onecnt=0;
				}

			}
		}

		checksum=checksum%128;

		for(int i=7;i>=0;i--){
			int mask=1<<i;
			int pr=(checksum & mask);
			pr=pr>>i;
			intarr[inserted]=pr;
			inserted++;
			if(inserted==8){
				int byteval=0;
				int wt=128;
				for(int k=0;k<8;k++){
					byteval=byteval+(intarr[k]*wt);
					wt=wt/2;
				}
				vb.addElement((byte)byteval);
				inserted=0;
			}
			if(pr==1){
				onecnt++;
			}
			else {
				onecnt=0;
			}
			if(onecnt==5){
				intarr[inserted]=0;
				inserted++;
				if(inserted==8){
					int byteval=0;
					int wt=128;
					for(int k=0;k<8;k++){
						byteval=byteval+(intarr[k]*wt);
						wt=wt/2;
					}
					vb.addElement((byte)byteval);
					inserted=0;
				}
				onecnt=0;
			}
		}

		for (int i=7;i>=0;i--){
			int mask=1<<i;
			int pr=(126 & mask);
			pr=pr>>i;
			intarr[inserted]=pr;
			inserted++;
			if(inserted==8){
				int byteval=0;
				int wt=128;
				for(int k=0;k<8;k++){
					byteval=byteval+(intarr[k]*wt);
					wt=wt/2;
				}
				vb.addElement((byte)byteval);
				inserted=0;
			}
		}

		if(inserted!=0) {
			int tbi = 8 - inserted;
			for (int i = 0; i < tbi; i++) {
				intarr[inserted] = 0;
				inserted++;
			}
			int byteval = 0;
			int wt = 128;
			for (int k = 0; k < 8; k++) {
				byteval = byteval + (intarr[k] * wt);
				wt = wt / 2;
			}
			vb.addElement((byte)byteval);
		}

		byte[] stuffed = new byte[vb.size()];
		for(int i=0;i<vb.size();i++) {
			stuffed[i] = vb.elementAt(i);
		}
		return stuffed;
	}


	public  FrameInfo bitDestuff(byte[] stuffed){
		FrameInfo fr=new FrameInfo();
		Vector<Byte> destuffed=new Vector<>();
		if(stuffed.length<7 | stuffed[0]!=126){
			fr.hasFrameError=true;
			return fr;
		}
		else{
			int onecnt=0;
			int bitcount = 0;
			int byteno,bitno;
			int [] intarr = new int[8];
			int inserted=0;
			int checksumverifier=0;
			while(true){

				byteno=1+bitcount/8;
				bitno=7-(bitcount%8);

				int mask=1<<bitno;
				if(byteno>=stuffed.length){
					fr.hasFrameError=true;
					return fr;
				}
				int pr=(stuffed[byteno] & mask);
				pr=pr>>bitno;

				if(onecnt==5 && pr==1){
					checksumverifier=(checksumverifier+128-5)%128;
					break;
				}
				else if(onecnt==5 && pr==0){
					onecnt=0;
					bitcount++;
					continue;
				}

				if(pr==1){
					onecnt++;
					checksumverifier=(checksumverifier+1)%128;

				}
				else {
					onecnt=0;
				}

				intarr[inserted]=pr;
				inserted++;
				if(inserted==8){
					int byteval=0;
					int wt=128;
					for(int k=0;k<8;k++){
						byteval=byteval+(intarr[k]*wt);
						wt=wt/2;
					}
					destuffed.addElement((byte)byteval);
					inserted=0;
				}

				bitcount++;

			}

			fr.type=destuffed.elementAt(0);
			fr.seq=destuffed.elementAt(1);
			fr.ack=(int)destuffed.elementAt(2);

			fr.payload=new byte[destuffed.size()-4];
			for(int i=3;i<destuffed.size()-1;i++){
				fr.payload[i-3]=destuffed.elementAt(i);
			}
			fr.checksum=destuffed.elementAt(destuffed.size()-1);
			hasCheckSumError(checksumverifier,fr);

			return fr;

		}
	}

	private boolean hasCheckSumError(int checksumverifier, FrameInfo fr) {

		for(int i=7;i>=0;i--){
			int mask=1<<i;
			if(((fr.checksum & mask)>>i)==1){
				checksumverifier=(checksumverifier+128-1)%128;

			}
		}
		if(checksumverifier!=fr.checksum){

			fr.hasCerror=true;
			fr.hasFrameError=true;

			return true;
		}
		else {
			fr.hasCerror=false;
			fr.hasFrameError=false;
		}
		return false;
	}


	public static void byteprint(byte b){
		for(int j=7;j>=0;j--){
			int mask=1<<j;
			int pr=(b & mask);
			pr=pr>>j;
			System.out.print(pr);
		}
		System.out.print(" ");
	}

	public void printByteArray(byte[] b){
		for(int i=0;i<b.length;i++){
			byteprint(b[i]);
		}
		System.out.println(" ");
	}

	public void printByteArray(byte[] b, int limit){
		for(int i=0;i<limit;i++){
			byteprint(b[i]);
		}
		System.out.println(" ");
	}


	public void bitInvert(byte[] stuffedByte,int by,int bt){
		if(by<stuffedByte.length){
			bt = 1<<bt;
			stuffedByte[by]= (byte) (stuffedByte[by]^bt);
		}
	}

}

