/**
 * 
 */
package Likelihood;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.commons.math3.special.Gamma;

import model.HyperParameters;

import data.Data;

/**
 * Dirichlet Likelihood for the collapsed Gibbs Sampler
 * @author rajarshd
 *
 */
public class DirichletLikelihood extends Likelihood {

	private static HashMap<Double,Double> cached_gamma_values = new HashMap<Double,Double>();
	
	public DirichletLikelihood(HyperParameters h) {
		hyperParameters = h;
	}

	@Override
	public double computeTableLogLikelihood(ArrayList<Integer> table_members,
			int list_index) {
		
		//get the observations
		ArrayList<ArrayList<Double>> list_observations = Data.getObservations();
		ArrayList<Double> observations = list_observations.get(list_index);
		HashMap<Double,Integer> obs_category_count = new HashMap<Double,Integer>(); //this map will store the index of the venue category and the respective counts of the table members
		//creating the map
		for(int i=0;i<table_members.size();i++)
		{
			Double obs_category = observations.get(table_members.get(i));
			if(obs_category_count.get(obs_category) == null) //new category			
				obs_category_count.put(obs_category, 1);
			else
				obs_category_count.put(obs_category, obs_category_count.get(obs_category) + 1 );	
		}
		
		//get the dirichlet hyper-parameter
		ArrayList<Double> dirichletParams = hyperParameters.getDirichletParam();
		double  sum_venue_cat_alpha=0, sum_log_gamma_sum_venue_cat_alpha = 0,sum_alpha =0,sum_log_gamma_alpha=0 ;
		
		for(int i=0;i<dirichletParams.size();i++) //loop for each possible venue category
		{
			Integer category_count = obs_category_count.get(new Double(i));
			if(category_count == null) 
				category_count = 0; //in case no venue of a certain category isnt present, the count is 0
			sum_alpha = sum_alpha + dirichletParams.get(i); 			
			sum_venue_cat_alpha = sum_venue_cat_alpha + dirichletParams.get(i) + category_count;
			sum_log_gamma_sum_venue_cat_alpha = sum_log_gamma_sum_venue_cat_alpha + logGamma(dirichletParams.get(i)+category_count);
			sum_log_gamma_alpha = sum_log_gamma_alpha + logGamma(dirichletParams.get(i));
		}
		
		double log_numerator = sum_log_gamma_sum_venue_cat_alpha - logGamma(sum_venue_cat_alpha);
		double log_denominator = sum_log_gamma_alpha - Gamma.logGamma(sum_alpha); //NO need to compute the denominator as it is same for all tables, given an alpha
		
		
		double log_likelihood = log_numerator - log_denominator;
		
		return log_likelihood;
	}
	
	/**
	 * Checks if the value of the gamma is cached, if so returns it, else caches it
	 * @param arg
	 * @return
	 */
	private static double logGamma(double arg)
	{
		if(cached_gamma_values.get(arg) == null)
		{
			double log_gamma = Gamma.logGamma(arg);
			cached_gamma_values.put(arg, log_gamma);
			return log_gamma;
		}
		else				
			return cached_gamma_values.get(arg);		
	}

	public double computeFullLogLikelihood(ArrayList<HashMap<Integer, HashSet<Integer>>> customersAtTableList) {
		double ll = 0;
		for (int listIndex=0; listIndex<customersAtTableList.size(); listIndex++) {
			HashMap<Integer, HashSet<Integer>> customersAtTable = customersAtTableList.get(listIndex);
			for (Integer tableId : customersAtTable.keySet()) {
				if (customersAtTable.get(tableId) != null) {
					HashSet<Integer> hs = customersAtTable.get(tableId);
					ArrayList<Integer> tableMembers = new ArrayList<Integer>(hs);
					ll += computeTableLogLikelihood(tableMembers, listIndex);
				}
			}
		}
		return ll;
	}

}
