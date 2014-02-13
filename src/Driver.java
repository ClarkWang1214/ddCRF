import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;


import model.HyperParameters;
import model.SamplerStateTracker;
import model.SamplerState;
import model.Theta;
import model.Posterior;
import sampler.GibbsSampler;
import util.Util;
import Likelihood.DirichletLikelihood;
import Likelihood.Likelihood;
import data.Data;
import test.TestUniform;
import test.Predictor;
import test.TestSample;

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
			double crp_alpha = Double.parseDouble(args[4]);
			System.out.println("Self Linkage Prob is "+alpha);
			HyperParameters h = new HyperParameters(vocab_size, dirichlet, alpha,crp_alpha);
			
			// generate some test samples
			TestUniform test = new TestUniform(10);
			test.generateTestSamples();
			ArrayList<ArrayList<TestSample>> testSamples = test.getTestSamples();

			ArrayList<ArrayList<Double>> list_observations = Data.getObservations();	
			SamplerStateTracker.initializeSamplerState(list_observations);
			Likelihood l = new DirichletLikelihood(h);
			// TODO FIX THIS!!!!!!!
			// l.setTestSamples(new HashSet<TestSample>(testSamples));		

			SamplerStateTracker.max_iter = Integer.parseInt(args[0]);
			System.out.println("Gibbs Sampler will run for "+SamplerStateTracker.max_iter+" iterations.");
			
			// set the output directory based on the parameters
			Util.setOutputDirectoryFromArgs(SamplerStateTracker.max_iter, dirichlet_param, alpha, crp_alpha);
			
			//do sampling		
			long init_time = System.currentTimeMillis();
			for(int i=1;i<=SamplerStateTracker.max_iter;i++)
			{
				long init_time_iter = System.currentTimeMillis();
				GibbsSampler.doSampling(l, i>1);
				System.out.println("----------------------");
				System.out.println("Iteration "+i+" done");
				System.out.println("Took "+(System.currentTimeMillis() - init_time_iter)/(double)1000+" seconds");
				SamplerStateTracker.returnCurrentSamplerState().prettyPrint(System.out);
				double posteriorLogPrior = SamplerStateTracker.returnCurrentSamplerState().getLogPosteriorDensity(l);
				System.out.println("Posterior log density: " + posteriorLogPrior);

				double logLik = l.computeFullLogLikelihood(SamplerStateTracker.returnCurrentSamplerState());
				System.out.println("Log likelihood: " + logLik);
				System.out.println("----------------------");
			}
			
			long diff = System.currentTimeMillis() - init_time; 
			System.out.println("Time taken for Sampling "+(double)diff/1000+" seconds");		
			/*for(int i=0;i<list_observations.size();i++)
				Util.printTableConfiguration(i, new PrintStream("tables/table_configuration"+i+".txt"));*/
			
			//Printing the output csv file
			Util.outputCSVforMap();
	
			// TEMP: just a quick test of theta estimate on the last state

			// SamplerState s = SamplerStateTracker.returnCurrentSamplerState();
			Posterior p = new Posterior(0, h);
			SamplerState sMAP = p.getMapEstimateDensity(l);
			Theta t = new Theta(sMAP, h);
			t.estimateThetas();
			Util.outputTopKWordsPerTopic(t, 15);

			// Run a test
			System.out.println("Running a test");
			for (ArrayList<TestSample> citySamples : testSamples) {
				for (TestSample sample : citySamples) {
					Predictor predictor = new Predictor(p, l, sample);
					System.out.println("  predicted probability of true observation: " + predictor.computeProbabilityForSample());
				}
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

}
