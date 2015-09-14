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
            int size = 30;
            double n = 3.415;
            double c = -33.77;
            ArrayList<RadioEntry> modelMap = new ArrayList<RadioEntry>();
            for(int i = 0; i<=size; i++) {
            	for(int j = 0; j<=size; j++) {
            		GeoPosition pos = new GeoPosition(i,j);
            		RadioEntry rE = new RadioEntry(pos);
            		for(MACAddressPosition map : MAClist) {
            			MACAddress address = map.getAddress();
            			GeoPosition macPos = map.getPosition();
            			double d = pos.distance(macPos);
            			
            			double signal = 10 * n * Math.log10(d) + c;
            			
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
							// (s1 - s2)^2
							total += Math.pow(p2.getValue() - p.getValue(),2);
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
