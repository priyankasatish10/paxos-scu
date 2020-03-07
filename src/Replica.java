import java.util.*;

public class Replica extends Process {
	ProcessId[] leaders;
	int slot_num = 1;
	Map<Integer /* slot number */, Command> proposals = new HashMap<Integer, Command>();
	Map<Integer /* slot number */, Command> decisions = new HashMap<Integer, Command>();
    Double[] weights;
    LinkedList<Command> ReqMsgQ = new LinkedList<Command>();

	public Replica(Env env, ProcessId me, ProcessId[] leaders, int slot_num, Double[] weights){
		this.env = env;
		this.me = me;
		this.leaders = leaders;
        this.slot_num = slot_num;
        this.weights = weights;
		env.addProc(me, this);
	}

	void propose(Command c, int s, boolean retry){
		if (!decisions.containsValue(c)) {
			//for (int s = 1;; s++) {
                // fixme : tina : check if this is correct
                // never proposed this slot before and no decision has been made on this slot
            if (retry) {
            	if (!proposals.containsKey(s) && !decisions.containsKey(s)) {
					proposals.put(s, c);
					for (ProcessId ldr: leaders) {
						sendMessage(ldr, new ProposeMessage(me, s, c));
					}
				}
                else {
                    //should not be an else on a retry

                }
            } else {
                //proposing because of incoming command
                if (!proposals.containsKey(s) && !decisions.containsKey(s)) {
                    proposals.put(s, c);
                    ReqMsgQ.remove(); // this command has been proposed
                    for (ProcessId ldr: leaders) {
                        sendMessage(ldr, new ProposeMessage(me, s, c));
                    }
                }
                // else it remains in the queue till this condition satisfies
            }
			//}
		}
	}

	void perform(Command c){
		for (int s = 1; s < slot_num; s++) {
			if (c.equals(decisions.get(s))) {
				slot_num++;
				return;
			}
		}
		System.out.println("" + me + ": execute " + c.toString());
		slot_num++;
	}

	public void body(){
		System.out.println("Here I am: " + me);
		for (;;) {
			PaxosMessage msg = getNextMessageBlking();

			if (msg instanceof RequestMessage) {
				RequestMessage m = (RequestMessage) msg;
                ReqMsgQ.add(m.command);
				propose(ReqMsgQ.peek(), slot_num, false);
			}

			else if (msg instanceof DecisionMessage) {
				DecisionMessage m = (DecisionMessage) msg;
				decisions.put(m.slot_number, m.command);
				for (;;) {
					Command c = decisions.get(slot_num);
					if (c == null) {
						break;
					}
                    // some decision has been reached for slot_num
                    // check if it was your command
                    // if not, propose your command again
					Command c2 = proposals.get(slot_num);
					if (c2 != null && !c2.equals(c)) {
                        // propose at the next slot number because perform(c) immediately after
                        // this instruction will increment slot_num.
						propose(c2, slot_num+1, true);
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

    public int getSlot_num(){
        return slot_num;
    }

}
