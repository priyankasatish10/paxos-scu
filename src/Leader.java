import java.util.*;

public class Leader extends Process {
	ProcessId[] acceptors;
	ProcessId[] replicas;
	BallotNumber ballot_number;
	boolean active = false;
    long pulse = 510;
	Map<Integer, Command> proposals = new HashMap<Integer, Command>();
    int[] heartbeatMap;
    long[] pulseTimeMap;
    Double[] weights;
    Process commanderHandle, scoutHandle;
    Boolean done = false;
    int msgCount =0;
    Process replica;
    int slot_num = 1;
    int high = 400;
    int low = 100;
    int ver = 10;
	public Leader(Env env, ProcessId me, ProcessId[] acceptors,
										ProcessId[] replicas, Double[] weights, ProcessId[] leaders){
		this.env = env;
		this.me = me;
		ballot_number = new BallotNumber(0, me);
		this.acceptors = acceptors;
		this.replicas = replicas;
        this.heartbeatMap = new int[acceptors.length];
        this.pulseTimeMap = new long[acceptors.length];
        this.weights = new Double[acceptors.length];
        for (ProcessId a: acceptors) {
            this.heartbeatMap[a.convToInt()] = 0;
            this.weights[a.convToInt()] = weights[a.convToInt()];
        }
        if (!done){
		    env.addProc(me, this);
            done = true;
        }
        replica = new LeaderReplica(env, new ProcessId("pReplica:" + me.convToInt()), leaders, slot_num, this.weights);
	}

	public void body(){
		System.out.println("Here I am: " + me);

		scoutHandle = new Scout(env, new ProcessId("scout:" + me + ":" + ballot_number),
			me, acceptors, ballot_number, weights);
		for (;;) {
			PaxosMessage msg = getNextMessageBlking();
            //msgCount++;
            //System.out.println(me + " dequeued this msg :"+msg + " , my msg cnt :" + msgCount);

			if (msg instanceof ProposeMessage) {
                msgCount++;
				ProposeMessage m = (ProposeMessage) msg;
				if (!proposals.containsKey(m.slot_number)) {
					System.out.println(me+ " received client request with slot number " + m.slot_number + " and " + m.command);
					proposals.put(m.slot_number, m.command);

					if (active) {
						commanderHandle = new Commander(env,
							new ProcessId("commander:" + me + ":" + ballot_number + ":" + m.slot_number),
							me, acceptors, replicas, ballot_number, m.slot_number, m.command, weights);
					}
				}
			}

			else if (msg instanceof AdoptedMessage) {
                //msgCount++;
				AdoptedMessage m = (AdoptedMessage) msg;
               // System.out.println(me+ " got adopted msg " + m.ballot_number );
				if (ballot_number.equals(m.ballot_number)) {
					Map<Integer, BallotNumber> max = new HashMap<Integer, BallotNumber>();
					for (PValue pv : m.accepted) {
						BallotNumber bn = max.get(pv.slot_number);
						if (bn == null || bn.compareTo(pv.ballot_number) < 0) {
							max.put(pv.slot_number, pv.ballot_number);
							proposals.put(pv.slot_number, pv.command);
						}
					}

					for (int sn : proposals.keySet()) {
						commanderHandle = new Commander(env,
							new ProcessId("commander:" + me + ":" + ballot_number + ":" + sn),
							me, acceptors, replicas, ballot_number, sn, proposals.get(sn), weights);
					}
					active = true;
				}
			}

			else if (msg instanceof PreemptedMessage) {
				PreemptedMessage m = (PreemptedMessage) msg;
              //  System.out.println(me+ " got preempted msg " + msg );
				if (ballot_number.compareTo(m.ballot_number) < 0) {
					ballot_number = new BallotNumber(m.ballot_number.round + 1, me);
					System.out.println(me + " received preemption!!! Incrementing ballot " + ballot_number);
				}
                else if (ballot_number.compareTo(m.ballot_number) == 0) {
                    // the weights must be unequal. In this case do not change the ballot number, resend scout
                    try {
                        Random rand = new Random();
                        int random_int = rand.nextInt(high - low) + low;
                        Thread.sleep(random_int);  // wait for acceptor to update weights
                    } catch (InterruptedException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                }
                scoutHandle = new Scout(env, new ProcessId("scout:" + me + ":" + ballot_number),
                        me, acceptors, ballot_number, weights);
                active = false;
			}

			else {
				System.err.println("Leader: unknown msg type");
			}
		}
	}

    void enqueueHeartbeats(ProcessId src){
        heartbeatMap[src.convToInt()] = 1;
    }

}
