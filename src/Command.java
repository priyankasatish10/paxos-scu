public class Command {
	ProcessId client;
	int req_id;
	Object op;

	public Command(ProcessId client, int req_id, String op){
		this.client = client;
		this.req_id = req_id;
		this.op = op;
	}

	public Command setOp(String op) {
		this.op = op;
		return this;
	}

	public boolean equals(Object o) {
		Command other = (Command) o;
		return client.equals(other.client) && req_id == other.req_id && op.equals(other.op);
	}

	public String toString(){
		return "Command(" + client + ", " + req_id + ", " + op + ")";
	}

    public Double[] getWeights(){
        Double[] op1 = (Double[])op;
        //double[] temp = new double[op1.length-1];
        /*for (int i=1; i< str.length; i++){
            temp[k++] = Double.parseDouble(str[i]);
        }*/
        return op1;
    }
}
