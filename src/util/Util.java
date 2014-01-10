package util;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import model.SamplerState;
import model.SamplerStateTracker;

public class Util {

	/**
	 * Samples from a discrete distribution. The input is a list of probabilites (non negative and non zero)
	 * They need not sum to 1, the list will be normalized. 
	 * @param probs
	 * @return
	 */
	public static int sample(List<Double> probs)
	{
		ArrayList<Double> cumulative_probs = new ArrayList<Double>();
		double sum_probs = 0;
		for(double prob:probs)
		{
			sum_probs = sum_probs + prob;
			cumulative_probs.add(sum_probs);
		}
		if(sum_probs!=1)		//normalizing
			for(int i=0;i<probs.size();i++)
			{
				probs.set(i, probs.get(i)/sum_probs);
				cumulative_probs.set(i, cumulative_probs.get(i)/sum_probs);
			}
		Random r  = new Random();
		double nextRandom = r.nextDouble();
		for(int i=0;i<cumulative_probs.size();i++)		
			if(cumulative_probs.get(i)>nextRandom)			
				return i;
		
		return -1;		
	}
	
	/**
	 * Prints the table configuration for the current (last) state of the sampler for a given list index
	 * @param list_index
	 */
	public static void printTableConfiguration(int list_index, PrintStream out)
	{
		SamplerState s = SamplerStateTracker.returnCurrentSamplerState();
		int count  = 0;
		for(int table_id=0;table_id<s.getC().get(list_index).size();table_id++)
		{
      HashSet<Integer> customers = s.getCustomersAtTable(table_id, list_index);
			if(customers != null && customers.size() > 0)
			{
				count++;
				out.println("Table "+table_id+" Count "+customers.size()+" :\t"+customers);
				
			}
		}
		out.println("There are "+count+" occupied tables");
	}



	public static void testSamplerStateEquals() {
	  SamplerState s1 = SamplerStateTracker.returnCurrentSamplerState();
    SamplerState s2 = s1.copy();

    // Make a different state
    SamplerState s3 = s1.copy();
    ArrayList<ArrayList<Integer>> c = s3.getC();
    ArrayList<Integer> c1 = c.get(1);
    c1.set(1, 999999);
    c.set(1, c1);
    s3.setC(c);

    HashMap<SamplerState, Integer> countsMap = new HashMap<SamplerState, Integer>();  
    if (countsMap.get(s1) == null) {
      System.out.println("  Get s1 returns null as expected");
    } else {
      System.out.println("  Get s1 didn't return null");
    }

    countsMap.put(s1, 0);
    Integer n = countsMap.get(s2);
    if (n == null) {
      System.out.println("  n is unexpectedly null");
    } else {
      System.out.println("  n is not null as expected.  value: " + String.valueOf(n));
      countsMap.put(s1,n+1);
      System.out.println("  updated value: " + String.valueOf(countsMap.get(s2)));
    }

    if (countsMap.get(s2) == null) {
      System.out.println("  Get s2 returned null.  this is a problem.");
    } else {
      System.out.println("  Get s2 didn't return null, as expected.");
    }

    if (countsMap.get(s3) == null) {
      System.out.println("  Get s3 returns null as expected");
    } else {
      System.out.println("  Get s3 didn't return null");
    }

    System.out.println("Does s1 equal s2?");
    System.out.println(s1.equals(s2));
    System.out.println("Does s2 equal s3?");
    System.out.println(s2.equals(s3));
    System.out.println("hashCode()");
    System.out.println(String.valueOf(s1.hashCode()));
    System.out.println(String.valueOf(s2.hashCode()));
    System.out.println(String.valueOf(s3.hashCode()));
	}


}
