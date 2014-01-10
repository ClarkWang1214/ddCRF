import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;

import model.HyperParameters;
import model.SamplerStateTracker;
import sampler.GibbsSampler;
import util.Util;
import Likelihood.DirichletLikelihood;
import Likelihood.Likelihood;
import data.Data;


public class Driver {
	
	public static int vocab_size;

	/**
	 * @param args
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws FileNotFoundException {
		// TODO Auto-generated method stub
		try {
			
			vocab_size = Integer.parseInt(args[1]);
			System.out.println("Vocab Size is "+vocab_size);
			
			//set the hyper-parameters
			double dirichlet_param = Double.parseDouble(args[2]);
			System.out.println("Dirichlet parameter is "+dirichlet_param);
			ArrayList<Double> dirichlet = new ArrayList<Double>();
			for(int i=0;i<vocab_size;i++)
				dirichlet.add(dirichlet_param);
			double alpha = Double.parseDouble(args[3]);
			System.out.println("Self Linkage Prob is "+alpha);
			HyperParameters h = new HyperParameters(vocab_size, dirichlet, alpha);
			
			ArrayList<ArrayList<Double>> list_observations = Data.getObservations();	
			SamplerStateTracker.initializeSamplerState(list_observations);
			Likelihood l = new DirichletLikelihood(h);
			
			//do sampling		
			SamplerStateTracker.max_iter = Integer.parseInt(args[0]);
			System.out.println("Gibbs Sampler will run for "+SamplerStateTracker.max_iter+" iterations.");
			long init_time = System.currentTimeMillis();
			for(int i=1;i<=SamplerStateTracker.max_iter;i++)
			{
				long init_time_iter = System.currentTimeMillis();
				GibbsSampler.doSampling(l);
				System.out.println("Iteration "+i+" done");
				System.out.println("Took "+(System.currentTimeMillis() - init_time_iter)/(double)1000+" seconds");
			}
			
			long diff = System.currentTimeMillis() - init_time; 
			System.out.println("Time taken for Sampling "+(double)diff/1000+" seconds");		
			for(int i=0;i<list_observations.size();i++)
				Util.printTableConfiguration(i, new PrintStream("tables/table_configuration"+i+".txt"));
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

}
