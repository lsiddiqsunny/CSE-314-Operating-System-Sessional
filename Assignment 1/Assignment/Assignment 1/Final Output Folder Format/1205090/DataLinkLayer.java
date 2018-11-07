/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Abdullah Al Maruf
 */
package dlloffline;

import java.util.*;
import java.net.*;
import java.io.*;

//+++++++++++++++++Class: DataLinkLayer+++++++++++++++++++++++++++++++++
class DataLinkLayer extends Thread{
	SimPhy simPhy;			
	
	NetworkLayer nl;
	
	//Buffer<Integer> eventQueue;
	Buffer<MyEvent> eventQueue;
	MyTimer[] timers;
		
	public static int frameBufferSize=10;
	public static int packetBufferSize=10;
	
	//Event Enumeration 
	public static final int network_layer_ready=1;
	public static final int frame_arrival=2;
	public static final int cksum_err=3;
	public static final int timeout=4;
	
	public static final int MAX_SEQ=7;		
	
	public int timeout_duration;//=10;	
			
	//=======================================================
	DataLinkLayer(int timeOut, NetworkLayer n){		
		nl=n;
		simPhy=new SimPhy(this);			
		eventQueue=new Buffer<MyEvent>("DLL Event Queue",100);		
		timers=new MyTimer[MAX_SEQ+1];	
		for(int i=0;i<MAX_SEQ+1;i++) timers[i]=new MyTimer(i,this);		
		timeout_duration=timeOut;			
		start();
	}
	//=======================================================
	public void addNetworkLayerReadyEvent(){		
		addEvent(new MyEvent(network_layer_ready,0));
	}
	//=======================================================
	public void addFrameArrivalEvent(){
		addEvent(new MyEvent(frame_arrival,0));		
	}
	//=======================================================
	public void addTimeOutEvent(MyTimer m, int eType){//both for ack and timeout
		MyEvent e=new MyEvent(eType,1);
		e.setArg(m,0);
		addEvent(e);										
	}	
	//=======================================================
	public void addChecksumErrorEvent(){
		addEvent(new MyEvent(cksum_err,0));
	}
	//=======================================================
	public void addEvent(MyEvent m){ //from simport class
		try{
			synchronized(eventQueue){
				if (eventQueue.full()) eventQueue.wait();	
				eventQueue.store(m);
				eventQueue.notify();
			}
		}
		catch(Exception e){
		}		
	}	
	//=======================================================
	public void run(){ 
		protocol5();
	}
	//=======================================================
	void protocol5(){
		try{				
			int next_frame_to_send;	/* MAX_SEQ > 1; used for outbound stream */
			int ack_expected;	/* oldest frame as yet unacknowledged */
			int frame_expected;	/* next frame expected on inbound stream */
			Frame r;	/* scratch variable */
			Packet buffer[]=new Packet[MAX_SEQ+1];	/* buffers for the outbound stream */
			int nbuffered;	/* # output buffers currently in use */
			int i;	/* used to index into the buffer array */
			MyEvent event;

			enable_network_layer();	/* allow network_layer_ready events */
			ack_expected = 0;	/* next ack expected inbound */
			next_frame_to_send = 0;	/* next frame going out */
			frame_expected = 0;	/* number of frame expected inbound */
			nbuffered = 0;	/* initially no packets are buffered */

			 while (true) {				 
				 event=wait_for_event();	/* four possibilities: see event_type above */				 
				 switch(event.getType()) { 
					case network_layer_ready:	/* the network layer has a packet to send */
							/* Accept, save, and transmit a new frame. */
							buffer[next_frame_to_send]=from_network_layer(); /* fetch new packet */
							nbuffered = nbuffered + 1;	/* expand the sender's window */
							send_data(next_frame_to_send, frame_expected, buffer);	/* transmit the frame */
							next_frame_to_send=inc(next_frame_to_send);	/* advance sender's upper window edge */														
							break;

					case frame_arrival:	/* a data or control frame has arrived */
							r=simPhy.from_physical_layer();	/* get incoming frame from physical layer */
			  
							if (r.getSeqNo() == frame_expected) {
									/* Frames are accepted only in order. */
									nl.to_network_layer(r.getPayload());	/* pass packet to network layer */
									frame_expected=inc(frame_expected);	//inc(frame_expected);	/* advance lower edge of receiver's window */																		
							}
							
							 /* Ack n implies n - 1, n - 2, etc.  Check for this. */
							while (between(ack_expected, r.getAckNo(), next_frame_to_send)) {
									/* Handle piggybacked ack. */
									nbuffered = nbuffered - 1;	/* one frame fewer buffered */
									stop_timer(ack_expected);	/* frame arrived intact; stop timer */
									ack_expected=inc(ack_expected);	//inc(ack_expected);	/* contract sender's window */
							}                
							break;

					case cksum_err: ;	/* just ignore bad frames */
							break;
			  
					case timeout:	/* trouble; retransmit all outstanding frames */
							MyTimer expired_timer=(MyTimer)event.getArg(0);													
							next_frame_to_send = ack_expected;	/* start retransmitting here */
							for (i = 1; i <= nbuffered; i++) {
									send_data(next_frame_to_send, frame_expected, buffer);	/* resend 1 frame */
									next_frame_to_send=inc(next_frame_to_send); //inc(next_frame_to_send);	/* prepare to send the next one */
							}							
				 }
			  
				 if (nbuffered < MAX_SEQ)
					enable_network_layer(); 
				 else
					disable_network_layer();
			  }		
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	//=======================================================
	int inc(int i){
		if (i<MAX_SEQ) i=i+1;
		else i=0;
		return i;
	}
	//=======================================================
	void enable_network_layer(){
		nl.setEnable();
	}
	//=======================================================
	void disable_network_layer(){
		nl.disable();
	}
	//=======================================================
	MyEvent wait_for_event(){	
		MyEvent event=null;
		try{
			synchronized(eventQueue){
				if (eventQueue.empty()) {					
					eventQueue.wait();				
				}				
				event=eventQueue.get();
				eventQueue.notify();
			}
		}
		catch(Exception e){}
		return event;	
	}
	//=======================================================
	Packet from_network_layer(){
		return nl.getPacket();
	}
	//=======================================================
	void send_data(int frame_nr, int frame_expected, Packet buffer[]){
		/* Construct and send a data frame. */
		Packet p = buffer[frame_nr];	/* insert packet into frame */
		int seq = frame_nr;	/* insert sequence number into frame */
		int ack = (frame_expected + MAX_SEQ) % (MAX_SEQ + 1);	 
		Frame s=new Frame(seq, ack, p.getBytes());	/* scratch variable */		  
		simPhy.to_physical_layer(s);	/* transmit the frame */
		start_timer(frame_nr);	/* start the timer running */
	}
	//=======================================================
	boolean between(int a, int b, int c){
	/* Return true if (a <=b < c circularly; false otherwise. */
		if (((a <= b) && (b < c)) || ((c < a) && (a <= b)) || ((b < c) && (c < a)))
			return(true);
		else
			return(false);
	}
	//=======================================================
	void start_timer(int i){		
		timers[i].stopTimer();
		timers[i]=new MyTimer(i,this);
		timers[i].startTimer(timeout, timeout_duration);
	}
	//=======================================================
	void stop_timer(int i){		
		timers[i].stopTimer();		
	}	
}


