package edu.berkeley.cs.cs162.Client;

import edu.berkeley.cs.cs162.Synchronization.*;
import edu.berkeley.cs.cs162.Writable.*;
import java.util.concurrent.TimeoutException;
import java.net.*;
//import java.io.IOException;
import java.util.Random;
//import edu.berkeley.cs.cs162.common.*;
import java.lang.Integer;
/*
 * idea: Main method has two worker threads, ClientToServer and ServerToClient interactions.
 *       worker threads would be given the randNum, and wait for reply. Once both have numbers
 *       returned by the gameServer, the main method will assign the cTos, and sToc threads
 *       respectively. Once assigned, the worker threads would then be allowed to proceed to
 *       their while loop. Once there is an error or disconnect, the worker thread's readyExit()
 *       function will be called to end their while loop hence terminating them. The main method
 *       will be the last to terminate since it blocks while waiting for the worker threads to join.
 */

//fsdf
public class PrintingObserver extends Observer {

	protected Worker sToC; //server to client
	protected Worker cToS;  //client to server
	public Semaphore s1, s2; //for blocking until both workers receive random numbers
	public Semaphore canStartLoop1, canStartLoop2; //for blocking worker thread from running before beng assigned role.
	//	ClientInfo CInfo; // the client info needed to send in connect messages
	InetAddress ipInfo; // required ip information for creating sockets.
	Thread thread1, thread2;
	Worker w1p;
	Worker w2p;
	ClientInfo cInfo;
	public PrintingObserver(){
		s1=new Semaphore(0);
		s2=new Semaphore(0);
		canStartLoop1 = new Semaphore(0);
		canStartLoop2 = new Semaphore(0);
		cInfo = new ClientInfo();
	}

	public void setThreads(Thread t1, Thread t2,Worker w1, Worker w2){
		thread1 = t1;
		thread2 = t2;
		w1p = w1; 
		w2p = w2;
	}
	public void killWorkers(){
		w1p.readyExit();
		w2p.readyExit();
		thread1.interrupt();
		thread2.interrupt();
	}

	public ClientInfo getClientInfo() {
		return cInfo;
	}

	//sets the worker types
	public void setWorkers(Worker w1,Worker w2){
		if(w1.getReceivedRan()>w2.getReceivedRan()){
			sToC=w1;
			w1.setflag_sToc();
			cToS=w2;
			try{w2.s.setSoTimeout(3000);}catch(Exception e){System.out.println("Error setting soTimeout");}
		}
		else{
			w2.setflag_sToc();
			sToC=w2;
			cToS=w1;
			try{w1.s.setSoTimeout(3000);}catch(Exception e){System.out.println("Error setting soTimeout");}
		}
	}

	public static byte[] translate(String ip4address){
		byte[] blah = new byte[4];

		int blah_index = 0;
		int start_index = 0;
		for(int i = 0; i < ip4address.length(); i++){
			if(ip4address.charAt(i) == '.'){
				blah[blah_index] = (byte) Integer.parseInt(ip4address.substring(start_index, i));
				blah_index++;
				start_index = i+1;
			}
			else if(i == ip4address.length() - 1){
				blah[blah_index] = (byte) Integer.parseInt(ip4address.substring(start_index));
			}
		}
		System.out.format("%d,%d,%d,%d", (int)blah[0],(int)blah[1],(int)blah[2],(int)blah[3]);
		return blah;
	}

	/*
	 * 1. extract ipaddress and port and client name from args
	 * 2. Instantiate InetAddress objects form ipAddress
	 * 2. instantiate new worker object
	 * 3. start the worker thread
	 * 4. block until workers got rand nums
	 * 5. Once workers get random number sent from game server, game server 
	 * 	  assign who is receiver and transmitter.
	 * 6. Then allows the worker to continue to their while loop
	 * 7. Once all workers have terminated the main will terminate 
	 *    after both threads join().
	 */
	public static void main(String[] args) {

		PrintingObserver ob=new PrintingObserver();
		byte[] ip_address = translate(args[0]);
		int port = Integer.parseInt(args[1]);
		ob.cInfo.name.s = args[2];
		ob.cInfo.playerType = MessageProtocol.TYPE_OBSERVER;

		Random rand=new Random();
		int randNum=rand.nextInt();

		try{ //getting the InetAddress object
			InetAddress IPInfo = InetAddress.getByAddress(ip_address);
			ob.ipInfo = IPInfo;
		}catch(UnknownHostException e){
			System.out.println("Problem connecting to the specified IP address.");
			System.exit(-1);
		}
		System.out.println("Before creating w1");
		Worker w1=new Worker(ob.ipInfo, port, randNum, ob.s1, ob.canStartLoop1, ob.cInfo, ob);
		System.out.println("Before after w1 before w2");
		//~~~~~~~Remember to change this back!!!
		try{
			InetAddress test = InetAddress.getByAddress(new byte[]{127, 0, 0, 1});
			Worker w2=new Worker(ob.ipInfo, port, randNum, ob.s2, ob.canStartLoop2, ob.cInfo, ob);
			w1.setPartner(w2);
			w2.setPartner(w1);

			Thread t1 = new Thread(w1);
			Thread t2 = new Thread(w2);
			ob.setThreads(t1, t2,w1,w2);
			t1.start();
			t2.start();
			//block until both workers receive the random numbers from server.f
			try{
				ob.s1.p(10000);
				ob.s2.p(10000);
			}catch(TimeoutException e){
				System.out.println("Timed out from waiting for returned random number");
				w1.readyExit();
				w2.readyExit();
				t1.interrupt();
				t2.interrupt();
				System.exit(-1);
			}
			//check if two workers are alive
			if(!t1.isAlive() || !t2.isAlive()){
				System.out.println("one or more of the worker thread died...");
				w1.readyExit();
				w2.readyExit();
				t1.interrupt();
				t2.interrupt();
				System.exit(-1);
			}
			//Assign workers to be transmitting and receiving workers.
			ob.setWorkers(w1, w2);
			//after being assigned their roles, the worker threads semaphore 
			// is released. The worker threads have also automatically connected
			// to the game server.
			ob.canStartLoop1.v();
			ob.canStartLoop2.v();

			//The main method will end once both worker threads have terminated.
			try{
				t1.join();
				t2.join();}catch(InterruptedException e){
					System.out.println("The main method in printing observer has been interrupted before its two worker thread has ended. ");
				}
		}catch(Exception e){System.out.println("Error creating test InetAddress");}




	}


}
