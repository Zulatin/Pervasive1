import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


public class Main {

	public static void main(String[] args) {
		
		try{
			
			System.out.println("Commands are:");
    		System.out.println("empirical_FP_NN");
    		System.out.println("empirical_FP_KNN <integer>");
    		System.out.println("model_FP_NN");
    		System.out.println("model_FP_KNN <integer>");
    		System.out.println("scoreNN <filepath>");
    		System.out.println("help");
    		
		    BufferedReader bufferReader = new BufferedReader(new InputStreamReader(System.in));
		    String s;
		    while(true) {
		    	s = bufferReader.readLine();
		    	String[] a = s.split(" ");
		    	String s2 = a[0];
		    	
	    		
		    	if(s2.toLowerCase().equals("empirical_fp_nn")) {
		    		empirical_FP_NN.go();
		    	}
		    	
		    	if(s2.toLowerCase().equals("empirical_fp_knn")) {
		    		empirical_FP_KNN.go(Integer.parseInt(a[1]));
		    	}
		    	
		    	if(s2.toLowerCase().equals("model_fp_nn")) {
		    		model_FP_NN.go();
		    	}
		    	
		    	if(s2.toLowerCase().equals("model_fp_knn")) {
		    		model_FP_KNN.go(Integer.parseInt(a[1]));
		    	}
		    	
		    	if(s2.toLowerCase().equals("scorenn")) {
		    		scoreNN.go((a[1]));
		    	}
		    	
		    	if(s2.toLowerCase().equals("help")) {
		    		System.out.println("Commands are:");
		    		System.out.println("empirical_FP_NN");
		    		System.out.println("empirical_FP_KNN <integer>");
		    		System.out.println("model_FP_NN");
		    		System.out.println("model_FP_KNN <integer>");
		    	}
		    }
		      
		    
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}

	}

}
