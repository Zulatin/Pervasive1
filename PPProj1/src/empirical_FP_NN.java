import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import org.pi4.locutil.GeoPosition;
import org.pi4.locutil.MACAddress;
import org.pi4.locutil.io.TraceGenerator;
import org.pi4.locutil.trace.Parser;
import org.pi4.locutil.trace.SignalStrengthSamples;
import org.pi4.locutil.trace.TraceEntry;


public class empirical_FP_NN {
	
	public static void main(String[] args) {
	
		String offlinePath = "data/MU.1.5meters.offline.trace", onlinePath = "data/MU.1.5meters.online.trace";
		
		//Construct parsers
		File offlineFile = new File(offlinePath);
		Parser offlineParser = new Parser(offlineFile);
		System.out.println("Offline File: " +  offlineFile.getAbsoluteFile());
		
		File onlineFile = new File(onlinePath);
		Parser onlineParser = new Parser(onlineFile);
		System.out.println("Online File: " + onlineFile.getAbsoluteFile());
		
		System.out.println();
		//Construct trace generator
		TraceGenerator tg;
		try {
			int offlineSize = 25;
			int onlineSize = 5;
			tg = new TraceGenerator(offlineParser, onlineParser,offlineSize,onlineSize);
			
			//Generate traces from parsed files
			tg.generate();
			
			List<TraceEntry> offlineTrace = tg.getOffline();
			GeoPosition currentPosition = null;
			List<TraceEntry> temp = null;
			for(TraceEntry entry: offlineTrace) {
				if(!entry.getGeoPosition().equalsWithoutOrientation(currentPosition)) {
					if(temp != null) {
						ArrayList<tempHelper> help = new ArrayList<tempHelper>();
						for(TraceEntry entry2 : temp) {
							if(temp.size() == 0) {
								SignalStrengthSamples sss = entry2.getSignalStrengthSamples();
								LinkedList<MACAddress> addresses = sss.getSortedAccessPoints();
								boolean b = true;
								while(b) {
									MACAddress a = addresses.pop();
									if(a == null) {
										b = false;
									}
									else {
										Vector<Double> vec = sss.getSignalStrengthValues(a);
										Double total = 0.0;
										for(Double d : vec) {
											total += d;
										}
										Double result = total / vec.size();
										tempHelper tH = new tempHelper(a,result);
										if(help.contains(tH)) {
											int i = help.indexOf(tH);
											tempHelper t = help.get(i);
											t.add(result);
										}
										else {
											help.add(tH);
										}
										
									}
								}
							}
						}
						
						
						
						
						
					}
					
					
					temp = null;
					temp.add(entry);
					currentPosition = entry.getGeoPosition();
				}
				else {
					temp.add(entry);
				}
				
				//Print out coordinates for the collection point and the number of signal strength samples
				System.out.println(entry.getGeoPosition().toString() + " - " + entry.getSignalStrengthSamples().size());				
			}
			
			
			
			
			
			
			
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}
}

