import java.util.ArrayList;

import org.pi4.locutil.MACAddress;


public class TempHelper implements Comparable<TempHelper> {
	
	public MACAddress address;
	ArrayList<Double> values = new ArrayList<Double>();
	
	public TempHelper(MACAddress a, double d) {
		address = a;
		values.add(d);
	}
	
	public TempHelper(MACAddress a) {
		address = a;
	}
	
	public void add(Double d) {
		values.add(d);
	}
	
	public MACAddress getAddress() {
		return address;
	}
	
	public Double getAverage() {
		Double total = 0.0;
		for(Double d : values) {
			total += d;
		}
		return total/values.size();
	}

	public int compareTo(TempHelper t) {
		int i = this.address.compareTo(t.address);
		return i;
	}
}
