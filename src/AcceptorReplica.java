import java.util.*;

public class AcceptorReplica extends Process {
	ProcessId[] leaders;
	int slot_num = 1;
	Map<Integer /* slot number */, Command> proposals = new HashMap<Integer, Command>();
	Map<Integer /* slot number */, PValue> decisions = new HashMap<Integer, PValue>();
    Double[] weights;
    Set<PValue> acceptedSet ; //= new HashSet<PValue>();
    Boolean doSleep = false;
    long pulse = 500;

	public AcceptorReplica(Env env, ProcessId me, ProcessId[] leaders, int slot_num, Double[] weights,
                           Set<PValue> accepted){
		this.env = env;
		this.me = me;
		this.leaders = leaders;
        this.slot_num = slot_num;
        this.weights = weights;
        this.acceptedSet = accepted;
        //this.doSleep = doSleep;
		env.addProc(me, this);
	}

    public void setDoSleep(Boolean value){
        doSleep = value;
    }
	void propose(Command c){
		if (!decisionContains(c)) {
			for (int s = 1;; s++) {
                // never proposed this slot before and no decision has been made on this slot
            	if (!proposals.containsKey(s) && !decisions.containsKey(s)) {
					proposals.put(s, c);
					for (ProcessId ldr: leaders) {
					    sendMessage(ldr, new ProposeMessage(me, s, c.setOp("Operation "+ s +"x")));
					}
                    break;
				}
            }
		}
	}

	void perform(Command c){
		for (int s = 1; s < slot_num; s++) {
			if (c.equals(decisions.get(s).command)) {
                System.out.println("x:" + me + ": execute " + c.toString() + " slot_num "+slot_num);
				slot_num++;
				return;
			}
		}
        for (int sn: decisions.keySet()){
            acceptedSet.add(decisions.get(sn));
        }
		System.out.println("" + me + ": execute " + c.toString() + " slot_num "+slot_num);
		slot_num++;
	}

    boolean decisionContains(Command c){
        for (int sn : decisions.keySet()){
            if ((decisions.get(sn).command).equals(c)) {
                return true;
            }
        }
        return false;
    }

	public void body(){
		System.out.println("Here I am: " + me);
        PaxosMessage msg;
		for (;;) {
            // fixme : tina : see if this is really necessary
            if (!doSleep){
                //msg = getNextMessageBlking();
                msg = getNextMessageNonBlking();

                if (msg == null) {
                    try {
                        Thread.sleep(pulse);
                    } catch (InterruptedException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                    continue;
                }

                if (msg instanceof RequestMessage) {
                    RequestMessage m = (RequestMessage) msg;
                    propose(m.command);
                }

                else if (msg instanceof DecisionMessage) {
                    DecisionMessage m = (DecisionMessage) msg;
                    decisions.put(m.slot_number, new PValue(m.ballot_number, m.slot_number, m.command));
                    for (;;) {
                        PValue pv = decisions.get(slot_num);
                        if (pv == null)
                            break;
                        Command c = decisions.get(slot_num).command;
                        if (c == null) {
                            break;
                        }
                        Command c2 = proposals.get(slot_num);
                        if (c2 != null && !c2.equals(c)) {
                            propose(c2);
                        }
                        perform(c);
                    }
                }
                else {
                    System.err.println("Replica: unknown msg type");
                }
            }
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
	}

    void enqueueHeartbeats(ProcessId src){
        //just because it's abstract in Process
    }
}
