import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import org.pi4.locutil.GeoPosition;
import org.pi4.locutil.MACAddress;
import org.pi4.locutil.PositioningError;
import org.pi4.locutil.io.TraceGenerator;
import org.pi4.locutil.trace.Parser;
import org.pi4.locutil.trace.SignalStrengthSamples;
import org.pi4.locutil.trace.TraceEntry;


public class empirical_FP_KNN {
	
	private static int k = 3;
	
	public static void main(String[] args) {
		
		if(args.length != 0) {
			k = Integer.parseInt(args[0]);
		}
		
		go();
	}
	
	public static double go(int i) {
		k = i;
		return go();
	}
	
	public static double go() {
		
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
			

			ArrayList<RadioEntry> radiomap = new ArrayList<RadioEntry>();
			for(TraceEntry entry: offlineTrace) {
				SignalStrengthSamples sss = entry.getSignalStrengthSamples();
				LinkedList<MACAddress> addresses = sss.getSortedAccessPoints();
				ArrayList<Pair> temp = new ArrayList<Pair>();
				for(int i=0; i<addresses.size(); i++) {
					MACAddress a = addresses.get(i);
					double total = 0.0;
					Vector<Double> vec = sss.getSignalStrengthValues(a);
					for(Double d : vec) {
						total += d;															
					}
					double val = total / vec.size();
					
					Pair p = new Pair(a,val);
					
					temp.add(p);
				}
				
				RadioEntry radioEntry = new RadioEntry(entry.getGeoPosition());
				for(Pair p : temp) {
					radioEntry.add(p);
				}
				radiomap.add(radioEntry);			
			}
			
			/*
			 * Løb nu online positions igennem, og for hver position lav en
			 * nearest neighbour search. Skriv resultaterne ud til en fil.
			 */
			
			//System.out.println("Radiomap size = "+radiomap.size());
			
			File f = new File("empiricalResults.txt");
			FileOutputStream out = new FileOutputStream(f);
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));
			
			List<TraceEntry> onlineTrace = tg.getOnline();
			double totalError = 0.0;
			for(TraceEntry entry: onlineTrace) {
				
				GeoPosition pos = entry.getGeoPosition();
				SignalStrengthSamples sss = entry.getSignalStrengthSamples();
				ArrayList<Pair> help = new ArrayList<Pair>();
				LinkedList<MACAddress> addresses = sss.getSortedAccessPoints();
				// Opbyg liste af access points for denne position
				for(int i=0; i<addresses.size(); i++) {
					MACAddress a = addresses.get(i);
					//System.out.println(a.toString());
					Double signal = sss.getAverageSignalStrength(a);
					Pair p = new Pair(a,signal);
					help.add(p);
				}
				
				//System.out.println("Addresses = "+addresses.size());
				//System.out.println("Help = "+help.size());
				
				for(RadioEntry rEntry : radiomap) {
					ArrayList<Pair> signals = rEntry.get();
					Double total = 0.0;
					//System.out.println("Signals = "+signals.size());
					for(Pair p : signals) {
						//System.out.println(p.getAddress().toString());
						if(help.contains(p)) {
							//System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!");
							int i = help.indexOf(p);
							Pair p2 = help.get(i);
							double val = p2.getValue();
							if(val < -100) val = -100.0; 
							// (s1 - s2)^2
							total += Math.pow(val - p.getValue(),2);
						}
						else { // uden for rækkevidde, giv værdi
							total+= Math.pow(-100 - p.getValue(),2);
						}
					}
					double match = Math.sqrt(total);
					rEntry.setMatch(match);
				}
				
				Collections.sort(radiomap, new Comparator<RadioEntry>() {
					public int compare(RadioEntry r1, RadioEntry r2)
			        {
			            return  r1.compareTo(r2);
			        }
				});
				
				//System.out.println("--------------------------");
				//System.out.println(radiomap.get(0).match);
				//System.out.println(radiomap.get(1).match);
				//System.out.println(radiomap.get(radiomap.size()-1).match);
				
				RadioEntry rE = radiomap.get(0); // Best match
				System.out.println("Match = " + rE.getMatch());
				PositioningError error = new PositioningError(pos,rE.pos);
				Double dError = error.getPositioningError();
				totalError += dError;
				writer.write("True Position = "+pos.toString()+" Estimated Position = "+rE.pos.toString()+ " Error = "+ dError);
				writer.newLine();
			}
			double averageError = totalError / onlineTrace.size();
			writer.write("Average Error = "+ averageError);
			writer.close();
			out.close();
			System.out.println("Average Error = "+ averageError);
			
			return averageError;

		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return 0.0;
	}
	
	public static ArrayList<Double> test() {
		
		ArrayList<Double> results = new ArrayList<Double>();
		
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
			

			ArrayList<RadioEntry> radiomap = new ArrayList<RadioEntry>();
			for(TraceEntry entry: offlineTrace) {
				SignalStrengthSamples sss = entry.getSignalStrengthSamples();
				LinkedList<MACAddress> addresses = sss.getSortedAccessPoints();
				ArrayList<Pair> temp = new ArrayList<Pair>();
				for(int i=0; i<addresses.size(); i++) {
					MACAddress a = addresses.get(i);
					double total = 0.0;
					Vector<Double> vec = sss.getSignalStrengthValues(a);
					for(Double d : vec) {
						total += d;															
					}
					double val = total / vec.size();
					
					Pair p = new Pair(a,val);
					
					temp.add(p);
				}
				
				RadioEntry radioEntry = new RadioEntry(entry.getGeoPosition());
				for(Pair p : temp) {
					radioEntry.add(p);
				}
				radiomap.add(radioEntry);			
			}
			
			/*
			 * Løb nu online positions igennem, og for hver position lav en
			 * nearest neighbour search. Skriv resultaterne ud til en fil.
			 */
			
			//System.out.println("Radiomap size = "+radiomap.size());
			
			List<TraceEntry> onlineTrace = tg.getOnline();
			double totalError = 0.0;
			for(TraceEntry entry: onlineTrace) {
				
				GeoPosition pos = entry.getGeoPosition();
				SignalStrengthSamples sss = entry.getSignalStrengthSamples();
				ArrayList<Pair> help = new ArrayList<Pair>();
				LinkedList<MACAddress> addresses = sss.getSortedAccessPoints();
				// Opbyg liste af access points for denne position
				for(int i=0; i<addresses.size(); i++) {
					MACAddress a = addresses.get(i);
					//System.out.println(a.toString());
					Double signal = sss.getAverageSignalStrength(a);
					Pair p = new Pair(a,signal);
					help.add(p);
				}
				
				//System.out.println("Addresses = "+addresses.size());
				//System.out.println("Help = "+help.size());
				
				for(RadioEntry rEntry : radiomap) {
					ArrayList<Pair> signals = rEntry.get();
					Double total = 0.0;
					//System.out.println("Signals = "+signals.size());
					for(Pair p : signals) {
						//System.out.println(p.getAddress().toString());
						if(help.contains(p)) {
							//System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!");
							int i = help.indexOf(p);
							Pair p2 = help.get(i);
							double val = p2.getValue();
							if(val < -100) val = -100.0; 
							// (s1 - s2)^2
							total += Math.pow(val - p.getValue(),2);
						}
						else { // uden for rækkevidde, giv værdi
							total+= Math.pow(-100 - p.getValue(),2);
						}
					}
					double match = Math.sqrt(total);
					rEntry.setMatch(match);
				}
				
				Collections.sort(radiomap, new Comparator<RadioEntry>() {
					public int compare(RadioEntry r1, RadioEntry r2)
			        {
			            return  r1.compareTo(r2);
			        }
				});
				
				//System.out.println("--------------------------");
				//System.out.println(radiomap.get(0).match);
				//System.out.println(radiomap.get(1).match);
				//System.out.println(radiomap.get(radiomap.size()-1).match);
				
				double x = 0.0;
				double y = 0.0;
				for(int i = 0; i < k; i++) {
					RadioEntry rE = radiomap.get(i);
					GeoPosition position = rE.getPosition();
					x += position.getX();
					y += position.getY();
				}
				
				x = x / k;
				y = y / k;
				
				GeoPosition estimatedPosition = new GeoPosition(x,y);
				
				PositioningError error = new PositioningError(pos,estimatedPosition);
				Double dError = error.getPositioningError();
				results.add(dError);
				totalError += dError;

			}
			double averageError = totalError / onlineTrace.size();


			

		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return results;
	}
	
}

