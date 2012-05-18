package edu.berkeley.cs.cs162.Synchronization;

import java.util.concurrent.TimeoutException;
import java.util.*;

public class Lock {
    private int value;
    private SpinLock spLock;
    private Queue<Thread> waitQueue;
    private Thread putInQ;
    
	public Lock(){
		value=1;
		spLock=new SpinLock();
		waitQueue = new LinkedList<Thread>();
	}
	
	public void acquire() {
		// TODO: implement me
		try{

			spLock.acquire();
			if (value==0){
				putInQ = Thread.currentThread();
				waitQueue.add(putInQ);
				spLock.release();
				Thread.sleep(1000000000);
			}
			else{
				value=0;
				spLock.release();
			}
		}catch (InterruptedException e){
			spLock.acquire();
			value=0;//changed
			spLock.release();
		}

	}

    public void acquireWithTimeout(int timeoutInMs) throws TimeoutException {
	// TODO: implement me
    	try{
	    	spLock.acquire();
	    	if(value==0){
	    		putInQ = Thread.currentThread();
	    		waitQueue.add(putInQ);
	    		spLock.release();
	    		Thread.sleep(timeoutInMs);
	    		throw new TimeoutException();
	    	}
	    	else
	    	{
	    		value=0;
	    		spLock.release();
	    	}
    	}
	    catch (InterruptedException e) {
	    	spLock.acquire();
			value=0;//changed
	    	System.out.println("Thread just got woken up");
			spLock.release();
	        }
    }
    
    public void release() {
	// TODO: implement me
    	spLock.acquire();
    	if (!waitQueue.isEmpty())
    	{
    		Thread t = waitQueue.poll();
    		t.interrupt();
    	}
    	else{
    		value=1;
    		}
    	spLock.release();
    }
}

