package sampler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import util.Util;

import model.CityTable;
import model.SamplerState;
import model.SamplerStateTracker;
import Likelihood.Likelihood;

public class CRPGibbsSampler {

	/**
	 * Sample a topic for a table in a city.
	 * @param l
	 * @param table_index
	 * @param list_index
	 * @return
	 */
	public static int sampleTopic(Likelihood l,int tableId,int listIndex)
	{
		SamplerState currentState = SamplerStateTracker.returnCurrentSamplerState();
		ArrayList<Double> observationsAtTable = currentState.getObservationAtTable(tableId, listIndex); //observations of customers sitting at the table. 
		//For this table (which we are sampling for), they presently do not belong to any topic, since we are sampling for one
		//hence, removing the entries from all datastructures
		CityTable ct = new CityTable(listIndex, tableId);
		Integer k_old = currentState.getTopicForCityTable(ct); //old topic for the table
		if(k_old != null)
		{
			currentState.getTopicAtTable().remove(ct); //removing the entry from the map of citytable to topic
			currentState.removeTableFromTopic(k_old, ct); //removing the table from the corresponding topic
			currentState.decreaseTableCountsForTopic(k_old); //decrementing the table count for k_old
		}
		
		//Now setup the log-posterior for sampling
		HashMap<Integer,Integer> numTablesPerTopic = currentState.getM();
		Set<Entry<Integer,Integer>> allTopicsToNumTablesMapping = numTablesPerTopic.entrySet();
		Iterator<Entry<Integer,Integer>> iter = allTopicsToNumTablesMapping.iterator();
		ArrayList<Double> posterior = new ArrayList<Double>(); //this will hold all the posterior probabilities
		ArrayList<Integer> indexes = new ArrayList<Integer>();
		while(iter.hasNext())
		{
			Entry<Integer,Integer> mapEntry = iter.next();
			int topicId = mapEntry.getKey();
			int numTables = mapEntry.getValue(); //this is the prior for CRP
			double logPrior = Math.log(numTables);
			ArrayList<Double> allObservationFromTopic = currentState.getAllObservationsForTopic(topicId);
			double logConditionalLikelihood = l.computeConditionalLogLikelihood(observationsAtTable, allObservationFromTopic);
			double logPosteriorProb = logPrior + logConditionalLikelihood;
			posterior.add(Math.exp(logPosteriorProb));
			indexes.add(topicId); //to keep track of which topic_id got selected.
		}
		//now for self-linkage
		double logBeta = Math.log(l.getHyperParameters().getSelfLinkProbCRP());
		double logMarginalLikelihood = l.computeTableLogLikelihood(observationsAtTable); //this is marginal likelihood, instead of conditional
		double logPosteriorProb = logBeta + logMarginalLikelihood;
		posterior.add(Math.exp(logPosteriorProb));
		int maxTopicId = currentState.getMaxTopicId();
		indexes.add(maxTopicId+1); //incrementing maxTopicId to account for the new topic
		
		//Now finally sample for a topic
		int sampledTopicId = Util.sample(posterior);
		if(sampledTopicId == maxTopicId+1) //The table sat chose to sit in a new topic table ie new topic sampled
		{
			currentState.setMaxTopicId(maxTopicId+1); //increase the maxTopicId
			currentState.setK(currentState.getK()+1); //increment the total number of topics
		}
		currentState.getTopicAtTable().put(ct, sampledTopicId); //updating the map of citytables to topic
		currentState.addTableToTopic(sampledTopicId, ct);
		currentState.addTableCountsForTopic(sampledTopicId);
		
		return sampledTopicId;
		
	}
	
}
