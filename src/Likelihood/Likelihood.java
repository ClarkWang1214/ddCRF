package Likelihood;

import java.util.ArrayList;

import model.HyperParameters;


/**
 * Generic interface for computing likelihood of the data.
 * Your likelihood implementation should implement this interface
 * @author rajarshd
 *
 */
abstract public class Likelihood {
	
  protected HyperParameters hyperParameters;  // Need to abstract the notion of Hyperparameters.  Currently its for Dir.

  public HyperParameters getHyperParameters() {
    return(hyperParameters);
  }

	/**
	 * Method for computing log-likelihood of the data at a table.
	 * @param table_members list of indexes of the observation.
	 * @param list_index index of the list, the observation at the tables belong to.
	 * @return
	 */
	abstract public double computeTableLogLikelihood(ArrayList<Integer> table_members,int list_index);

}
