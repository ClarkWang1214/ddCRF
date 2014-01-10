package model;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.la4j.matrix.sparse.CRSMatrix;


/**
 * This class is for storing the state of the sampler for an iteration.
 * An object of this class represent the state of the sampler at one iteration.
 * @author rajarshd
 */
public class SamplerState {

	/**
	 * The number of data instances.
	 */
	private static Long num_data;
	
	/**
	 * This stores the customer link for each data point. Each list represents a city/document etc.
	 */
	private ArrayList<ArrayList<Integer>> c;
	
	/**
	 * This stores the table assignment for each data point.Each list represents a city/document etc.
	 */
	private ArrayList<ArrayList<Integer>> t;
	
	/**
	 * The number of occupied tables at this iteration
	 */
	private Long T;
	
	/**
	 * The total number of topics
	 */
	private Long K;
	/**
	 * This stores the topic assignments for each data point (which is basically the topic assignment at the given table they are sitting at)
	 */
	private ArrayList<ArrayList<Long>> k_c = new ArrayList<ArrayList<Long>>();
	
	/**
	 * This stores the topic assignments for each table;
	 */
	private ArrayList<ArrayList<Long>> k_t;
	
	/**
	 * This stores the number of tables(clusters) assigned to each topic.
	 */
	private HashMap<Long,Long> m;
	
	/**
	 * Map of table and the customer_ids.
	 * ArrayList is over documents (cities), hashmap keys are table ids, hasmap values are a hashset of customer ids
	 */
	private ArrayList<HashMap<Integer,HashSet<Integer>>> customersAtTableList;

  /** 
   * The multinomial parameter seen for each city, for each table in the city.  each theta is a CRSMatrix
   */
  private ArrayList<ArrayList<CRSMatrix>> thetas = new ArrayList<ArrayList<CRSMatrix>>();



	/**
	 * 
	 * Getters and Setters
	 * 
	 */
	
	public static Long getNum_data() {
		return num_data;
	}

	public static void setNum_data(Long num_data) {
		SamplerState.num_data = num_data;
	}


	public Long getT() {
		return T;
	}

	public void setT(Long t) {
		T = t;
	}

	public Long getK() {
		return K;
	}

	public void setK(Long k) {
		K = k;
	}

	public HashMap<Long,Long> getM() {
		return m;
	}
	
	public void setM(HashMap<Long,Long> m) {
		this.m = m;
	}

	public ArrayList<ArrayList<Integer>> getC() {
		return c;
	}
	
	/**
	 * Returns the customer link of a particular customer given the list and the customer indexes
	 * @param customer_index
	 * @param city_index
	 * @return
	 */
	public int getC(int customer_index,int city_index)
	{
		return c.get(city_index).get(customer_index);
	}

	public void setC(ArrayList<ArrayList<Integer>> c) {
		this.c = c;
	}
	
	/**
	 * Sets the new customer assignment for a customer
	 * @param cust_assignment
	 * @param customer_index
	 * @param city_index
	 */
	public void setC(Integer cust_assignment, int customer_index,int city_index)
	{
		c.get(city_index).set(customer_index, cust_assignment);
	}

	public ArrayList<ArrayList<Long>> getK_c() {
		return k_c;
	}

	public void setK_c(ArrayList<ArrayList<Long>> k_c) {
		this.k_c = k_c;
	}

	public ArrayList<ArrayList<Long>> getK_t() {
		return k_t;
	}

	public void setK_t(ArrayList<ArrayList<Long>> k_t) {
		this.k_t = k_t;
	}

	public ArrayList<ArrayList<Integer>> get_t()
	{
		return t;
	}
	
	/**
	 * Returns the table assignment for a specific customer given the list and the customer index
	 * @param customer_index
	 * @param city_index
	 * @return
	 */
	public int get_t(int customer_index,int city_index)
	{
		return t.get(city_index).get(customer_index);
	}
	public void set_t(ArrayList<ArrayList<Integer>> t) {
		this.t = t;
	}
	/**
	 * Sets the new table assignment for a customer
	 * @param table_assignment
	 * @param customer_index
	 * @param city_index
	 */
	public void set_t(Integer table_assignment, int customer_index,int city_index)
	{
		t.get(city_index).set(customer_index, table_assignment);
	}


