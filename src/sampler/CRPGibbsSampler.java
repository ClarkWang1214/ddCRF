package sampler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

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
			currentState.topicAtTable.remove(ct); //removing the entry from the map of citytable to topic
			currentState.removeTableFromTopic(k_old, ct); //removing the table from the corresponding topic
			currentState.decreaseTableCountsForTopic(k_old); //decrementing the table count for k_old
		}
		
		//Now setup the log-posterior for sampling
		HashMap<Integer,Integer> numTablesPerTopic = currentState.getM();
		Set<Entry<Integer,Integer>> allTopicsToNumTablesMapping = numTablesPerTopic.entrySet();
		Iterator<Entry<Integer,Integer>> iter = allTopicsToNumTablesMapping.iterator();
		ArrayList<Double> logPosterior = new ArrayList<Double>(); //this will hold all the posterior probabilities
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
			logPosterior.add(logPosteriorProb);
			indexes.add(topicId); //to keep track of which topic_id got selected.
		}
		//now for self-linkage
		
		
		return 0;
	}
	
}
