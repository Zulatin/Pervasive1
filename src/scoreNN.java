import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class scoreNN {

	public static void main(String[] args) {
		
		String filepath;
		if(args.length==0) {
			filepath = "empiricalResults.txt";
		}
		else {
			filepath = args[0];
		}
		
		go(filepath);
		
	}
	
	public static void go(String filepath) {
		
		try {

			ArrayList<Double> error = new ArrayList<Double>();
			File f = new File(filepath);
			if (!f.exists()) return;
			BufferedReader in = new BufferedReader(new FileReader(f));
            String s;
            while ((s = in.readLine()) != null) {
                if (s.startsWith("True")) {
                	String[] data = s.split(" ");
                	Double d = Double.parseDouble(data[data.length-1]);
                	error.add(d);
                }
            }
            in.close();
            
            BufferedWriter out = new BufferedWriter(new FileWriter(new File("ScoreNN.txt")));
            
            Collections.sort(error);
            
            int size = error.size();
            
            System.out.println("Errors listed as % worse than this value");
            out.write("Errors listed as % worse than this value");
            out.newLine();
            
            // 5% worst
            int five = size - size/100*5;
            System.out.println("5% worst errors: "+error.get(five));
            out.write("5% worst errors: "+error.get(five));
            out.newLine();
            
            // 10% worst
            int ten = size - size/100*10;
            System.out.println("10% worst errors: "+error.get(ten));
            out.write("10% worst errors: "+error.get(ten));
            out.newLine();
            
            // 25% worst
            int twentyfive = size - size/100*25;
            System.out.println("25% worst errors: "+error.get(twentyfive));
            out.write("25% worst errors: "+error.get(twentyfive));
            out.newLine();
            
            // 50% worst
            int fifty = size - size/100*50;
            System.out.println("50% worst errors: "+error.get(fifty));
            out.write("50% worst errors: "+error.get(fifty));
            out.newLine();
            
            // 75% worst
            int seventyfive = size - size/100*75;
            System.out.println("75% worst errors: "+error.get(seventyfive));
            out.write("75% worst errors: "+error.get(seventyfive));
            out.newLine();
            
            // 95% worst
            int ninetyfive = size - size/100*95;
            System.out.println("95% worst errors: "+error.get(ninetyfive));
            out.write("95% worst errors: "+error.get(ninetyfive));
            out.newLine();
            
            out.close();
            
		}
		catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}