	public ArrayList<HashMap<Integer, HashSet<Integer>>> getCustomersAtTableList() {
		return customersAtTableList;
	}


	/**
	 * returns the string of customers sitting at a table in a given list
	 * @param table_id
	 * @param list_index
	 * @return
	 */
	public HashSet<Integer> getCustomersAtTable(int tableId,int listIndex)
	{		
		if(customersAtTableList.get(listIndex).get(tableId) == null)
			return null;
		return customersAtTableList.get(listIndex).get(tableId);
	}

	/**
	 * Sets the customers sitting at a table, given the indexes and the table number
	 * @param s
	 * @param table_id
	 * @param list_index
	 */
	public void setCustomersAtTable(HashSet<Integer> customers, int tableId, int listIndex)
	{
		customersAtTableList.get(listIndex).put(tableId, customers);
	}

	public void setCustomersAtTableList(
			ArrayList<HashMap<Integer, HashSet<Integer>>> customersAtTableList) {
		this.customersAtTableList = customersAtTableList;
	}
	

	/**
	 * Returns a new sampler state which is identical to the given sampler state.
	 * @return
	 */
	public SamplerState copy()
	{
		SamplerState s = new SamplerState();
		ArrayList<ArrayList<Integer>> new_c = new ArrayList<ArrayList<Integer>>(); //customer assignments
		ArrayList<ArrayList<Integer>> new_t = new ArrayList<ArrayList<Integer>>(); //table assignments per customer
		ArrayList<HashMap<Integer, HashSet<Integer>>> newCustomersAtTableList = new ArrayList<HashMap<Integer, HashSet<Integer>>>();  
		//ArrayList<ArrayList<Long>> new_k_c = new ArrayList<ArrayList<Long>>(); //topic assignments per customer
		for(int i=0; i<c.size(); i++)
		{
			ArrayList<Integer> customer_assignments_copy = new ArrayList<Integer>(c.get(i)); //this will create a new list pointing to the same long objects, but its ok since Long is immutable.
			new_c.add(customer_assignments_copy);
			ArrayList<Integer> table_assignments_copy = new ArrayList<Integer>(t.get(i));
			new_t.add(table_assignments_copy);
			
			//ArrayList<Long> topic_assignments_copy = new ArrayList<Long>(k_c.get(i));
			//new_k_c.add(topic_assignments_copy);
		}
		for(int i=0; i<customersAtTableList.size(); i++)
		{
			HashMap<Integer,HashSet<Integer>> customersAtTableCopy = new HashMap<Integer,HashSet<Integer>>(customersAtTableList.get(i));
			newCustomersAtTableList.add(customersAtTableCopy);
		}
		s.c = new_c;
		s.t = new_t;
		s.T = new Long(T);
		s.customersAtTableList = newCustomersAtTableList;
		//s.K = new Long(K);
		return s;
	}

	/**
	 * Prints the object state
	 */
	public void prettyPrint(PrintStream out)
	{
		out.println("Total number of observations are "+SamplerState.num_data);
		out.println("Total number of documents: "+c.size());
		out.println("Total number of tables are "+T);
		
		out.println("Total number of topics "+K);
	}
	
	/*
	 * Computes the expected value of the theta vectors for this stampler state, for each city, for each table in the city
	 * Each table has a CRSMatrix theta, where
	 * theta_j = (N_j + a_j) / (n + sum_i(a_i))
	 * where a_j is the Dirichlet prior parameter
	 */
	// public void estimateThetas() {
	// 	// ArrayList<ArrayList<Double>> listObservations = Data.getObservations();  
	// 	// private ArrayList<ArrayList<CRSMatrix>> thetas

	// }

