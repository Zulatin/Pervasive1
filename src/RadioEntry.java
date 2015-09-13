import java.util.ArrayList;

import org.pi4.locutil.GeoPosition;
import org.pi4.locutil.MACAddress;


public class RadioEntry implements Comparable<RadioEntry> {

	public double match = 0;
	public GeoPosition pos;
	public ArrayList<Pair> signals = new ArrayList<Pair>();
	
	public RadioEntry(double x, double y) {
		pos = new GeoPosition(x,y);
	}
	
	public RadioEntry(GeoPosition g) {
		pos = g;
	}
	
	public void add(Pair p) {
		signals.add(p);
	}
	
	public ArrayList<Pair> get() {
		return signals;
	}
	
	public void setMatch(double d) {
		match = d;
	}
	
	public double getMatch() {
		return match;
	}

	public int compareTo(RadioEntry r) {
		if(this.match > r.match) return 1;
		if(this.match < r.match) return -1;
		return 0;
	}
}
