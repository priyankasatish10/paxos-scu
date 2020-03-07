import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;

public class Env {
	Map<ProcessId, Process> procs = new HashMap<ProcessId, Process>();
	public static int nAcceptors, nReplicas, nLeaders, nRequests;
    public static Double[] weights;
    public static int[] downNumMsgs;
    public static int[] downTime;
    public Integer debug = 0;
    ProcessId[] acceptors;
    ProcessId[] replicas;
    ProcessId[] leaders;
    //Map<ProcessId, Double> weight = new HashMap<ProcessId, Double>();
    boolean ready = false;

    synchronized void sendMessage(ProcessId dst, PaxosMessage msg){
		Process p = procs.get(dst);
		if (p != null) {
			p.deliver(msg);
		}
	}

    synchronized void sendMessage(Process p, PaxosMessage msg){
        if (p != null){
            p.deliver(msg);
        }
    }

    synchronized void sendHeartbeat(ProcessId src){
        // heartbeat sent to all leaders
        // fixme how to make static variable like in c++?
        if (ready){
            for (ProcessId leaderId: leaders)
            {
                Process p = procs.get(leaderId);
                p.enqueueHeartbeats(src);
            }
        }
    }

	synchronized void addProc(ProcessId pid, Process proc){
		procs.put(pid, proc);
		proc.start();
	}

	synchronized void removeProc(ProcessId pid){
		procs.remove(pid);
	}

    public void debugPrint(ProcessId me, String p) {
        if (debug == 1)
        System.out.println("DEBUG:" + me + " --> " + p);
    }

	void run(String[] args){

        int k =0;
		for (int i = 0; i < nAcceptors; i++) {
			acceptors[i] = new ProcessId("acceptor:" + i);
            replicas[k++] = new ProcessId("areplica:" + i);
			Acceptor acc = new Acceptor(this, acceptors[i],downNumMsgs[i],downTime[i],leaders, nAcceptors, weights);
		}
        /*
		for (int i = 0; i < nReplicas; i++) {
			Replica repl = new Replica(this, replicas[i], leaders);
		} */
		for (int i = 0; i < nLeaders; i++) {
			leaders[i] = new ProcessId("leader:" + i);
            replicas[k++] = new ProcessId("lreplica:"+i);
			Leader leader = new Leader(this, leaders[i], acceptors, replicas, weights, leaders);
		}
        ready = true;
        for (int i = 1; i <= nRequests; i++) {
			ProcessId pid = new ProcessId("client:" + i);
			for (int r = 0; r < nReplicas; r++) {
				sendMessage(replicas[r],
					new RequestMessage(pid, new Command(pid, 0, "operation " +i)));
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
		}
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
	   /*	
        ProcessId pid1 = new ProcessId("client:" + 1);
        ProcessId pid2 = new ProcessId("client:" + 2);
        ProcessId pid3 = new ProcessId("client:" + 3);
        ProcessId pid4 = new ProcessId("client:" + 4);
        ProcessId pid5 = new ProcessId("client:" + 5);
        sendMessage(replicas[0],
        	new RequestMessage(pid1, new Command(pid1, 0, "operation " +3)));
        sendMessage(replicas[1],
        	new RequestMessage(pid2, new Command(pid2, 0, "operation " +5)));
        sendMessage(replicas[1],
        	new RequestMessage(pid5, new Command(pid5, 0, "operation " +1)));
        sendMessage(replicas[1],
        	new RequestMessage(pid4, new Command(pid4, 0, "operation " +2)));
        sendMessage(replicas[2],
        	new RequestMessage(pid3, new Command(pid3, 0, "operation " +4)));
        sendMessage(replicas[2],
                new RequestMessage(pid5, new Command(pid5, 0, "operation " +6)));
        sendMessage(replicas[2],
                new RequestMessage(pid1, new Command(pid1, 0, "operation " +7)));
        sendMessage(replicas[2],
                new RequestMessage(pid2, new Command(pid2, 0, "operation " +8)));
        sendMessage(replicas[2],
                new RequestMessage(pid3, new Command(pid3, 0, "operation " +9)));
*/	}

    void fetch_inputs(String[] args) {
        if (args.length != 1) {
            System.out.println("input file missing");
            System.exit(-1);
        }

        try {
            String sCurrentLine;
            int index = 0;
            BufferedReader br = new BufferedReader(new FileReader(args[0]));
            while ((sCurrentLine = br.readLine()) != null) {
                if (sCurrentLine.charAt(0) == ';') continue;
                switch (index) {
                    case 0:
                        String[] lineOne = sCurrentLine.split(",");
                        if (lineOne.length < 4) {
                            System.out.println("ERROR : you missed one of your processors altogether");
                            System.exit(-1);
                        }
                        //nReplicas = Integer.parseInt(lineOne[0].trim());
                        nAcceptors  = Integer.parseInt(lineOne[0].trim());
                        nLeaders   = Integer.parseInt(lineOne[1].trim());
                        nRequests  = Integer.parseInt(lineOne[2].trim());
                        weights    = new Double[nAcceptors];
                        nReplicas = nAcceptors + nLeaders;
                        if (lineOne.length == 4) {
                            String[] weigh = lineOne[3].split(":");
                            double sum = 0;
                            for (int i = 0; i < weigh.length; i++) {
                                weights[i] = Double.parseDouble(weigh[i].trim());
                                sum += weights[i];
                            }
                            if (sum != (double)nAcceptors) {
                                System.out.println("ERROR : weights don't add up, learn math!");
                                System.exit(-1);
                            }
                        }
                        else {
                            for (int i = 0; i < weights.length; i++) {
                                weights[i] = 1.0;
                            }
                        }
                        downTime        = new int[nAcceptors];
                        acceptors = new ProcessId[nAcceptors];
                        replicas = new ProcessId[nReplicas];
                        leaders = new ProcessId[nLeaders];
                        downNumMsgs     = new int[nAcceptors];
                        for (int i =0; i<nAcceptors; i++)
                            downNumMsgs[i]= -1;
                        break;

                    case 1:
                        debug = Integer.parseInt(sCurrentLine.trim());
                        break;

                    default:    // All lines for downtime fall here.
                        String[] lineTwo = sCurrentLine.split(",");
                        int downAcceptorId             = Integer.parseInt(lineTwo[0].trim());
                        downNumMsgs[downAcceptorId]    = Integer.parseInt(lineTwo[1].trim());
                        downTime[downAcceptorId]       = Integer.parseInt(lineTwo[2].trim());
                        break;
                }
                index++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    //parsing complete
    System.out.println("<br> Config ****************************** ");
    System.out.println("<br> Num Replicas  : "+nReplicas);
    System.out.println("<br> Num Leaders   : "+nLeaders);
    System.out.println("<br> Num Acceptors : "+nAcceptors);
    System.out.println("<br> Num Requests  : "+nRequests);
    //System.out.print("<br> Init Weights  : ");
    //for (int i =0; i<nAcceptors; i++)
        //System.out.print( weights[i] + " ");
    System.out.println("");
    for (int i =0; i<nAcceptors; i++){
        if (downTime[i] == 0) continue;
        System.out.println("<br> Acceptor : "+i+", Msgs to DownTime : " +downNumMsgs[i]+", DownTime : "+downTime[i]/1000  +"s");
    }
    System.out.println("<br> ************************************ ");

    }

	public static void main(String[] args){
        	Env Environ = new Env();
        	Environ.fetch_inputs(args);
		    Environ.run(args);
	}
}
