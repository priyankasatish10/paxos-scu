public abstract class Process extends Thread {
	ProcessId me;
	Queue<PaxosMessage> inbox = new Queue<PaxosMessage>();
	Env env;

	abstract void body();

	public void run(){
		body();
		env.removeProc(me);
	}

	PaxosMessage getNextMessageBlking(){
		return inbox.bdequeue();
	}

    PaxosMessage getNextMessageNonBlking(){
        return inbox.dequeue();
    }

	void sendMessage(ProcessId dst, PaxosMessage msg){
		env.sendMessage(dst, msg);
	}

    void sendMessage(Process dst, PaxosMessage msg){
        env.sendMessage(dst, msg);
    }

    void sendHeartbeat(ProcessId src){
        env.sendHeartbeat(src);
    }

	void deliver(PaxosMessage msg){
		inbox.enqueue(msg);
	}

    abstract void enqueueHeartbeats(ProcessId src);

    //return true if weights are equal
    boolean weightsEqual(Double[] wt1, Double[] wt2){
        if (wt1.length != wt2.length)
            return false;
        for (int i = 0; i< wt1.length ; i++){
            if (wt1[i] != wt2[i])
                return false;
        }
        return true;
    }

}
