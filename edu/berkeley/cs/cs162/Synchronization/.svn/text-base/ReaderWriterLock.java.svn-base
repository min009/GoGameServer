package edu.berkeley.cs.cs162.Synchronization;

/**
 * Synchronization class that provides classic
 * reader-writer locking behavior. This lock should
 * allow multiple readers, and prioritize waiting
 * writers over waiting readers.
 * 
 */
//public class ReaderWriterLock {
//    Lock lock;
//    int AR,WR,AW,WW;
//    ConditionVariable okToWrite, okToRead;
//    public ReaderWriterLock() {
//    	lock = new Lock();
//    	okToWrite = new ConditionVariable(lock);
//    	okToRead = new ConditionVariable(lock);
//    	AR = 0;
//    	WR = 0;
//    	AW = 0;
//    	WW = 0;
//    }
//    
//    /**
//     * Locks the readers lock
//     * until ok to read
//     */
//    public void readLock() {
//        lock.acquire();
//        while(WW + AW > 0){
//        	WR++;
//        	System.out.println("reader is going to sleep\n");
//        	okToRead.ConditionWait();
//        	System.out.println("reader woke up\n");
//        	WR--;
//        }
//        AR++;
//        lock.release();
//        	
//    }
//    
//    /**
//     * Unlocks the readers lock
//     * if there are no active reader and more that 0 waiting writer
//     * signal to waiting writers
//     */
//    public void readUnlock() {
//    	lock.acquire();
//    	AR--;
//    	if(AR == 0 && WW>0){
//    		okToWrite.ConditionSignal();
//    	}
//        lock.release();
//    }
//    
//    /**
//     * Locks the writers lock
//     * if there are active writer or active reader, block
//     * else proceed
//     */
//    public void writeLock() {
//        lock.acquire();
//        System.out.println("writeLock is holding the lock now \n");
//        System.out.println("out of while loop\n");
//        while(AW +AR>0){
//        	WW++;
//        	System.out.println("writer going to sleep\n");
//        	okToWrite.ConditionWait();
//        	System.out.println("writer woke up\n");
//        	WW--;
//        }
//        AW++;
//        System.out.println("writelock is going to release the lock");
//        lock.release();
//        
//    }
//    
//    /**
//     * Unlocks the writers lock
//     * if there are no waiting writer, broadcast to all waiting writer
//     * else give priority to writer
//     */
//    public void writeUnlock() {
//    	System.out.println("Before acquire in unlock\n");
//        lock.acquire();
//        System.out.println("writeUnLock is holding the lock now \n");
//        System.out.println("after acquire in unlock \n");
//        int temp;
//        if( WW > 0 ){
//        	System.out.println("In unlock, WW > 0\n");
//        	okToWrite.ConditionSignal();
//        	System.out.format("A signal has been send for waiting writer\n");}
//
//        else if(WR >0){
//        	temp = WR;
//        	System.out.println("In unlock, WW = 0, and wr >0\n");
//        	while(temp > 0){
//        		okToRead.ConditionSignal();
//        		temp--;
//        	}
//        }
//        System.out.println("In unlock, nothing matched\n");
//        AW--;
//        System.out.format("the lock will be released released, and AW is : %d\n", AW);
//        System.out.println("writeUnLock is going to release the lock \n");
//        lock.release();
//        
//        	
//        
//    }
//}
public class ReaderWriterLock {
    Lock lock;
    int AR,WR,AW,WW;
    Semaphore okToWrite, okToRead;
    public ReaderWriterLock() {
    	lock = new Lock();
    	okToWrite = new Semaphore(0);
    	okToRead = new Semaphore(0);
    	AR = 0;
    	WR = 0;
    	AW = 0;
    	WW = 0;
    }
    
    /**
     * Locks the readers lock
     * until ok to read
     */
    public void readLock() {
        lock.acquire();
        while(WW + AW > 0){
        	WR++;
//        	System.out.println("reader is going to sleep\n");
        	lock.release();
        	okToRead.p();
        	lock.acquire();
//        	System.out.println("reader woke up\n");
        	WR--;
        }
        AR++;
        lock.release();
        	
    }
    
    /**
     * Unlocks the readers lock
     * if there are no active reader and more that 0 waiting writer
     * signal to waiting writers
     */
    public void readUnlock() {
    	lock.acquire();
    	AR--;
    	if(AR == 0 && WW>0){
    		okToWrite.v();
    	}
        lock.release();
    }
    
    /**
     * Locks the writers lock
     * if there are active writer or active reader, block
     * else proceed
     */
    public void writeLock() {
        lock.acquire();
//        System.out.println("writeLock is holding the lock now \n");
//        System.out.println("out of while loop\n");
        while(AW +AR>0){
        	WW++;
//        	System.out.println("writer going to sleep\n");
        	lock.release();
        	okToWrite.p();
        	lock.acquire();
//        	System.out.println("writer woke up\n");
        	WW--;
        }
        AW++;
//        System.out.println("writelock is going to release the lock");
        lock.release();
        
    }
    
    /**
     * Unlocks the writers lock
     * if there are no waiting writer, broadcast to all waiting writer
     * else give priority to writer
     */
    public void writeUnlock() {
//    	System.out.println("Before acquire in unlock\n");
        lock.acquire();
//        System.out.println("writeUnLock is holding the lock now \n");
//        System.out.println("after acquire in unlock \n");
        int temp;
        if( WW > 0 ){
//        	System.out.println("In unlock, WW > 0\n");
        	okToWrite.v();
//        	System.out.format("A signal has been send for waiting writer\n");
        	}

        else if(WR >0){
        	temp = WR;
//        	System.out.println("In unlock, WW = 0, and wr >0\n");
        	while(temp > 0){
        		okToRead.v();
        		temp--;
        	}
        }
//        System.out.println("In unlock, nothing matched\n");
        AW--;
//        System.out.format("the lock will be released released, and AW is : %d\n", AW);
//        System.out.println("writeUnLock is going to release the lock \n");
        lock.release();
        
        	
        
    }
}
