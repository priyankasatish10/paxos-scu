import java.util.*;

public class Scout extends Process {
	ProcessId leader;
	ProcessId[] acceptors;
	BallotNumber ballot_number;
    Double[] weights;

	public Scout(Env env, ProcessId me, ProcessId leader,
			ProcessId[] acceptors, BallotNumber ballot_number, Double[] weights){
		this.env = env;
		this.me = me;
		this.acceptors = acceptors;
		this.leader = leader;
		this.ballot_number = ballot_number;
        this.weights = weights;
		env.addProc(me, this);
	}

	public void body(){
		P1aMessage m1 = new P1aMessage(me, ballot_number, weights);
        boolean[] added = new boolean[acceptors.length];

		System.out.println(leader + " sending What's up message to all acceptors with ballot " + ballot_number);
        for (ProcessId a: acceptors) {
			sendMessage(a, m1);
		}

        Double presentWeight = 0.0;
		Set<PValue> pvalues = new HashSet<PValue>();
        while ( 2 * presentWeight <= acceptors.length) {
			PaxosMessage msg = getNextMessageBlking();

            if (msg instanceof P1bMessage) {
				P1bMessage m = (P1bMessage) msg;

				int cmp = ballot_number.compareTo(m.ballot_number);
				if (cmp != 0) {
                    // the ballot numbers aren't equal. // preempted. send msg
                    sendMessage(leader, new PreemptedMessage(me, m.ballot_number, m.weights));
                    return;
				}
                // ballot numbers are equal, we need to check if weights are equal
                if (!weightsEqual(weights, m.weights)){
                    sendMessage(leader, new PreemptedMessage(me, m.ballot_number, m.weights));
                    return;
                }
                if (!added[m.src.convToInt()]) {
                    pvalues.addAll(m.accepted);
                    added[m.src.convToInt()] = true;
                    presentWeight += weights[m.src.convToInt()];
                }
			}
            /*
            else if (msg instanceof AbortMessage){
                AbortMessage m = (AbortMessage) msg;
                sendMessage(leader, new PreemptedMessage(me, ballot_number, weights));
            }*/

		}
		sendMessage(leader, new AdoptedMessage(me, ballot_number, pvalues));
	}

    void enqueueHeartbeats(ProcessId src){
        //just because it's abstract in Process
    }
}
