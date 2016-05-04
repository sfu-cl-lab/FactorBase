/* July 3rd, 2014, zqian
 * input: 
 * @database@ (original data based on ER diagram)
 * output: 
 * @database@_BN (e.g. _CP, Path_BayesNets, Score)
 * 			@database@_CT (e.g. BiggestRchain_CT)
 * 			@database@_setup (preconditions for learning)
 * 
 * */
public class RunBB {
	static String opt1;
	
	public static void main(String[] args) throws Exception {
		long t1 = System.currentTimeMillis(); 
		System.out.println("Start Program...");
		setVarsFromConfig();
		if (opt1.equals("1")) {
			MakeSetup.runMS();
			System.out.println("Setup database is ready.");
		} else {
			System.out.println("Setup database exists.");
		}
		runBBLearner();
		
		long t2 = System.currentTimeMillis(); 
		System.out.println("Total Running time is " + (t2-t1) + "ms.");
	}
	
	
	public static void setVarsFromConfig(){
		Config conf = new Config();
		//1: run Setup; 0: not run
		opt1 = conf.getProperty("AutomaticSetup");
	}
	
	public static void runBBLearner() throws Exception {
		
		//assumes that dbname is in config file and that dbname_setup exists.

		BayesBaseCT_SortMerge.buildCT();
		System.out.println("The CT database is ready for use.");
		System.out.println("*********************************************************");
		CSVPrecomputor.runCSV();
		System.out.println("CSV files are generated.");
		System.out.println("*********************************************************");
		BayesBaseH.runBBH();
		//System.out.println("\nFinish running BayesBaseH.");

	}

}
