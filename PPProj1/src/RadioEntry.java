import java.util.ArrayList;

import org.pi4.locutil.MACAddress;


public class RadioEntry {

	public MACAddress address;
	public double value;
	
	public RadioEntry(MACAddress a, double v) {
		address = a;
		value = v;
	}
	
	
	
	
	/*
	public ArrayList<Pair> list = new ArrayList<Pair>();
	public void add(Pair p) {
		list.add(p);
	}
	
	public ArrayList<Pair> getList() {
		return list;
	}*/
}
