import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
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


public class model_FP_NN {

	public static void main(String[] args) {
		
		go();
		
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
			
			
			/*
			 * Indl�s filen med AP positions.
			 * Process den til objekter der parrer macaddresser og positioner.
			 * Opbyg et array af disse objekter.
			 */
			ArrayList<MACAddressPosition> MAClist = new ArrayList<MACAddressPosition>();			
			BufferedReader in = new BufferedReader(new FileReader(new File("data/MU.AP.positions")));
            String s;
            boolean b = true;
            while ((s = in.readLine()) != null) {
                if (b) { // Skip f�rste linie i filen
                	b = false;
                	continue;
                }
                String[] data = s.split(" ");
                MACAddress a = MACAddress.parse(data[0]);
                double x = Double.parseDouble(data[1]);
                double y = Double.parseDouble(data[2]);
                
                MACAddressPosition map = new MACAddressPosition(x, y, a);
                MAClist.add(map);
            }
            in.close();
			
            /*
             * Opbyg et array af size*size positioner hvor vi udregner signalstyrkerne
             * for hver AP p� hver position. L�g disse udregninger ind i arrayet.
             */
            int size = 120;
            double n = 3.415;
            double Pd0 = -45;
            ArrayList<RadioEntry> modelMap = new ArrayList<RadioEntry>();
            for(int i = -size; i<=size; i++) {
            	for(int j = -size; j<=size; j++) {
            		GeoPosition pos = new GeoPosition(i,j);
            		RadioEntry rE = new RadioEntry(pos);
            		for(MACAddressPosition map : MAClist) {
            			MACAddress address = map.getAddress();
            			GeoPosition macPos = map.getPosition();
            			double d = pos.distance(macPos);
            			
            			double signal = Pd0 - 10 * n * Math.log10(d);
            			
            			Pair p = new Pair(address,signal);
            			rE.add(p);
            		}
            		modelMap.add(rE);
            	}
            }
            
            File f = new File("modelResults.txt");
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
				
				for(RadioEntry rEntry : modelMap) {
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
						else { // uden for r�kkevidde, giv v�rdi
							total+= Math.pow(-100 - p.getValue(),2);
						}
					}
					double match = Math.sqrt(total);
					rEntry.setMatch(match);
				}
				
				Collections.sort(modelMap, new Comparator<RadioEntry>() {
					public int compare(RadioEntry r1, RadioEntry r2)
			        {
			            return  r1.compareTo(r2);
			        }
				});
				
				//System.out.println("--------------------------");
				//System.out.println(radiomap.get(0).match);
				//System.out.println(radiomap.get(1).match);
				//System.out.println(radiomap.get(radiomap.size()-1).match);
				
				RadioEntry rE = modelMap.get(0); // Best match
				//System.out.println("Match = " + rE.getMatch());
				PositioningError error = new PositioningError(pos,rE.pos);
				Double dError = error.getPositioningError();
				totalError += dError;
				System.out.println("Error = "+dError);
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
			
			
			/*
			 * Indl�s filen med AP positions.
			 * Process den til objekter der parrer macaddresser og positioner.
			 * Opbyg et array af disse objekter.
			 */
			ArrayList<MACAddressPosition> MAClist = new ArrayList<MACAddressPosition>();			
			BufferedReader in = new BufferedReader(new FileReader(new File("data/MU.AP.positions")));
            String s;
            boolean b = true;
            while ((s = in.readLine()) != null) {
                if (b) { // Skip f�rste linie i filen
                	b = false;
                	continue;
                }
                String[] data = s.split(" ");
                MACAddress a = MACAddress.parse(data[0]);
                double x = Double.parseDouble(data[1]);
                double y = Double.parseDouble(data[2]);
                
                MACAddressPosition map = new MACAddressPosition(x, y, a);
                MAClist.add(map);
            }
            in.close();
			
            /*
             * Opbyg et array af size*size positioner hvor vi udregner signalstyrkerne
             * for hver AP p� hver position. L�g disse udregninger ind i arrayet.
             */
            int size = 120;
            double n = 3.415;
            double Pd0 = -45;
            ArrayList<RadioEntry> modelMap = new ArrayList<RadioEntry>();
            for(int i = -size; i<=size; i++) {
            	for(int j = -size; j<=size; j++) {
            		GeoPosition pos = new GeoPosition(i,j);
            		RadioEntry rE = new RadioEntry(pos);
            		for(MACAddressPosition map : MAClist) {
            			MACAddress address = map.getAddress();
            			GeoPosition macPos = map.getPosition();
            			double d = pos.distance(macPos);
            			
            			double signal = Pd0 - 10 * n * Math.log10(d);
            			
            			Pair p = new Pair(address,signal);
            			rE.add(p);
            		}
            		modelMap.add(rE);
            	}
            }
			
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
				
				for(RadioEntry rEntry : modelMap) {
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
						else { // uden for r�kkevidde, giv v�rdi
							total+= Math.pow(-100 - p.getValue(),2);
						}
					}
					double match = Math.sqrt(total);
					rEntry.setMatch(match);
				}
				
				Collections.sort(modelMap, new Comparator<RadioEntry>() {
					public int compare(RadioEntry r1, RadioEntry r2)
			        {
			            return  r1.compareTo(r2);
			        }
				});
				
				//System.out.println("--------------------------");
				//System.out.println(radiomap.get(0).match);
				//System.out.println(radiomap.get(1).match);
				//System.out.println(radiomap.get(radiomap.size()-1).match);
				
				RadioEntry rE = modelMap.get(0); // Best match
				//System.out.println("Match = " + rE.getMatch());
				PositioningError error = new PositioningError(pos,rE.pos);
				Double dError = error.getPositioningError();
				totalError += dError;
				results.add(dError);
				System.out.println("Error = "+dError);

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
