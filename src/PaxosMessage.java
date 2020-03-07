import java.util.*;

public class PaxosMessage {
	ProcessId src;
}

class P1aMessage extends PaxosMessage {
	BallotNumber ballot_number;
    Double[] weights;
	P1aMessage(ProcessId src, BallotNumber ballot_number, Double[] weights){
		this.src = src; this.ballot_number = ballot_number; this.weights = weights;
}	}
class P1bMessage extends PaxosMessage {
	BallotNumber ballot_number; Set<PValue> accepted;
    Double[] weights;
    P1bMessage(ProcessId src, BallotNumber ballot_number, Set<PValue> accepted, Double[] weights) {
		this.src = src; this.ballot_number = ballot_number; this.accepted = accepted; this.weights = weights;
}	}
class P2aMessage extends PaxosMessage {
	BallotNumber ballot_number; int slot_number; Command command;
    Double[] weights;
	P2aMessage(ProcessId src, BallotNumber ballot_number, int slot_number, Command command, Double[] weights){
		this.src = src; this.ballot_number = ballot_number;
		this.slot_number = slot_number; this.command = command;
        this.weights = weights;
}	}
class P2bMessage extends PaxosMessage {
	BallotNumber ballot_number; int slot_number;
    Double[] weights;
	P2bMessage(ProcessId src, BallotNumber ballot_number, int slot_number, Double[] weights){
		this.src = src; this.ballot_number = ballot_number; this.slot_number = slot_number;
        this.weights = weights;
}	}
class PreemptedMessage extends PaxosMessage {
	BallotNumber ballot_number;
    Double[] weights;
	PreemptedMessage(ProcessId src, BallotNumber ballot_number, Double[] weights){
		this.src = src; this.ballot_number = ballot_number;
        this.weights = weights;
}	}
class AdoptedMessage extends PaxosMessage {
	BallotNumber ballot_number; Set<PValue> accepted;
	AdoptedMessage(ProcessId src, BallotNumber ballot_number, Set<PValue> accepted){
		this.src = src; this.ballot_number = ballot_number; this.accepted = accepted;
}	}
class DecisionMessage extends PaxosMessage {
	ProcessId src; int slot_number; Command command; BallotNumber ballot_number;
	public DecisionMessage(ProcessId src, int slot_number, Command command, BallotNumber ballot_number){
		this.src = src; this.slot_number = slot_number; this.command = command; this.ballot_number = ballot_number;
}	}
class RequestMessage extends PaxosMessage {
	Command command;
	public RequestMessage(ProcessId src, Command command){
		this.src = src; this.command = command;
}	}
class ProposeMessage extends PaxosMessage {
	int slot_number; Command command;
	public ProposeMessage(ProcessId src, int slot_number, Command command){
		this.src = src; this.slot_number = slot_number; this.command = command;
}	}
class WeightsChangedMessage extends PaxosMessage {
    ProcessId src;             Double[] weights;
    WeightsChangedMessage(ProcessId src, Double[] weights){
        this.src = src;
        this.weights = weights;
    }    }
class AbortMessage extends PaxosMessage {
    ProcessId src;
    AbortMessage(ProcessId src){
        this.src = src;
    }    }
