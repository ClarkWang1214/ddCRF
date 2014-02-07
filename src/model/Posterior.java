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
  private ArrayList<Integer> counts;
  
  /**
   *  The estimated probability (normalized counts) of each observed state 
   *  in the estimated joint distribution.
   */
  private ArrayList<Double> probabilities;
    
  /**
   *  The possible states. 
   */
  private ArrayList<SamplerState> states;

  /**
   *  The burnin period (number of initial samples to ignore)
   */
  private int burnInPeriod;

  /**
   * The number of Gibbs samples computed
   */
  private int numSamples;

  /**
   * The normalizing constant (should be numSamples - burnInPeriod, but as a sanity check, keep store it)
   */
  private int normConstant;

  /**
   * The model hyperparameters
   */
  private HyperParameters hyperParameters;

  public Posterior(HyperParemeters hyperparameters) {
    this.hyperparameters = hyperparameters;
  }


  /**
   * Estimates the probabilities over states discovered by the sampler
   */
  public void estimatePosterior()
  {
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
  
  /**
   * Return the Sampler state with largest posterior density
   * Instead of counting the state that appears most frequently in our samples,
   * for high dimensional problems we get the posterior density of each state,
   * and return the state that maximizes this.
   */
  public SamplerState getMapEstimateDensity() {
    ArrayList<SamplerState> states = SamplerStateTracker.samplerStates;
    double maxLogLik = -10000000000000000000.0;
    for (SamplerState s : states) {
      Theta t = new Theta(s, );
    }
  }
  
  /**
   * Prints the object state
   */
  public void prettyPrint(PrintStream out)
  {
    out.println("Total number of states are "+probabilities.size());
    out.println("Probabilities: ");
    String probs = "";
    for (double p : probabilities) {
      probs += String.valueOf(p) + ",";
    }    
    out.println(probs);
  }  


}