	/**
	 * Returns for each city, a set of sets of customers sitting at each table
	 */
	public ArrayList<HashSet<HashSet<Integer>>> getTableSeatingsSet() {
		ArrayList<HashSet<HashSet<Integer>>> tableSeatings = new ArrayList<HashSet<HashSet<Integer>>>();
		for (ArrayList<Integer> cityTables : t) {
			HashMap<Integer, HashSet<Integer>> tableMembers = new HashMap<Integer, HashSet<Integer>>();
			for (int i=0; i<cityTables.size(); i++) {
				Integer tab = cityTables.get(i);
				// check if the table is empty
				if (tableMembers.get(tab) == null)
					tableMembers.put(tab, new HashSet<Integer>());
				HashSet<Integer> tableTabMembers = tableMembers.get(tab);
				tableTabMembers.add(i);
				tableMembers.put(tab, tableTabMembers);
			}
			// Now look over the hash, and put members in a set
			HashSet<HashSet<Integer>> cityTableSeatings = new HashSet<HashSet<Integer>>();
			for (HashSet<Integer> value : tableMembers.values()) {
				cityTableSeatings.add(value);
			}
			tableSeatings.add(cityTableSeatings);
		}
		return tableSeatings;
	}

	/**
	 * Return a number from 0 to 1 giving the Jiccard similarity between
	 * tables in s and tables in this.
	 */
	public double tableJiccardSimilarity(SamplerState s) {
		ArrayList<HashSet<HashSet<Integer>>> seatingsA = getTableSeatingsSet();
		ArrayList<HashSet<HashSet<Integer>>> seatingsB = s.getTableSeatingsSet();
		int sizeUnion = 0;
		int sizeIntersection = 0;

		for (int i=0; i<seatingsA.size(); i++) {
			HashMap<HashSet<Integer>,Integer> counts = new HashMap<HashSet<Integer>,Integer>();
			HashSet<HashSet<Integer>> citySeatingsA = seatingsA.get(i);
			HashSet<HashSet<Integer>> citySeatingsB = seatingsB.get(i);
			for (HashSet<Integer> t : citySeatingsA) {
				if (counts.get(t) == null)
					counts.put(t, 0);
				counts.put(t, counts.get(t)+1);
			}
			for (HashSet<Integer> t : citySeatingsB) {
				if (counts.get(t) == null)
					counts.put(t, 0);
				counts.put(t, counts.get(t)+1);
			}
			int citySizeUnion = counts.keySet().size();
			int citySizeIntersection = 0;
			// any key in counts that has a value of 2 is in the intersection
			for (int count : counts.values()) {
				if (count == 2) {
					citySizeIntersection += 1;
				}
				if (count > 2) {
					// something is wrong if this happens.
					System.out.println("More than 2 in the intersection!");
					System.out.println("  "+count);
				}
			}
			sizeUnion += citySizeUnion;
			sizeIntersection += citySizeIntersection;
		}

		return sizeIntersection / (double) sizeUnion;
	}

	/**
	 * equals comparator for SamplerStates, just checks the customer assignments and topic assignments
	 */
	@Override
	public boolean equals(Object obj) 
	{
		if (obj == null) return false;
    if (obj == this) return true;
    if (!(obj instanceof SamplerState))return false;	
		SamplerState s = (SamplerState) obj;
		// return (this.c.equals(s.getC()) && this.k_c.equals(s.getK_c()));
		// return c.equals(s.getC());  // need to take into account k_c.  for now its buggy because of null.
		return getTableSeatingsSet().equals(s.getTableSeatingsSet());
	}

	/**
	 * Overrids hashCode based on c and k_c.  This concatenates the individual hashCodes as strings,
	 * then computes the hashCode of the resulting unique String.
	 */
	@Override
	public int hashCode() {
		// String s = String.valueOf(this.c.hashCode()) + ":" + String.valueOf(this.k_c.hashCode());
		// return c.hashCode();  // need to take into account k_c too.  for now its buggy because of null.
		return getTableSeatingsSet().hashCode();
	}
}
