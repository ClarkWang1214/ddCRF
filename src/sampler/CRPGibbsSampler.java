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
		Double maxLogPosterior = new Double(-1000000000.0);
		while(iter.hasNext()) //iterating over all topics
		{
			Entry<Integer,Integer> mapEntry = iter.next();
			int topicId = mapEntry.getKey();
			int numTables = mapEntry.getValue(); //this is the prior for CRP
			double logPrior = Math.log(numTables);
			ArrayList<Double> allObservationFromTopic = currentState.getAllObservationsForTopic(topicId);
			double logConditionalLikelihood = l.computeConditionalLogLikelihood(observationsAtTable, allObservationFromTopic);
			double logPosteriorProb = logPrior + logConditionalLikelihood;
			if (logPosteriorProb > maxLogPosterior)
				maxLogPosterior = logPosteriorProb;
			posterior.add(logPosteriorProb);
			indexes.add(topicId); //to keep track of which topic_id got selected.
		}
		//now for self-linkage
		double logBeta = Math.log(l.getHyperParameters().getSelfLinkProbCRP());
		double logMarginalLikelihood = l.computeConditionalLogLikelihood(observationsAtTable, new ArrayList<Double>()); //this is marginal likelihood, instead of conditional
		double logPosteriorProb = logBeta + logMarginalLikelihood;
		if (logPosteriorProb > maxLogPosterior)
			maxLogPosterior = logPosteriorProb;		
		posterior.add(logPosteriorProb);
		int maxTopicId = currentState.getMaxTopicId();
		indexes.add(maxTopicId+1); //incrementing maxTopicId to account for the new topic
		
		// Subtract the maxLogPosterior from each term of posterior (avoid overflows), then exponentiate
		for (int i=0; i<posterior.size(); i++)
			posterior.set(i, Math.exp(posterior.get(i) - maxLogPosterior));

		//Now finally sample for a topic
		int sampledTopicIndex = Util.sample(posterior);
		
		int sampledTopicId = indexes.get(sampledTopicIndex); //actual topic id
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
	
	/**
	 * Sample a topic for a table in a city, without updating the sampler state
	 * @param l
	 * @param tableId
	 * @param listIndex
	 * @return
	 */
	public int sampleTopicWithoutSavingState(Likelihood l,int tableId,int listIndex)
	{
		SamplerState currentState = SamplerStateTracker.returnCurrentSamplerState();
		ArrayList<Double> observationsAtTable = currentState.getObservationAtTable(tableId, listIndex); //observations of customers sitting at the table.
		CityTable ct = new CityTable(listIndex, tableId);
		Integer k_old = currentState.getTopicForCityTable(ct); //old topic for the table
		//Now setup the log-posterior for sampling
		HashMap<Integer,Integer> numTablesPerTopic = currentState.getM();
		Set<Entry<Integer,Integer>> allTopicsToNumTablesMapping = numTablesPerTopic.entrySet();
		Iterator<Entry<Integer,Integer>> iter = allTopicsToNumTablesMapping.iterator();
		ArrayList<Double> posterior = new ArrayList<Double>(); //this will hold all the posterior probabilities
		ArrayList<Integer> indexes = new ArrayList<Integer>();
		
		while(iter.hasNext()) //iterating over all topics
		{
			Entry<Integer,Integer> mapEntry = iter.next();
			int topicId = mapEntry.getKey();
			int numTables = mapEntry.getValue(); //this is the prior for CRP
			double logPrior = Math.log(numTables);
			if(topicId == k_old) //since the sampler state has not been updated, therefore in this case, I have to subtract the prior by 1, since I shouldnot include this table we are sampling for
			{
				if(numTables == 1)
					continue; //if numTables == 1, then this is the only table with this topic and ideally if we remove this from the sampler state, then we will remove the topic too
				else
					logPrior = Math.log(numTables - 1);
			}
			double logConditionalLikelihood = 0.0;
			double logPosteriorProb = 0.0;
			if(topicId != k_old)
			{
				ArrayList<Double> allObservationFromTopic = currentState.getAllObservationsForTopic(topicId);
				logConditionalLikelihood = l.computeConditionalLogLikelihood(observationsAtTable, allObservationFromTopic);
			}
			else //if they are equal
			{
				ArrayList<Double> allObservationFromTopicMinusTable = currentState.getAllObservationsForTopicMinusTable(topicId, tableId, listIndex);
				logConditionalLikelihood = l.computeConditionalLogLikelihood(observationsAtTable, allObservationFromTopicMinusTable);
			}
			logPosteriorProb = logPrior + logConditionalLikelihood;
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
		int sampledTopicIndex = Util.sample(posterior);
		int sampledTopicId = indexes.get(sampledTopicIndex); //actual topic id
		
		//Not updating any sampler state
		return sampledTopicId;
	}
	
	/**
	 * This method basically does only the updates to the sampler state because of sampleTopics WITHOUT actually sampling a topic.
	 * That's why it takes the sampleTopicId as a param. Basically the work done by sampleTopic = work done by sampleTopicWithoutSavingState + updateSamplerStateAfterSamplingTopic
	 * Why is this important? -- Because, while sampling for a link in ddcrp, we have to consider all possible customer assignments, and for every resulting table join
	 * we should ideally sample a topic for the joined table. Now at this stage of the ddcrp, this is just a possibility of joining of tables and not an actual join, so we shouldn't
	 * actually change the sampler states. So for that, we can call the sampleTopicWithoutSavingState method to sample a topic and then when we actually sample a customer assignment and if 
	 * that results in a join of the table, we can update the sampler state by calling this method.
	 * @param l
	 * @param tableId
	 * @param sampledTopicId
	 * @param listIndex
	 */
	public void updateSamplerStateAfterSamplingTopic(Likelihood l,int tableId,int sampledTopicId,int listIndex)
	{
		SamplerState currentState = SamplerStateTracker.returnCurrentSamplerState();
		ArrayList<Double> observationsAtTable = currentState.getObservationAtTable(tableId, listIndex); //observations of customers sitting at the table. 
		//Now removing the entries from all datastructures for old topic
		CityTable ct = new CityTable(listIndex, tableId);
		Integer k_old = currentState.getTopicForCityTable(ct); //old topic for the table
		if(k_old != null)
		{
			currentState.getTopicAtTable().remove(ct); //removing the entry from the map of citytable to topic
			currentState.removeTableFromTopic(k_old, ct); //removing the table from the corresponding topic
			currentState.decreaseTableCountsForTopic(k_old); //decrementing the table count for k_old
		}
		
		//Now performing required updates for the new sampled topic
		int maxTopicId = currentState.getMaxTopicId();
		if(sampledTopicId == maxTopicId+1) //The table sat chose to sit in a new topic table ie new topic sampled
		{
			currentState.setMaxTopicId(maxTopicId+1); //increase the maxTopicId
			currentState.setK(currentState.getK()+1); //increment the total number of topics
		}
		currentState.getTopicAtTable().put(ct, sampledTopicId); //updating the map of citytables to topic
		currentState.addTableToTopic(sampledTopicId, ct);
		currentState.addTableCountsForTopic(sampledTopicId);

		
	}
	
}
