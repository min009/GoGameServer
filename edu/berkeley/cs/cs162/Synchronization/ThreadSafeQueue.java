package edu.berkeley.cs.cs162.Synchronization;

import java.util.concurrent.TimeoutException;
import java.util.*;

public class ThreadSafeQueue<E> {
	private Queue<E> q;
	private Semaphore availableSpot, totalElement;
	private int maxSize;
	public Lock add, get ;
	
    public ThreadSafeQueue(int maxNumElements) {
    	// TODO: implement me
    	q = new LinkedList<E>();
    	//s = new Semaphore(1);
    	maxSize = maxNumElements;
    	availableSpot = new Semaphore(maxSize);  //used when queue is full
    	totalElement = new Semaphore(0);
    	add = new Lock();
    	get = new Lock();
    }
    
    public void add(E element) {
    	// TODO: implement me
    	add.acquire();       //get lock
    	availableSpot.p(); //decrease available spot
    	q.add(element);    //add element
    	totalElement.v();  //increase filled spot
    	add.release();       //release lock

    }

    public E get() {
    	// TODO: implement me
    	if(totalElement.value != q.size()){
    	  System.out.println("totalElement  " + totalElement.value + "  queue size :" + q.size());
    	}
    	get.acquire();       //get lock
    	totalElement.p();  //decrease filled spot
    	E element = q.poll();    	
    	availableSpot.v(); //increase available spot
    	get.release();       //release lock
    	if(element == null){
    		 System.out.println("totalElement  " + totalElement.value + "  queue size :" + q.size());
    	     System.out.println("element "+ element);
    	}
    	return element;
    }

    public E getWithTimeout(int timeoutInMs) throws TimeoutException {
    	// TODO: implement me
    	//System.out.println("In ThreadSafeQueue.getWithTimeout\n");
    	//System.out.println("totalElement  " + totalElement.value + "  queue size :" + q.size());
    	get.acquire();
    	totalElement.p(timeoutInMs);
    	E element = q.poll();
    	availableSpot.v();
    	get.release();
    	//System.out.println("element "+ element);
    	return element;
    }
    
    //void report(String b){
    //	System.out.println(b + "totalElement: " + totalElement.value + " size of q: " + q.size() + "\n");
    //}
}
