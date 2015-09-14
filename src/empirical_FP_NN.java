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
			
			/*
			 * Byg en liste af TraceEntry kaldet temp.
			 * Tilføj løbende TraceEntrys til temp.
			 * Når positionen skifter indeholder temp alle TraceEntry for den gamle position.
			 * Udfør arbejde på temp, hvorefter vi nulstiller.
			 */
			ArrayList<RadioEntry> radiomap = new ArrayList<RadioEntry>();
			ArrayList<TraceEntry> temp = new ArrayList<TraceEntry>();
			for(TraceEntry entry: offlineTrace) {
				if(!entry.getGeoPosition().equalsWithoutOrientation(currentPosition)) {
					if(temp.size() != 0) { // Positionen skiftede og temp er ikke tom, begynd arbejdet!
						ArrayList<TempHelper> help = new ArrayList<TempHelper>();
						for(TraceEntry entry2 : temp) {
							SignalStrengthSamples sss = entry2.getSignalStrengthSamples();
							LinkedList<MACAddress> addresses = sss.getSortedAccessPoints();
							for(int i=0; i<addresses.size(); i++) {
								MACAddress a = addresses.get(i);

								// Findes denne MACAdresse allerede?
								TempHelper tH = new TempHelper(a);
								if(help.contains(tH)) {
									int j = help.indexOf(tH);
									tH = help.get(j);
								}
								else {
									help.add(tH);
								}
								
								// Tilføj værdier fra sss til tempHelperen
								Vector<Double> vec = sss.getSignalStrengthValues(a);
								for(Double d : vec) {
									tH.add(d);	
																	
								}
							}
						
						}
						// Vi har nu kørt temp igennem og tilføjet til help.
						// Opbyg en radioentry for positionen.
						//System.out.println("help = "+help.size());
						RadioEntry radioEntry = new RadioEntry(currentPosition);
						for(TempHelper h : help) {
							Pair p = new Pair(h.getAddress(),h.getAverage());
							radioEntry.add(p);
						}
						radiomap.add(radioEntry);
						
					}
					temp = new ArrayList<TraceEntry>();
					temp.add(entry);
					currentPosition = entry.getGeoPosition();
				}
				else {
					
					// Positionen har ikke ændret sig, tilføj til temp
					temp.add(entry);
				}				
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
							// (s1 - s2)^2
							total += Math.pow(p2.getValue() - p.getValue(),2);
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
				writer.write("True Position = "+pos.toString()+" Estimated Position = "+rE.pos.toString()+ " Error = "+ dError);
				writer.newLine();
			}
			writer.close();
			out.close();

		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}

