import org.pi4.locutil.GeoPosition;
import org.pi4.locutil.MACAddress;


public class MACAddressPosition {
	
	public MACAddress address;
	public GeoPosition pos;
	
	public MACAddressPosition(double x, double y, MACAddress a) {
		address = a;
		pos = new GeoPosition(x,y);
	}
	
	public MACAddress getAddress() {
		return address;
	}
	
	public GeoPosition getPosition() {
		return pos;
	}

}
