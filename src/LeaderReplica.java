import java.util.*;

public class LeaderReplica extends Process {
	ProcessId[] leaders;
	int slot_num = 1;
	Map<Integer /* slot number */, Command> proposals = new HashMap<Integer, Command>();
	Map<Integer /* slot number */, PValue> decisions = new HashMap<Integer, PValue>();
    Double[] weights;

	public LeaderReplica(Env env, ProcessId me, ProcessId[] leaders, int slot_num, Double[] weights){
		this.env = env;
		this.me = me;
		this.leaders = leaders;
        this.slot_num = slot_num;
        this.weights = weights;
		env.addProc(me, this);
	}

	void propose(Command c){
		if (!decisionContains(c)) {
			for (int s = 1;; s++) {
               if (!proposals.containsKey(s) && !decisions.containsKey(s)) {
					proposals.put(s, c);
					for (ProcessId ldr: leaders) {
						sendMessage(ldr, new ProposeMessage(me, s, c));
					}
                    break;
               }
			}
		}
	}

	void perform(Command c){
		for (int s = 1; s < slot_num; s++) {
			if (c.equals(decisions.get(s).command)) {
                System.out.println("" + me + ": execute " + c.toString() + " slot_num "+slot_num);
				slot_num++;
				return;
			}
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
		for (;;) {
			PaxosMessage msg = getNextMessageBlking();

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
					Command c = pv.command;
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
	}

    void enqueueHeartbeats(ProcessId src){
        //just because it's abstract in Process
    }

    /*public int getSlot_num(){
        return slot_num;
    }*/

}
