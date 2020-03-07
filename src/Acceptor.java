import java.util.*;

public class Acceptor extends Process {
	BallotNumber ballot_number = null;
	Set<PValue> accepted = new HashSet<PValue>();
    int downMsgCount = 0;
    int downTime = 0;
    long pulse = 200;
//    long prevTime ;
    int curMsgCount = 0;
    public Boolean doSleep = false;
    AcceptorReplica replica;
    ProcessId[] leaders;
    int slot_num = 1;
    Double[] weights;
    long waitTime = 500;

    public Acceptor(Env env, ProcessId me, int dMsgCount, int dTime, ProcessId[] leaders, int numAcc, Double[] weights){
		this.env = env;
		this.me = me;
        this.downMsgCount = dMsgCount;
        this.downTime = dTime;
        this.leaders = leaders;

        this.weights = new Double[numAcc];
        for (int i = 0; i < numAcc ; i++){
            this.weights[i] = weights[i];
        }
        replica = new AcceptorReplica(env, new ProcessId("areplica:" + me.convToInt()),
                            leaders, slot_num, this.weights, accepted);
        env.addProc(me, this);
	}

/*
    Runnable hBeatSend = new Runnable() {
//        int k=0;
        public void run() {

            try {
                while(true) {
                    if (!doSleep){
                    // me is source
//                        k++;
//                        System.out.println(me+" Sent heartbeat"+k);
                    sendHeartbeat(me);
                    }
                    Thread.sleep(pulse);
                }
            }
            catch(Exception e) {
                System.out.println(e);
            }
        }
    };
*/

	public void body(){
        System.out.println("Here I am: " + me);
        for (;;) {
            if (curMsgCount == downMsgCount){
                try {
                    System.out.println(me + " Reached "+downMsgCount+" msgs, going to sleep for " + downTime/1000 + " secs !!!!!!!!");
                    doSleep = true;
                    replica.setDoSleep(true);
                    Thread.sleep(downTime);
                    doSleep = false;
                    replica.setDoSleep(false);
                    System.out.println(me + " Just woke up !!!!!!!!!!!!");
                } catch (InterruptedException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }

			PaxosMessage msg = getNextMessageBlking();

			if (msg instanceof P1aMessage) {
                curMsgCount++;
				P1aMessage m = (P1aMessage) msg;

                if (weightsEqual(weights,m.weights)){
                    if (ballot_number == null || ballot_number.compareTo(m.ballot_number) < 0) {
                        ballot_number = m.ballot_number;
                    }
                }
                else{

                }
                System.out.println(this.me + " promising audience with ballot number " + ballot_number);
                sendMessage(m.src, new P1bMessage(me, ballot_number, new HashSet<PValue>(accepted), weights));

			}
			else if (msg instanceof P2aMessage) {
                curMsgCount++;
				P2aMessage m = (P2aMessage) msg;

                if (weightsEqual(weights,m.weights)){
                    if (ballot_number == null || ballot_number.compareTo(m.ballot_number) < 0) {
                        ballot_number = m.ballot_number;
                    }
                }
                System.out.println(this.me + " accepting command with slot " + m.slot_number + " and  ballot " + ballot_number);
                sendMessage(m.src, new P2bMessage(me, ballot_number, m.slot_number, weights));
			}


        }
	}


    void enqueueHeartbeats(ProcessId src){
        //just because it's abstract in Process
    }
}
