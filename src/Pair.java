import org.pi4.locutil.MACAddress;


public class Pair implements Comparable<Pair>{
	
	public MACAddress address;
	public double value;
	
	public Pair(MACAddress a, double v) {
		address = a;
		value = v;
	}
	
	public double getValue() {
		return value;
	}
	
	public MACAddress getAddress() {
		return address;
	}

	public int compareTo(Pair arg0) {
		return this.address.compareTo(arg0.getAddress());
	}
	
	public boolean equals(Object o) {
		Pair p = (Pair) o;
		return address.equals(p.getAddress());		
	}

}
