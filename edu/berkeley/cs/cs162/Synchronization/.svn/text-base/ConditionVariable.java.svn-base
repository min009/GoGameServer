package edu.berkeley.cs.cs162.Synchronization;

public class ConditionVariable {

	Semaphore s;
	Lock m;
	int waitingProcess;
	Lock MonitorLock;  //added new lock
	
	public ConditionVariable(Lock m){
		this.m=m;
		s=new Semaphore(0);
		waitingProcess=0;
		MonitorLock=new Lock();
	}
	
	public void ConditionWait(){
		MonitorLock.acquire();
		waitingProcess++;
		m.release();
		MonitorLock.release();
		s.p();
		MonitorLock.acquire();
		m.acquire();
		MonitorLock.release();
	}
	
	public void ConditionSignal(){
		MonitorLock.acquire();
		if(waitingProcess>0){
			waitingProcess--;
			s.v();
		}
		MonitorLock.release();
	}
}
