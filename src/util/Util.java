package util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import data.Data;

import model.CityTable;
import model.SamplerState;
import model.SamplerStateTracker;

public class Util {

	public static final String cityNamesFile = "Data/cities.txt";
	public static final String venueCategoriesFile = "Data/venue_categories.txt";
	public static final String outputCSV = "output.csv";
	
	/**
	 * Samples from a discrete distribution. The input is a list of probabilites (non negative and non zero)
	 * They need not sum to 1, the list will be normalized. 
	 * @param probs
	 * @return
	 */
	public static int sample(List<Double> probs)
	{
		ArrayList<Double> cumulative_probs = new ArrayList<Double>();
		Double sum_probs = new Double(0.0);
		for(Double prob:probs)
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
		Double nextRandom = r.nextDouble();
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
	
	/**
	 * Utility method for generating the csv file for cities.
	 */
	public static void outputCSVforMap()
	{
		//read the list of city names
		ArrayList<String> cityNames = new ArrayList<String>();
		try
		{
			BufferedReader reader = new BufferedReader(new FileReader(cityNamesFile));			
			String line;
			 while((line = reader.readLine())!=null)
			 {
				 if(line!=null)
				 {
					 String [] splits = line.split(",");
					 cityNames.add(splits[1]);
				 }
			 }
			 reader.close();
		}catch(FileNotFoundException ex){
			ex.printStackTrace();
		}catch(IOException ex){
			ex.printStackTrace();
		}
		
		//read the venueCategories file
		ArrayList<String> venueCategories = new ArrayList<String>();
		try
		{
			BufferedReader reader = new BufferedReader(new FileReader(venueCategoriesFile));
			String line;
			while((line = reader.readLine())!=null)
			 {
				venueCategories.add(line);
			 }
		}catch(FileNotFoundException ex){
			ex.printStackTrace();
		}catch(IOException ex){
			ex.printStackTrace();
		}
		
		//read the city_venue files for all cities
		ArrayList<ArrayList<Venue>> allVenues = new ArrayList<ArrayList<Venue>>(); 
		try
		{
			for(int i=1;i<=87;i++)
			{
				allVenues.add(new ArrayList<Venue>()); //a new city
				BufferedReader reader = new BufferedReader(new FileReader("Data/cities_sim/city_"+i+"_venue_ids.txt"));
				String line;
				while((line = reader.readLine())!=null)
				 {
					String [] splits = line.split(",");
					Venue v = new Venue();
					v.setVenueName(splits[1]);
					v.setCityId(i-1);
					v.setCityName(cityNames.get(i-1)); //i-1 because city index starts from 0
					v.setLat(Double.parseDouble(splits[splits.length-2]));
					v.setLon(Double.parseDouble(splits[splits.length-1]));
					//will put the category name later, because if the venue_name has ',', then the indices might be different and I can get the 
					//venue_cats later by mapping from the observations 
					allVenues.get(allVenues.size() -1).add(v);
				 }
			}
		}catch(FileNotFoundException ex){
			ex.printStackTrace();
		}catch(IOException ex){
			ex.printStackTrace();
		}
		
		ArrayList<ArrayList<Double>> allObservations = Data.getObservations(); // all observations
		
		SamplerState s = SamplerStateTracker.returnCurrentSamplerState();
		HashSet<Integer> allTopicIds = s.getAllTopics();
		Iterator<Integer> iter = allTopicIds.iterator();
		PrintStream p = null;
		try {
			 p = new PrintStream("output.csv");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		while(iter.hasNext()) //over all topic_ids
		{
			Integer topicId = iter.next();
			if(topicId != null)		
			{
				HashSet<CityTable> cityTables = s.getCityTablesForTopic(topicId);
				Iterator<CityTable> ctIter = cityTables.iterator();
				while(ctIter.hasNext()) //over all citytables in a topic
				{
					CityTable ct = ctIter.next();
					int cityId = ct.getCityId();
					int tableId = ct.getTableId();
					HashSet<Integer> customerIndices = s.getCustomersAtTable(tableId, cityId);
					ArrayList<Double> allObservationsInCity = allObservations.get(cityId);
					Iterator<Integer> venueIter = customerIndices.iterator();
					while(venueIter.hasNext()) //over venues at a table of a topic
					{
						Integer venueId = venueIter.next();
						if(venueId != null)
						{
							Venue v = allVenues.get(cityId).get(venueId);
							v.setTableId(tableId);
							v.setTopicId(topicId);							
							Double obs = allObservationsInCity.get(venueId);
							v.setVenueCategoryId(obs.intValue());
							v.setVenueCategory(venueCategories.get(obs.intValue()-1));							
							v.printVenueConfig(p);
						}
					}
				}
			}
		}
		p.close();
		
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
