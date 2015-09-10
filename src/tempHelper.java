import java.util.ArrayList;

import org.pi4.locutil.MACAddress;


public class tempHelper implements Comparable<tempHelper> {
	
	public MACAddress address;
	ArrayList<Double> values = new ArrayList<Double>();
	
	public tempHelper(MACAddress a, double d) {
		address = a;
		values.add(d);
	}
	
	public void add(Double d) {
		values.add(d);
	}
	
	public Double getAverage() {
		Double total = 0.0;
		for(Double d : values) {
			total += d;
		}
		return total/values.size();
	}

	public int compareTo(tempHelper t) {
		int i = this.address.compareTo(t.address);
		return i;
	}
}
