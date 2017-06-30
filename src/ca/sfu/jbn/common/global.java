package ca.sfu.jbn.common;
import edu.cmu.tetrad.bayes.DirichletBayesIm;


public class global{
    public static boolean regular;
    public static boolean structure;
    public static boolean pruning;
    public static boolean doesContainFalse = false;
  //    public static boolean muta = false;
//    public static boolean muta_subsample1 = true;
//    public static boolean muta_subsample2 = false;
//    public static boolean financial = false;
//    public static boolean movieLens = false;
//    public static boolean movieLens_subsample1 = false ;
//    public static boolean movieLens_subsample2 =false;
//    public static boolean imdb = false;
//    public static boolean cora = false;
//    public static boolean hepatit = false;
//    public static boolean university = false;
//    public static boolean university_original = false;
//    public static boolean finan = false;
//    public static boolean hepForHas =false;
    
    public static DirichletBayesIm trainedBayes;
    
 //  public static String schema="MovieLens";
   // public static String schema = "";
//    public static String schema = "hep";
//    public static String schema = "muta_subsample1";
//    public static String schema = "Muta";
//    public static String schema = "university_original";
    
//    public static String schema = "university"; 

//    public static String schema = "hepatit";
//    public static String schema = "muta";
//    public static String schema = "fin";

    public static String schema = "unielwin" ;
    public static String dbURL="jdbc:mysql://kripke.cs.sfu.ca/";
    public static String dbUser="hassan";
    public static String dbPassword="joinBayes";
    public static db dataBase;
    public static String WorkingDirectory = global.schema;
    public static String XMLFile=global.schema+"/relation.xml";
    public static String getClassTables(){
        return"";
    }    
    
    public static String theChar="_|_";
    
	public static void initialize(){
	    WorkingDirectory = global.schema;
        XMLFile=global.schema+"/relation.xml";
        dataBase= new db();
    }
    
}