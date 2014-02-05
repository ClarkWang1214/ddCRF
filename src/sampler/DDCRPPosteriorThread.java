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
import java.util.Map.Entry;


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

public class DDCRPPosteriorThread implements Runnable {

  int i;
  Entry<Integer,Integer> mapEntry;
  ArrayList<Double> posterior;
  ArrayList<Integer> indexes;
  ArrayList<Double> observationsAtTable;
  SamplerState currentState;
  Likelihood l;

  public DDCRPPosteriorThread(int i, Entry<Integer,Integer> mapEntry, ArrayList<Double> posterior, ArrayList<Integer> indexes, ArrayList<Double> observationsAtTable, SamplerState currentState, Likelihood l) {
    // Set the parameters for this thread run
    this.i = i;
    this.mapEntry = mapEntry;
    this.posterior = posterior;
    this.indexes = indexes;
    this.observationsAtTable = observationsAtTable;
    this.currentState = currentState;
    this.l = l;
  }

  public void run() {
    int topicId = mapEntry.getKey();
    int numTables = mapEntry.getValue(); //this is the prior for CRP
    double logPrior = Math.log(numTables);
    ArrayList<Double> allObservationFromTopic = currentState.getAllObservationsForTopic(topicId);
    double logConditionalLikelihood = l.computeConditionalLogLikelihood(observationsAtTable, allObservationFromTopic);
    double logPosteriorProb = logPrior + logConditionalLikelihood;
    posterior.set(i, logPosteriorProb);
    indexes.set(i, topicId); //to keep track of which topic_id got selected.
  }
}