package no.ntnu.skyline;

import ifis.skysim2.data.generator.DataGenerator;
import ifis.skysim2.data.generator.DataGeneratorBKS01Anticorrelated;
import ifis.skysim2.data.generator.DataGeneratorBKS01Correlated;
import ifis.skysim2.data.generator.DataGeneratorIndependent;
import ifis.skysim2.simulator.DataSource;

public class Experiment2 extends SkylineExperiment {
	
	public Experiment2(int threadcount, DataGenerator dataGenerator) {
		super();
		config.setNumTrials(10);
		config.setNumberOfCPUs(threadcount);
		config.setDistributedNumBlocks(config.getNumberOfCPUs());
		config.setDataSource(DataSource.MEMORY);
		config.setUseDefaultGeneratorSeed(true);
		config.setD(5);
//		config.setN(1000000);
		config.setN(5000000); // 5M
		config.setDataGenerator(dataGenerator);
	}
	
	public Experiment2(int threadcount) {
		this(threadcount, new DataGeneratorIndependent());
	}
	
	public static void main(String args[]) {
		
		// Input
		if (args.length < 3) {
        	System.err.println("USAGE: CMD ALGORITHM_NAME THREAD_COUNT corr|acorr|indep [ n [ d ]]");
        	System.exit(1);
        }
		String algorithmName = args[0].toLowerCase();
		int threadcount = Integer.parseInt(args[1]);
		String generatorName = args[2].toLowerCase();
		int n = 5000000; // 5M
		int d = 5;
		if (args.length > 3) {
			n = Integer.parseInt(args[3]);
		}
		if (args.length > 4) {
			d = Integer.parseInt(args[4]);
		}
		
		// Prepare data generator
		DataGenerator dg = null;
		if (generatorName.equals("indep")) {
			dg = new DataGeneratorIndependent();
		}
		else if (generatorName.equals("corr")) {
			dg = new DataGeneratorBKS01Correlated();
		}
		else if (generatorName.equals("acorr")) {
			dg = new DataGeneratorBKS01Anticorrelated();
		}
		else if (generatorName.equals("nba")) {
			dg = new FileDataGenerator("../../data/5d-nba-17265.txt");
		}
		else if (generatorName.equals("houses")) {
			dg = new FileDataGenerator("../../data/6d-hou-127931.txt");
		}
		else if (generatorName.equals("zillow")) {
			dg = new FileDataGenerator("../../data/ZillowData.txt");
		}
		else {
			throw new RuntimeException("Data generator must be one of the following: indep, corr, acorr");
		}
		
		// Run algorithm
		SkylineExperiment experiment = new Experiment2(threadcount, dg);
		experiment.config.setN(n);
		experiment.config.setD(d);
		if (algorithmName.equals("pskyline")) {
			APSkylineAlgorithm alg = new APSkylineAlgorithm(new LinearPartitionStrategy());
			experiment.run("PSkyline", alg);
		}
		else if (algorithmName.equals("pskyline_nopart")) {
			experiment.run("PSKyline (no_part)", new PSkylineAlgorithm());
		}
		else if (algorithmName.equals("apskyline")) {
			PartitionStrategy p = new EquiAnglePartitionStrategy(experiment.config.getD(), experiment.config.getDistributedNumBlocks());
			APSkylineAlgorithm alg = new APSkylineAlgorithm(p);
			experiment.run("APSkyline", alg);
		}
		else if (algorithmName.equals("ppskyline")) {
			PartitionStrategy p = new ParallelAnglePartitionStrategy(experiment.config.getD(), experiment.config.getDistributedNumBlocks());
			APSkylineAlgorithm alg = new APSkylineAlgorithm(p);
			experiment.run("PPSkyline", alg);
		}
		else if (algorithmName.equals("dapskyline")) {
			PartitionStrategy p = new DynamicAnglePartitionStrategy();
			APSkylineAlgorithm alg = new APSkylineAlgorithm(p);
			experiment.run("DAPSkyline", alg);
		}
		else if (algorithmName.equals("adapskyline")) {
			PartitionStrategy p = new DynamicPlusPartitioningStrategy();
			APSkylineAlgorithm alg = new APSkylineAlgorithm(p);
			experiment.run("APSSampleDynamicFair", alg);
		}
		else if (algorithmName.equals("ppskyline2")) {
			PartitionStrategy p = new ParallelAnglePartitionStrategy(experiment.config.getD(), experiment.config.getDistributedNumBlocks(), 1.15);
			APSkylineAlgorithm alg = new APSkylineAlgorithm(p);
			experiment.run("PPSkyline2", alg);
		}
		else if (algorithmName.equals("bnl")) {
			experiment.run("ParallelBNL", new ParallelBNLAlgorithm());
		}
		else if (algorithmName.equals("bbs")) {
			experiment.run("BBS", new BBSAlgorithm());
		}
		else {
			throw new RuntimeException("Invalid algorithm name");
		}
	}
}
