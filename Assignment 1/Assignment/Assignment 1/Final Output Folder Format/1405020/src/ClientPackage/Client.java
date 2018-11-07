package ClientPackage;

import Utility.FrameInfo;
import Utility.clientUtility;
import Utility.messageObject;
import Utility.recievedFileInfo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Random;
import java.util.Scanner;
import java.util.Vector;

/**
 * Created by Asus on 9/24/2017.
 */
public class Client implements Runnable {
	Socket clientSocket;
	clientUtility cu;
	int clientId;
	Thread t;


	Client(){
		Scanner sc = new Scanner(System.in);
		System.out.println("Enter Roll No.: ");
		int client = sc.nextInt();
		System.out.println("Enter Server IP Address: ");
		sc.nextLine();

		String serv=sc.nextLine();

		try {
			clientSocket = new Socket(serv, 33333);
			cu = new clientUtility(clientSocket);

		} catch (Exception e) {
		//	e.printStackTrace();
			System.out.println("Error Connecting with Server.");
		}

		if(cu!=null) {
			messageObject message = new messageObject(1);
			message.setClientId(client);
			cu.write(message);
			messageObject ansFromServer = cu.read();
			if (ansFromServer != null) {
				if (ansFromServer.getMessage().equals("Success")) {
					clientId = client;
					System.out.println("Login Successful! You are logged in as Roll " + clientId);

					Thread t = new Thread(this);
					t.start();

					try {
						t.join();
					} catch (InterruptedException e) {
				//		e.printStackTrace();
					}

				} else {
					System.out.println("This Roll is already connected.Please try later.");
				}
			} else {
				System.out.println("Error connecting to server. Please try later.");
			}

			try {
				cu.closeConnection();
				clientSocket.close();
			} catch (IOException e) {
			//	e.printStackTrace();
				System.out.println("Error Closing Socket");
			}
		}
		else{
			System.out.println("Try later.");
		}

	}

	public static void main(String args[]){
		new Client();

	}

