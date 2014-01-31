package sampler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

import model.HyperParameters;
import model.SamplerState;
import model.SamplerStateTracker;
import model.CityTable;

import org.jgraph.graph.DefaultEdge;
import org.jgrapht.DirectedGraph;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.CycleDetector;
import org.jgrapht.graph.AsUndirectedGraph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.traverse.DepthFirstIterator;
import org.la4j.matrix.sparse.CRSMatrix;
import org.la4j.vector.Vector;

import util.Util;

import Likelihood.Likelihood;

import data.Data;

public class PosteriorThread implements Runnable {

  Integer i;
  Integer list_index; 
  Integer table_id;
  Integer table_proposed;
  CityTable currentCT;
  Integer currentTopic;
  Integer proposedTopic;
  Double currentTopicLogLik;
  Double currentTopicMinusTableLogLik;
  Vector priors;
  ArrayList<Double> posterior;
  ArrayList<Integer> posteriorIndices;
  SamplerState s;
  Likelihood ll;

  public PosteriorThread(Integer i, 
                         Integer list_index, 
                         Integer table_id,
                         CityTable currentCT,
                         Integer currentTopic,
                         Double currentTopicLogLik,
                         Double currentTopicMinusTableLogLik,
                         Vector priors,
                         ArrayList<Double> posterior,
                         ArrayList<Integer> posteriorIndices,
                         SamplerState s, 
                         Likelihood ll
                        ) {

    // Set the parameters for this thread run
    this.i = i;
    this.list_index = list_index;
    this.table_id = table_id;
    this.currentCT = currentCT;
    this.currentTopic = currentTopic;
    this.currentTopicLogLik = currentTopicLogLik;
    this.currentTopicMinusTableLogLik = currentTopicMinusTableLogLik;
    this.priors = priors;
    this.posterior = posterior;
    this.posteriorIndices = posteriorIndices;
    this.s = s;
    this.ll = ll;

    // System.out.println("-----------------");
    // System.out.println(i);
    // System.out.println(list_index);
    // System.out.println(table_id);
    // System.out.println(currentCT);
    // System.out.println(currentTopic);
    // System.out.println(currentTopicLogLik);
    // System.out.println(currentTopicMinusTableLogLik);
    // System.out.println(priors);
    // System.out.println(posterior);
    // System.out.println(posteriorIndices);
  }

  public void run() {
    if(priors.get(i)!=0)
    {
      // indexes.add(i); //adding the index of this possible customer assignment.
      //get the table id of this table        
      int table_proposed = s.get_t(i, list_index); //table_proposed is the proposed table to be joined
      if(table_proposed == table_id) //since the proposed table is the same, hence there will be no change in the likelihood if this is the customer assignment       
      { 
        Double logPosterior = Math.log(priors.get(i));
        posterior.set(i, logPosterior); //since the posterior will be determined only by the prior probability
        posteriorIndices.set(i, 1);
      }
      else //will have to compute the change in likelihood
      {         

        CityTable proposedCT = new CityTable(list_index, table_proposed);
        Integer proposedTopic = s.getTopicForCityTable(proposedCT);

        if (currentTopic == null) {
          System.out.println("There is a null in the current Topic");
          System.out.println(currentCT.getCityId() + ":" + currentCT.getTableId());
          System.out.println(s.getObservationAtTable(currentCT.getTableId(),currentCT.getCityId()));
        }

        Double changeInLogLik = GibbsSampler.computeCachedTopicChangeInLikelihood(s, ll, table_id, list_index, currentTopic, proposedTopic, currentTopicLogLik, currentTopicMinusTableLogLik);
        Double logPosterior = Math.log(priors.get(i)) + changeInLogLik;

        // //Now compute the change in likelihood
        posterior.set(i, logPosterior); //adding the prior and likelihood
        posteriorIndices.set(i, 1);
      }
    }
  }
}