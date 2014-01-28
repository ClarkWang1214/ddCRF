package model;

import data.Data;

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;

import org.la4j.matrix.sparse.CRSMatrix;


/**
 * Given a customer assignment and topic assignment(sampler state), this computes the inferred 
 * multinomial distribution parameters at each table
 * @author jcransh
 */
public class Theta {

  /** 
   * Constructor
   * @author jcransh
   */
  public Theta(SamplerState samplerState, HyperParameters hyperParameters)
  {
    this.samplerState = samplerState;
    this.hyperParameters = hyperParameters;
  }

  /** 
   * Constructor
   * @author jcransh
   */
  public Theta(){}

  /** 
   * The multinomial parameter seen for each city, for each table in the city.  each theta is a CRSMatrix
   */
  private HashMap<Integer, CRSMatrix> topicToThetaMap = new HashMap<Integer, CRSMatrix>();

  /**
   * The SamplerState for which we'd like to compute thetas for
   */ 
  private static SamplerState samplerState;

  /**
   * The model hyperparameters
   */ 
  private static HyperParameters hyperParameters;

  /**
   * Getter for the samplerState
   */
  public SamplerState getSamplerState() {
    return samplerState;
  }

  /**
   * Setter for the samplerState
   */
  public void setSamplerState(SamplerState s) {
    samplerState = s;
  }

  /**
   * Getter for the samplerState
   */
  public HyperParameters getHyperParameters() {
    return hyperParameters;
  }

  /**
   * Setter for the samplerState
   */
  public void setHyperParameters(HyperParameters h) {
    hyperParameters = h;
  }  

 /**
   * Getter for the computed theta map
   */
  public HashMap<Integer, CRSMatrix> getTopicToThetaMap() {
    return topicToThetaMap;
  }

  /*
   * Computes the value of the theta vectors for this stampler state, for each city, for each table in the city
   * Each table has a CRSMatrix theta, where
   * theta_j = (N_j + a_j) / (n + sum_i(a_i))
   * where a_j is the Dirichlet prior parameter
   */
  public void estimateThetas() {
    HashMap<Integer, CRSMatrix> newTopicToThetaMap = new HashMap<Integer, CRSMatrix>();

    // for each topic
    //   init a CRSMatrix of length of the vocab size (all zeros)
    //       for each observation of the topic, compute the counts per category

    HashSet<Integer> topics = samplerState.getAllTopics(); 
    for (Integer topic : topics) {
      // Initialize the topics's theta vector
      CRSMatrix topicTheta = new CRSMatrix(1,hyperParameters.getVocabSize()); 
      
      // add the dirichlet parameters
      ArrayList<Double> dirichletParam = hyperParameters.getDirichletParam();
      for (int j=0; j<dirichletParam.size(); j++) {
        topicTheta.set(0, j, dirichletParam.get(j));
      }

      // get all the observatiosn for this topic and add them to the counts
      ArrayList<Double> topicObservations = samplerState.getAllObservationsForTopic(topic);
      for (Double obs : topicObservations) {
        Integer observation = obs.intValue();
        double currentObservationCount = topicTheta.get(0, observation);
        topicTheta.set(0, observation, currentObservationCount + 1);
      }

      // get the normalizing constant
      double norm = 0.0;
      for (int j=0; j<hyperParameters.getVocabSize(); j++) {
        norm += topicTheta.get(0,j);
      }

      // divide by the normalizing constant
      for (int j=0; j<hyperParameters.getVocabSize(); j++) {
        double thetaJ = topicTheta.get(0,j);
        topicTheta.set(0, j, thetaJ/norm);
      }    

      newTopicToThetaMap.put(topic, topicTheta);
    } 

    topicToThetaMap = newTopicToThetaMap;
  }

  /*
   * Output the thetas by topic
   */
  public void prettyPrint() {
    for (Map.Entry<Integer, CRSMatrix> entry : topicToThetaMap.entrySet()) {
      Integer topic = entry.getKey();
      CRSMatrix theta = entry.getValue();
      System.out.println("Topic " + topic);
      String out = "       ";
      for (int k=0; k<hyperParameters.getVocabSize(); k++) {
        double count = theta.get(0, k);
        if (count > 0) {
          out += k + ":" + count + " ";
        } 
      }
      if (out != "       ")
        System.out.println(out);
    } 
  }
}