	@Override
	public void run() {

		boolean exit = false;

		while (exit!=true){
			System.out.println("\nType \"send\" to send a file. \"View\" to view recieved messages.");
			System.out.println("Type \"exit\" to log out.");

			Scanner sc = new Scanner(System.in);
			String order = sc.nextLine();
			if(order.equalsIgnoreCase("send")){
				System.out.println("Enter filename with directory: ");
				String filename=sc.nextLine();
				File file = new File(filename);
				if(file!=null && file.isFile()!=false) {
					System.out.println("Filename: " + file.getName() + " Size: " + file.length()+" Bytes");
					System.out.println("Enter reciever: ");
					int recieverId = sc.nextInt();
					System.out.println("Reciever: "+recieverId);
					messageObject reqMessage = new messageObject(3);
					reqMessage.setSender(clientId);
					reqMessage.setReciever(recieverId);
					reqMessage.setFilename(file.getName());
					reqMessage.setFilesize(file.length());
					cu.write(reqMessage);

					messageObject reply = cu.read();
					if(reply!=null && reply.getMessage().equals("success")) {

						FileInputStream fin=null;
						System.out.println("File Id: "+reply.getFileId() + " Payload Chunk Size: " + reply.getMaxchunksize()+" Bytes");
						System.out.println("Type 1 for bit invert error , 2 for frame drop , 3 for no error");
						int choice = sc.nextInt();
						int turn=0;


						int mx=reply.getMaxchunksize();
						int totchunk;
						if(file.length()%mx ==0){
							totchunk=(int)(file.length()/mx);
						}
						else {
							totchunk=(int)(((file.length())/mx)+1);
						}
						int chunksent=0;
						byte[] b = new byte[mx];
						long totstored=0;
						int N=8;
						byte [][] framebytes = new byte[8][];
						int prev_set_success=1;
						int frame_in_set=N;
						int success_sent=0;
						byte [] stuffed;
						int by=0,bt=0;

						Random rand = new Random();
						int frdrop = rand.nextInt(N);
						int tr;
						if(totchunk%N!=0){
							tr=(totchunk/N)+1;
						}
						else{
							tr=totchunk/N;
						}

						int trdrop =rand.nextInt(tr)+1;


						frdrop=2;
						trdrop=1;

						if(choice==2) {
							System.out.println("Will Simulate Frame drop in turn: "+trdrop+ " for Frame: "+frdrop);
						}
						else if(choice==1){
							System.out.println("Will Simulate File Corruption(Bit Invert) in turn: "+trdrop+" for Frame: "+frdrop);
						}


						try {
							fin = new FileInputStream(file);
							while((fin.available()!=0 || prev_set_success!=1)){
								if(prev_set_success==1) {
									turn++;
								}
								else {
									System.out.println("Re sending from seq. No. "+success_sent);
								}
								//	System.out.println("turn: "+turn);
								if(prev_set_success==1) {
									if (N * mx <= fin.available()) {
										frame_in_set = N;
									} else {
										if ((fin.available() % mx) == 0) {
											frame_in_set = fin.available() / mx;
										} else {
											frame_in_set = (fin.available() / mx) + 1;
										}
									}
								}
								for (int i=success_sent;i<frame_in_set;i++){
									if(prev_set_success==1){
										if(b.length<fin.available()){
											fin.read(b);
											System.out.println("\n\nPayload read from file: ");
											cu.printByteArray(b);
											stuffed=cu.bitstuff(b,1,i,0,b.length);
											System.out.println("After Stuffing: ");
											cu.printByteArray(stuffed);
										}
										else {
											int ava=fin.available();
											fin.read(b,0,fin.available());
											System.out.println("\n\nPayload read from file: ");
											cu.printByteArray(b,ava);
											stuffed=cu.bitstuff(b,1,i,0,ava);
											System.out.println("After Stuffing: ");
											cu.printByteArray(stuffed);
										}
										//		System.out.println("i: "+i+" fbl: "+framebytes.length+"stuffed: "+stuffed.length);
										framebytes[i]=stuffed;

										if(turn==trdrop && choice ==1 && i==frdrop){
											System.out.println("Stuffed Byte array before bit inversion: ");
											cu.printByteArray(stuffed);
								//			System.out.println("Simulating Bit Invert in turn: "+trdrop+" Frame: "+frdrop);
											Random rd = new Random();
											int paysize = stuffed.length - 6;
											by = (rd.nextInt(paysize) + 5);
											bt = rd.nextInt(8);
											System.out.println("Simulating Bit Invert in Byte: "+by+" Bit: "+bt);
											cu.bitInvert(stuffed,by,bt);
											System.out.println("After bit inversion: ");
											cu.printByteArray(stuffed);
										}

										if(turn!=trdrop  || choice != 2 || i!=frdrop) {
											messageObject ms = new messageObject(5);
											ms.setFileId(reply.getFileId());
											ms.setFilebytes(stuffed);
											cu.write(ms);
											System.out.println("Frame with seq. no. "+i+" sent.");
										}
										else{
											System.out.println("Simulating Frame Drop in turn: "+trdrop+" Frame seq. no: "+frdrop);
										}

									}
									else {

										if(turn==trdrop && choice == 1 && i==frdrop){
											cu.bitInvert(framebytes[i],by,bt);
										}
										messageObject ms = new messageObject(5);
										ms.setFileId(reply.getFileId());
										ms.setFilebytes(framebytes[i]);
										cu.write(ms);
										System.out.println("Frame with seq. no. "+i+" re-sent.");
									}
								}

								try {
									clientSocket.setSoTimeout(5000);
									prev_set_success=0;
									for (int i = success_sent; i < frame_in_set; i++) {
										messageObject rp6=cu.read();
										if(rp6!=null){

											System.out.println("\n\nReceived Acknowledgement Frame: ");
											cu.printByteArray(rp6.getFilebytes());
											FrameInfo fr=cu.bitDestuff(rp6.getFilebytes());
											System.out.println("After Destuff: ");
											System.out.println(fr);

											if(fr.hasFrameError == false){

												if(fr.seq == success_sent){
													success_sent++;
												}
												else{
													System.out.println("Error in sequence order , Expected "+success_sent+" , Found "+fr.seq);
												}

												if(success_sent==frame_in_set){
													prev_set_success=1;
													success_sent=0;
												}

												if(fr.ack==1){
													chunksent++;
													System.out.println("Chunk sent: "+chunksent+" of "+totchunk);
													continue;
												}
												else if(fr.ack==2){
													chunksent++;
													System.out.println("Chunk sent: "+chunksent+" of "+totchunk);
													messageObject sn7= new messageObject(7);
													sn7.setFileId(rp6.getFileId());
													sn7.setMessage("complete");
													cu.write(sn7);
													messageObject rp8=cu.read();
													if(rp8!=null){
														if(rp8.getMessage().equals("success")){
															System.out.println("File Successfully Sent!");
														}
														else{
															System.out.println("Sorry!File was not sent.Please try again.");
														}
													}
													else{
														System.out.println("Sorry!File was not sent.Please try again.");
													}
												}

											}
											else{
												if(fr.hasCerror==true){
													System.out.println("Checksum error in acknowledgement frame.");
												}
												else {
													System.out.println("Error in acknowledgement frame.");
												}
											}
										}
										else{
											throw new Exception("rp6Null");
										}
									}
								}catch (Exception e){
									//	System.out.println("dadada");
									System.out.println("Acknowledgement for sequence number "+success_sent+" not received.");
								}
							}
						} catch (Exception e) {
					//		e.printStackTrace();
							System.out.println("Error reading file.Please try again");
						}finally {
							try {
								fin.close();
								clientSocket.setSoTimeout(0);
							} catch (Exception e) {
						//		e.printStackTrace();
							}
						}
					}
					else{
						if(reply==null){
							System.out.println("Error connecting with server.Try Later.");
						}
						else if(reply.getMessage().equals("Failiure-recieverOffline")){
							System.out.println("Sorry.The Reciever is offline.Please try again.");
						}
						else if(reply.getMessage().equals("Failiure-bufferOverflow")){
							System.out.println("Sorry.File could not be sent at the moment.Please try later");

						}
						else{
							System.out.println("Sorry.File could not be sent at the moment.Please try later");
						}
					}

				}
				else{
					System.out.println("No such file found.Please try again.");
				}

			}
			else if(order.equalsIgnoreCase("View")){
				messageObject m = new messageObject(10);
				cu.write(m);

				messageObject rp11= cu.read();

				if (rp11!=null) {
					Vector<recievedFileInfo> r = rp11.getR();

					System.out.println("Recieved Files: "+r.size()+"\n");

					for (int i=0;i<r.size();i++){
						System.out.println((i+1)+".");
						System.out.println("File: "+r.elementAt(i).getFilename());
						System.out.println("Sender: "+r.elementAt(i).getSenderId());
						System.out.println("File Size: "+r.elementAt(i).getFilesize()+" Bytes\n");
					}

					while(true) {

						System.out.println("Type \"recieve <fileno.>\" to recieve file number <fileno.>");
						System.out.println("Type \"reject <fileno.>\" to reject file number <fileno.>");
						System.out.println("Type \"back\" to go back.");

						String inp = sc.nextLine();
						String[] val = inp.split(" ");
						if (val[0].equalsIgnoreCase("recieve")) {
							try {
								//		System.out.println(val[0]);
								//		System.out.println(val[1]);
								int fileno = Integer.parseInt(val[1]);
								fileno=fileno - 1;
								if (fileno < r.size()) {

									System.out.println("Enter destination directory: ");
									String dir = sc.nextLine();
									File f = new File(dir);

									if(f.isDirectory()==true) {

										try(FileOutputStream fos =
															new FileOutputStream(dir+"\\"+r.elementAt(fileno).getFilename(),true)) {


											long storedsize=0;
											long fileId = r.elementAt(fileno).getFileId();
											messageObject ms = new messageObject(12);
											ms.setFileId(fileId);
											ms.setMessage("recieve");
											cu.write(ms);

											while (true) {
												messageObject rp = cu.read();

												if(rp!=null && rp.getType()==13) {
													fos.write(rp.getFilebytes(), 0, (int) rp.getFilesize());
													storedsize=storedsize+rp.getFilesize();
													System.out.println("Recieved: "+storedsize);
												}
												else if(rp!=null && rp.getType()==14){
													if(storedsize==r.elementAt(fileno).getFilesize()){
														messageObject rp15=new messageObject(15);
														rp15.setFileId(fileId);
														rp15.setMessage("success");
														cu.write(rp15);

														System.out.println("Successfully recieved!");
													}
													else{
														messageObject rp15=new messageObject(15);
														rp15.setFileId(fileId);
														rp15.setMessage("failiure");
														cu.write(rp15);

														System.out.println("Failed to recieve. Please try again.");
													}
													break;
												}
												else{
													if(rp==null){
														System.out.println("Error connecting with server. Try Later.");
													}
													System.out.println("File not recieved. Try Again.");
													break;
												}
											}
										} catch (Exception e){
										//	e.printStackTrace();
											System.out.println("problem connecting with server.Try Later.");
										}

									}
									else{
										System.out.println("Directory not recognized.Please try again.");
									}

								} else {
									System.out.println("Wrong fileno input. please try again.");
								}
							} catch (Exception e) {
							//	e.printStackTrace();
								System.out.println("Wrong fileno input. please try again.");
							}
						} else if (val[0].equalsIgnoreCase("reject")) {
							try {
								int fileno=Integer.parseInt(val[1]);
								fileno=fileno-1;

								if(fileno<r.size()){
									messageObject ms = new messageObject(16);
									ms.setFileId(r.elementAt(fileno).getFileId());
									ms.setMessage("reject");
									cu.write(ms);

									messageObject rp17 = cu.read();
									if(rp17!=null){
										if (rp17.getMessage().equals("deleted")){
											System.out.println("File "+r.elementAt(fileno).getFilename()+" rejected.");
										}
									}
									else{
										System.out.println("Error reading from server. Try later.");
									}

								}


							}
							catch (Exception e){
							//	e.printStackTrace();
								System.out.println("Wrong fileno input. please try again.");
							}



						} else if(val[0].equalsIgnoreCase("back")) {
							break;
						}
						else {
							System.out.println("Command Not recognized.");
						}

					}
				}
				else{
					System.out.println("Error reading from server. Please try later.");
				}

			}
			else if(order.equalsIgnoreCase("exit")){
				System.out.println("You are logging out.");
				messageObject m = new messageObject(18);
				cu.write(m);

				exit=true;
			}
			else {
				System.out.println("Command not recognized.Please Try Again\n");
			}

		}

	}
}
