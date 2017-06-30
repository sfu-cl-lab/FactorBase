package ca.sfu.jbn.frequency;
import java.util.ArrayList;
import ca.sfu.jbn.common.*;
import edu.cmu.tetrad.bayes.BayesIm;
import edu.cmu.tetrad.graph.Node;
/***********************************
 * QueryGenerator will generate random queries from the
 * given database (in the global class).
 * 
 * It can also get the frequencies from the database using
 * SQL queries
 * 
 */

public class CopyOfQueryGenerator {
	
	private BayesIm tempBayesIm;

	public CopyOfQueryGenerator(){
		tempBayesIm = new BayesProbCounter().learnBayesModel();
	}
	
	public CopyOfQueryGenerator(BayesIm newBayesIm){
		tempBayesIm = newBayesIm;
	}
	

	// The execution methods:


	public Query generate(int newQueryNum){
		Query q=new Query();
		//BayesIm tempBayesIm = new BayesProbCounter().learnBayesModel();
		//int numnode=10;
		int numnode=tempBayesIm.getNumNodes();
		//1.for the case that only 1 query node
		
		//choose number of evidence
		//int eNodeNum=(int)(Math.random()*numnode);
		
		int eNodeNum=newQueryNum;
		
		int nodeIndex[]=new int [eNodeNum+1];
		q.evidenceNum=eNodeNum;
		boolean flag[] = new boolean[numnode+1];
		
		for(int i=0;i<eNodeNum+1;i++){
			nodeIndex[i]=0;
			flag[i]=false;
		}
		for(int i=0;i<eNodeNum+1;i++){
			int newNo = (int)(Math.random()*numnode);
			while (flag[newNo]){
				newNo = (int)(Math.random()*numnode);
			}
			nodeIndex[i]= newNo;
			flag[newNo]=true;
	
		}
		
		//query node index is the first in the Index array
		int qNodeIndex=nodeIndex[0];
		//System.out.println(qNodeIndex);
		Node qNode=tempBayesIm.getNode(qNodeIndex);
		//get the name of the query node
		String qNodeName = qNode.getName();
		q.queryName=qNodeName;
		//System.out.println(qNodeName);
		//get number of value for query node
		int qValueNum=tempBayesIm.getNumColumns(qNodeIndex);
        q.queryValNum=qValueNum;
        q.queryValue=new ArrayList<String>();
		for(int i=0;i<qValueNum;i++){
			String qValue=tempBayesIm.getBayesPm().getCategory(qNode,i);
			if (qValue.equals("*")||qValue.equals(global.theChar)) {
				q.queryValNum--;
				continue;
			}
			//System.out.println(qValue);
			q.queryValue.add(qValue);
		}

		//evidence node
		StringBuffer evidence=new StringBuffer();
		q.evidenceName=new ArrayList<String>();
		q.evidenceValue=new ArrayList<String>();
	    for(int i=0;i<q.evidenceNum;i++){
	    	boolean valid = true;
	    	Node eNode=tempBayesIm.getNode(nodeIndex[i+1]);
			String eNodeName = eNode.getName();
			q.evidenceName.add(eNodeName);
			evidence.append(eNodeName);
			evidence.append(" with value: ");
			int eValueNum=tempBayesIm.getNumColumns(nodeIndex[i+1]);
			
			int eValueIndex=(int)(Math.random()*eValueNum);
			String eValue=tempBayesIm.getBayesPm().getCategory(eNode, eValueIndex);
			
			while (eValue.equals("*")||eValue.equals(global.theChar)) {
				eValueIndex=(int)(Math.random()*eValueNum);
				eValue=tempBayesIm.getBayesPm().getCategory(eNode, eValueIndex);
			}
						
			q.evidenceValue.add(eValue);
			evidence.append(eValue);
			evidence.append(" ");
	    }
	    String querynode="Query Node: "+q.queryName+" ";
	    String evidencequery="Evidence Node:"+evidence;
		String testquery="The query is: "+querynode+" and "+evidencequery;
		//System.out.println(testquery);
		

		return q;
		

	}
	public static void main(String[] args){

		for (int i = 0; i< 20;i++){
			Query query=new CopyOfQueryGenerator().generate(3);
			query.print();
		}
		
		//int x = (int)(Math.random()*10);
		//double x1 = Math.random();
		//int x1 = (int)(Math.random()*10);
		//int x2 = (int)(Math.random()*10);
		//double x2 = Math.random();
		//System.out.println(x);
		//System.out.println(x1);
		//System.out.println(x2);
	}

	/***********************
		The information retrieve methods:		
	 *********************/

	/*****************************************************************
	ALL PRIVATE METHODS WRITE AFTER THIS:	
	 *****************************************************************/

}
