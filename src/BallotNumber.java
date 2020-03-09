public class BallotNumber implements Comparable {
	int round;
	ProcessId _id;

	public BallotNumber(int round, ProcessId _id){
		this.round = round;
		this._id = _id;
	}

	public boolean equals(Object other){
		return compareTo(other) == 0;
	}

	public int compareTo(Object other){
		BallotNumber bn = (BallotNumber) other;
		if (bn.round != round) {
			return round - bn.round;
		}
		return _id.compareTo(bn._id);
	}

	public String toString(){
		return "BN(" + round + ", " + _id + ")";
	}
}
