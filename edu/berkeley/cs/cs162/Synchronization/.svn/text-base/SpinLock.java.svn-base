package edu.berkeley.cs.cs162.Synchronization;

import java.util.concurrent.atomic.AtomicBoolean;

public class SpinLock {
	private AtomicBoolean value;
	
	public SpinLock(){
		value=new AtomicBoolean(false);
	}
    
    public void acquire() {
	// TODO: implement me
    	while(value.getAndSet(true)){
    	}
    }

    public void release() {
	// TODO: implement me
    	value.set(false);
    }
}

