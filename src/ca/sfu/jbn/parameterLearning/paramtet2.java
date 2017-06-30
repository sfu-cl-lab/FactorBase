package ca.sfu.jbn.parameterLearning;

import java.util.ArrayList;
import java.util.List;

import ca.sfu.jbn.common.GetDataset;
import ca.sfu.jbn.common.db;
import edu.cmu.tetrad.bayes.BayesPm;
import edu.cmu.tetrad.bayes.DirichletBayesIm;
import edu.cmu.tetrad.bayes.MlBayesEstimator;
import edu.cmu.tetrad.data.ColtDataSet;
import edu.cmu.tetrad.data.DiscreteVariable;
import edu.cmu.tetrad.graph.Dag;
import edu.cmu.tetrad.graph.Node;


//Replace this file with joinProbLearner.java

public class paramtet2 {
    public db database ;
    private BayesPm bayePm;
    private Dag dag;
    public DirichletBayesIm dirich;
    
    public paramtet2() throws Exception {
        database = new db();
//        bayePm = bayespm;
        // initialize dirichletBayesIm
//        dirich = DirichletBayesIm.blankDirichletIm(bayespm);
//        RectangularDataSet dataSet = new RectangularDataSet();
//        GraphMAker a = new GraphMAker(
//        "C:/Documents and Settings/hkhosrav/Desktop/newMovieLens.txt");
//        EdgeListGraph graph = a.getGraph();
        DataWrapper dw = new DataWrapper();
//        dw.setSourceGraph(graph);
//        bayePm = makeBayesPm.makepm(graph);
//        dag = bayePm.getDag();
//        dirich = DirichletBayesIm.blankDirichletIm(bayePm);
//        RectangularDataSet dataSet;
//        ColtDataSet dataset =  new ColtDataSet(0, new LinkedList<Node>());
        ArrayList result=database.describeTable("student");
//        System.out.println(result);
        
        List<Node> resultToNodes = new ArrayList<Node> ();
        for(Object s:result){
            DiscreteVariable gn = new DiscreteVariable(s.toString());
            resultToNodes.add(gn);
        }
        
        ColtDataSet dataset =  new ColtDataSet(result.size(), resultToNodes);
//        String columnNames = "";
//        for(Object r:result){
//            columnNames+=r.toString()+',';
//        }
//        columnNames=columnNames.substring(0, columnNames.length()-1);
//        System.out.println(columnNames);
//        ResultSet data = database.selectAll("course");
//        System.out.println(data);
        
        int size=database.countStar("student");
        int i=0;
        for(Object s:result){
        	 ArrayList<String> data = database.getAllRows("student",s.toString());
            for(int j=0;j<size;j++){
            //                Node n =new GraphNode(data.get(j));
//                n.setNodeType(NodeType.NO_TYPE);
                
                dataset.setInt(j, i,Integer.parseInt(data.get(j)));
            }
            i++;
        }

        System.out.println(dataset.toString());
        
//        Knowledge knowledge = new Knowledge();
//        DataParser dParser = new DataParser();
//        dataset = database.JoinRemovePrimaryRelation();
    
//        ArrayList result=database.getAllRows("student","ranking");
//        File f = printToFile(result, "write.txt");
//        dParser.setDelimiter(DelimiterType.COMMA);
//        dataSet = dParser.parseTabular(f);
        
//        MlBayesEstimator  est = new MlBayesEstimator ();
//         BayesIm estimatedIm=est.estimate(bayePm, dataSet);
//        System.out.println(estimatedIm);
        
    //    dirichJoint = DirichletBayesIm.blankDirichletIm(bayePm);
    }

//public  DirichletBayesIm paramterlearning(BayesPm bayesPm){
            // evaluateJoinRequired()
             //iterate over joins
//             RectangularDataSet dataset;
            // DirichletBayesIm temp = new DirichletBayesIm(bayesPm);
            //     dataset = getFromMYSQL(query);
//             dataset = database.JoinRemovePrimaryRelation()
//                 MlBayesEstimator mi;
    //get CPT for one node
    //bayesPM is the whole pm we have, but we only need a bayesPM that corresponds to actual dataset
//                 temp =  (DirichletBayesIm) mi.estimate(bayesPm, dataset);
//             
//             mergfrom     
             
//    return dirich;
//}
        // cd.orderNode();
    
    public DirichletBayesIm computeCPT() {
        MlBayesEstimator mi = new MlBayesEstimator();
        return dirich;
    }
    public static void main(String[] args) {
        try {
         //   paramtet2 pt= new paramtet2();
        	GetDataset gd = new GetDataset();
        //	ColtDataSet dataset = gd.GetData("student");
        	
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
     
 }

}

