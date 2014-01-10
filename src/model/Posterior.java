package model;

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Random;
import java.io.PrintStream;

/**
 * This class will store the estimated posterior from the finished Gibbs samples.
 * @author Justin Cranshaw
 *
 */
public class Posterior {

  /**
   *  The number of observations of each state (after the burnin period)
   */
  public static ArrayList<Integer> counts = new ArrayList<Integer>();
  
  /**
   *  The estimated probability (normalized counts) of each observed state 
   *  in the estimated joint distribution.
   */
  public static ArrayList<Double> probabilities = new ArrayList<Double>();
    
  /**
   *  The possible states. 
   */
  public static ArrayList<SamplerState> states = new ArrayList<SamplerState>();

  /**
   *  The burnin period (number of initial samples to ignore)
   */
  public static int burnInPeriod;

  /**
   * The number of Gibbs samples computed
   */
  public static int numSamples;

  /**
   * The normalizing constant (should be numSamples - burnInPeriod, but as a sanity check, keep store it)
   */
  public static int normConstant;


  /**
   * Estimates the probabilities over states discovered by the sampler
   */
  public static void estimatePosterior(int b)
  {
    burnInPeriod = b;
    ArrayList<SamplerState> states = SamplerStateTracker.samplerStates;
    numSamples = states.size();

    // Keep a count of each unique SamplerState observation.  
    // See @Override of SamplerState.equals() and hashMap() for equality determination.
    HashMap<SamplerState, Integer> countsMap = new HashMap<SamplerState, Integer>();  

    // Loop over each sample (after the burn in) and look for unique states
    for (int i=burnInPeriod+1; i<numSamples; i++) {
      SamplerState s = states.get(i);
      Integer count = countsMap.get(s);
      if (count != null) {
        countsMap.put(s, count + 1);
      }
      else {
        countsMap.put(s, 1);
      }
    }

    // Pull out the keys and values, and sum the normalizing constant
    Integer n = new Integer(0);
    for (Map.Entry<SamplerState, Integer> entry : countsMap.entrySet()) {
      SamplerState s = entry.getKey();
      Integer c = entry.getValue();
      n += c;
      counts.add(c);
      states.add(s);
    }
    normConstant = n;

    // Create the probabilities by dividing by the normalizing constant (should this be in log space?)
    if (n > 0) {
      for (int c : counts) {
        probabilities.add(c / (double) n);
      }
    } 

  }
  
  // public static SamplerState getMapEstimate() {

  // }
  
  /**
   * Prints the object state
   */
  public static void prettyPrint(PrintStream out)
  {
    out.println("Total number of states are "+probabilities.size());
    out.println("Probabilities: ");
    String probs = "";
    for (double p : probabilities) {
      probs += String.valueOf(p) + ",";
    }    
    out.println(probs);
  }  


  /* 
   * Here we estimate the observed draw from the Dirichlet for each table for each unique sampler state.  
   * That is, we would like to get the multinomial probability over observations for each table.  This is basically
   * just word counts, plus pseudocounts
   */
  // public static void estimateEmissionProb() {

  // }

}
