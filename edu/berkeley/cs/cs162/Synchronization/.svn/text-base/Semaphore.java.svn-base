package edu.berkeley.cs.cs162.Synchronization;

import java.util.concurrent.TimeoutException;
import java.util.*;

public class Semaphore {
	public int value=0;  //temporarily public, shoulb be 
	private Lock lock;
	private Thread putInQ;
	private Queue<Thread> waitingQueue = new LinkedList<Thread>(); 
	
    public Semaphore(int initialCount) {
	// TODO: implement me
    	if (initialCount<0){
    		throw new IllegalArgumentException ("no negative value");
    	}
    	else{
    		value = initialCount;
    	}
    	lock = new Lock();
    }

    public void p() {
	// TODO: implement me
    	try{
    		lock.acquire();
	    	if(value==0){
	    		putInQ = Thread.currentThread();
	    		waitingQueue.add(putInQ);
	    		lock.release();
	    		Thread.sleep(1000000000);
	    	}
	    	else
	    	{
	    		value--;
	    		lock.release();
	    	}
    	}
	    catch (InterruptedException e) {
	    	//System.out.println("Thread just got woken up");
	    	lock.acquire();
	    	value--;
	    	lock.release();

	        }
    }
    
    public void p(int duration)throws TimeoutException {
    	// TODO: implement me
        	try{
    	    	lock.acquire();
    	    	if(value==0){
    	    		putInQ = Thread.currentThread();
    	    		//System.out.print( " i am in semaphone value: " + value);
    	    		waitingQueue.add(putInQ);
    	    		lock.release();
    	    		Thread.sleep(duration);
    	    		throw new TimeoutException();
    	    	}
    	    	else
    	    	{
    	    		value--;
    	    		lock.release();
    	    	}
        	}
    	    catch (InterruptedException e) {
    	    	lock.acquire();
    	    	value--;
    	    	lock.release();

    	        }
        }
    
    public void v(){
    	lock.acquire();
    	if (!waitingQueue.isEmpty())
    	{
    		Thread t = waitingQueue.poll();
    		value++;
    		t.interrupt();
    	}
    	else{
    		value++;
    		}
    	lock.release();
    }
    public void report(){
    	System.out.println("value is " + value);
    }
    public int count(){
    	return value;
    	
    }
    public void set(int insert){
    	value = insert;
    }
   }
//
//
//    public void p(int timeoutInMs) throws TimeoutException {
//	// TODO: implement me
//    	lock.acquire();
//    	Date d = new Date();
//    	long start=d.getTime();
//    	while(value==0){
//    		
//    	}
//    	long end=d.getTime();
//    	if((end-start)>timeoutInMs){
//    		lock.release();
//    		throw new TimeoutException("Timeout");
//    	}
//    	else
//    		value--;
//    	lock.release();
//    }
//
//    public void v() {
//	// TODO: implement me
//    	lock.acquire();
//    	value++;
//    	lock.release();
//    }
//}
