import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProcessId implements Comparable {
	String name;

	public ProcessId(String name){ this.name = name; }

	public boolean equals(Object other){
		return name.equals(((ProcessId) other).name);
	}

	public int compareTo(Object other){
		return name.compareTo(((ProcessId) other).name);
	}

    public int convToInt() {
        String[] s = this.name.split(":");
        return Integer.parseInt(s[1]);
    }

    public int getId() {
		String[] s = this.name.split(":");
		return Integer.parseInt(s[2]);
	}

	public String toString(){ return name; }

    public int hashCode(){
        return this.name.hashCode();

    }
}
