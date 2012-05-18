package edu.berkeley.cs.cs162.Client;

import java.net.*;
import java.util.Random;
import java.util.concurrent.TimeoutException;

import edu.berkeley.cs.cs162.Synchronization.*;
import edu.berkeley.cs.cs162.Writable.ClientInfo;
import edu.berkeley.cs.cs162.Writable.MessageProtocol;


public class HumanPlayer extends Player {
	static Thread[] workerThreads = new Thread[2];;
	
	public ClientInfo getClientInfo() {
		ClientInfo blah = new ClientInfo();
    	blah.name.s = this.name;
    	blah.playerType = MessageProtocol.TYPE_HUMAN;
    	return blah;
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
	
  
    public void interruptPlayer(int id){
    	System.out.println(" interrupt is called !!!!!!!!!!!!!!!!!!!");
    	if (id == 0){
    		if(!workerThreads[1].isInterrupted()){
    			workerThreads[1].interrupt();
    		}
    	}
    	else{
    		if(!workerThreads[0].isInterrupted()){
    			workerThreads[0].interrupt();
    		}
    	}
    }

	
	//args is [ip address, port number, name]
    public static void main(String[] args) {
        // TODO: Write me!
    	try{ 
    		while(true){
	    		HumanPlayer player_obj = new HumanPlayer();
	        	
	        	Semaphore twh_semaphore1 = new Semaphore(0);
	        	Semaphore twh_semaphore2 = new Semaphore(0);
	        	//when done assigning roles, call V() 2 times
	        	Semaphore magical_semaphore = new Semaphore(0);
	        	
	        	byte[] ip_address = translate(args[0]);
	        	int port = Integer.parseInt(args[1]);
	        	player_obj.name = args[2];
	        	
	        	Random rand = new Random();
	        	int randNum = rand.nextInt();
	        	
	        	InetAddress IPInfo = InetAddress.getByAddress(ip_address);
	        	
	        	int id = 0;
	        	PlayerWorker w1 = new PlayerWorker(IPInfo, port, randNum, twh_semaphore1, magical_semaphore, player_obj, id);
	        	id +=1;
	    		PlayerWorker w2 = new PlayerWorker(IPInfo, port, randNum, twh_semaphore2, magical_semaphore, player_obj, id);
	    		
	    		w1.setPartner(w2);
	    		w2.setPartner(w1);
	    		Thread t1 = new Thread(w1);
	    		Thread t2 = new Thread(w2);
	    		workerThreads[0] = t1;
	    		workerThreads[1] = t2;
	    		
	    		t1.start();
	    		t2.start();
	    		
	    		//block until both workers finishes 3 way handshake
	    		try{
	    			twh_semaphore1.p(10000);
	    			twh_semaphore2.p(10000);
	    		}catch(TimeoutException e){
	    			System.out.println("Timed out from waiting for returned random number");
	    			w1.readyExit();
	    			w2.readyExit();
	    			System.exit(-1);
	    		}
	    		
	    		//check if two workers are alive
	    		if(!t1.isAlive() || !t2.isAlive()){
	    			System.out.println("one or more of the worker thread died...");
	    			w1.readyExit();
	    			w2.readyExit();
	        		System.exit(-1);
	    		}
	    		
	    		if(w1.getReceivedRan() > w2.getReceivedRan()){
	    			w1.sToc = true;
	    			w2.sToc = false;
	    		}
	    		else{
	    			w2.sToc = true;
	    			w1.sToc = false;
	    		}
	    		//done assigning roles
	    		magical_semaphore.v();
	    		magical_semaphore.v();
	    		
	    		//The main method will end once both worker threads have terminated.
	            try{
	            	t1.join();
	            	t2.join();
	            }catch(InterruptedException e){
	            	System.out.println("The main method in printing observer has been interrupted before its two worker thread has ended. ");
	            }
    		}	// end of while()           
        }catch(UnknownHostException e){
        	System.out.println("Problem connecting to the specified IP address.");
        	System.exit(-1);
        }
        
    }//end main
}
