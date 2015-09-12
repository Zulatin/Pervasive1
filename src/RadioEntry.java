import java.util.ArrayList;

import org.pi4.locutil.GeoPosition;
import org.pi4.locutil.MACAddress;


public class RadioEntry {

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
	
}
