import java.util.*;

public class Commander extends Process {
	ProcessId leader;
	ProcessId[] acceptors, replicas;
	BallotNumber ballot_number;
	int slot_number;
	Command command;
    Double[] weights;

	public Commander(Env env, ProcessId me, ProcessId leader, ProcessId[] acceptors,
			ProcessId[] replicas, BallotNumber ballot_number, int slot_number, Command command, Double[] weights){
		this.env = env;
		this.me = me;
		this.acceptors = acceptors;
		this.replicas = replicas;
		this.leader = leader;
		this.ballot_number = ballot_number;
		this.slot_number = slot_number;
		this.command = command;
        this.weights = weights;
		env.addProc(me, this);
	}

	public void body(){
		P2aMessage m2 = new P2aMessage(me, ballot_number, slot_number, command, weights);
        boolean[] added = new boolean[acceptors.length];

        System.out.println(this.leader + " Proposing " + command + " to all acceptors for slot "+ slot_number + " and ballot "+ ballot_number);
        for (ProcessId a: acceptors) {
			sendMessage(a, m2);
		}

        float presentWeight = 0;

        while ( 2 * presentWeight <= acceptors.length) {
            PaxosMessage msg = getNextMessageBlking();

            if (msg instanceof P2bMessage) {
                P2bMessage m = (P2bMessage) msg;

                if (ballot_number.equals(m.ballot_number)) {
                    // ballot numbers are equal, we need to check if weights are equal
                    if (!weightsEqual(weights, m.weights)){
                        sendMessage(leader, new PreemptedMessage(me, m.ballot_number, m.weights));
                        return;
                    }
                    if (!added[m.src.convToInt()]) {
                        added[m.src.convToInt()] = true;
                        presentWeight += weights[m.src.convToInt()];
                    }
                }
                else {
                    sendMessage(leader, new PreemptedMessage(me, m.ballot_number, m.weights));
                    return;
                }
            }
            /*else if (msg instanceof AbortMessage){
                AbortMessage m = (AbortMessage) msg;
                sendMessage(leader, new PreemptedMessage(me, ballot_number, weights));
            }*/
            else {
                System.out.println("Weird kind of msg");
            }

        }

		for (ProcessId r: replicas) {
			sendMessage(r, new DecisionMessage(me, slot_number, command, ballot_number));
		}
	}

    void enqueueHeartbeats(ProcessId src){
        //just because it's abstract in Process
    }
}
