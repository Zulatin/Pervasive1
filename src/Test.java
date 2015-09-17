import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
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


public class Test {

	public static void main(String[] args) {

//		test1();
//		test2();
		test3();
	}

	public static void test1() {
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

			File f = new File("plot1.txt");
			FileOutputStream out = new FileOutputStream(f);
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));

			for(RadioEntry rE : radiomap) {
				ArrayList<Pair> pairList = rE.get();
				for(Pair p : pairList) {
					GeoPosition pos = rE.getPosition();
					MACAddressPosition map = new MACAddressPosition(0, 0, p.getAddress());
					if(MAClist.contains(map)) {
						int i = MAClist.indexOf(map);
						MACAddressPosition newMap = MAClist.get(i);
						double signalStrength = p.getValue();
						MACAddress mac = newMap.getAddress();
						GeoPosition macPos = newMap.getPosition();
						double distance = pos.distance(macPos);

						writer.write(mac.toString()+", "+signalStrength+", "+distance);
						writer.newLine();
					}
				}
			}

			writer.close();
			out.close();


		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void test2() {

	}

	public static void test3() {
		ArrayList<Double> empirical = new ArrayList<>();
		ArrayList<Double> model = new ArrayList<>();


		for (int i=1; i<=5; i++){
			double eTotal = 0;
			double mTotal = 0;
			for (int j=0; j<100; j++){
				empirical.add(empirical_FP_KNN.goClean(i));
				model.add(model_FP_KNN.goClean(i));
				eTotal += empirical.get(j);
				mTotal += model.get(j);
				System.out.println("i:" + i + ", j:" + j);
			}
			
			try {
				BufferedWriter writerEmpirical = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File("empiricalAccuracy" + i + ".txt"))));
				BufferedWriter writerModel = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File("modelAccuracy" + i + ".txt"))));
				
				writerEmpirical.write(i + ", " + eTotal / empirical.size() + "\n");
				writerModel.write(i + ", " + mTotal / model.size() + "\n");
				
				writerEmpirical.close();
				writerModel.close();
				
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
