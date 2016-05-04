package ca.sfu.jbn.frequency;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import ca.sfu.jbn.common.Parser;
import ca.sfu.jbn.common.global;
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

public class QueryGenerator {

	private BayesIm tempBayesIm;
	private boolean relationSwitch = false;
	private Parser parser = Parser.getInstance();


	public QueryGenerator(){
		Parser.initialize();
		tempBayesIm = new BayesProbCounter().learnBayesModel();
	}

	public QueryGenerator(BayesIm newBayesIm){
		Parser.initialize();
		tempBayesIm = newBayesIm;
	}
	
	public QueryGenerator(boolean flag){
		Parser.initialize();
		tempBayesIm = new BayesProbCounter().learnBayesModel();
		relationSwitch = flag;
	}

	public QueryGenerator(BayesIm newBayesIm, boolean flag){
		Parser.initialize();
		tempBayesIm = newBayesIm;
		relationSwitch = flag;
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

		int queryNewNo = (int)(Math.random()*numnode);
		String queryNewName = tempBayesIm.getNode(queryNewNo).getName();


		// Relation Judge!

		if (relationSwitch){
			while (!isRelation(queryNewName)){
				queryNewNo = (int)(Math.random()*numnode);
				queryNewName = tempBayesIm.getNode(queryNewNo).getName();			
			}
		}
		else {
			while (isRelation(queryNewName)){
				queryNewNo = (int)(Math.random()*numnode);
				queryNewName = tempBayesIm.getNode(queryNewNo).getName();			
			}
		}

		nodeIndex[0]= queryNewNo;
		flag[queryNewNo]=true;

		for(int i=1;i<eNodeNum+1;i++){
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
			if (qValue.equals(global.theChar)) {
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


	public static ArrayList<Query> QueryReader(String FileName){
		ArrayList<Query> queryList=new ArrayList<Query>();

		try { 

			File file = new File(FileName);
			Scanner input = new Scanner(file);

			int N = input.nextInt();
			input.nextLine();
			for(int i = 0 ; i < N; i++){
				Query q = new Query();
				q.queryName = input.next();
				q.queryValNum= input.nextInt();
				input.nextLine();
				for(int j = 0; j < q.queryValNum; j++){
					String s = input.next();
					//System.out.println(j+" "+s);
					q.queryValue.add(s);				
				}
				q.evidenceNum=input.nextInt();
				input.nextLine();
				for(int j = 0 ; j < q.evidenceNum; j++){
					String s = input.next();
					//System.out.println(j+" "+s);
					q.evidenceName.add(s);
				}
				for(int j = 0 ; j < q.evidenceNum; j++){
					String s = input.next();
					//System.out.println(j+" "+s); 
					q.evidenceValue.add(s);
				}
				queryList.add(q);
			}
		}
		catch (IOException e) { 
			e.printStackTrace();  
		}  
		return queryList;

	}


	public static void QueryWriter(String dbName,Query query){
		try {  
			FileOutputStream outStream = new FileOutputStream(dbName,true); 
			PrintWriter outputStream = new PrintWriter(outStream);

			outputStream.println(query.queryName+" "+query.queryValNum);

			String temp = "";
			for(String s : query.queryValue) temp = temp+s+" ";
			outputStream.println(temp.substring(0, temp.length()-1));

			outputStream.println(query.evidenceNum);

			temp="";
			for(String s : query.evidenceName) temp = temp+s+" ";
			outputStream.println(temp.substring(0, temp.length()-1));

			temp="";
			for(String s : query.evidenceValue) temp = temp+s+" ";
			outputStream.println(temp.substring(0, temp.length()-1));

			outputStream.close();

		} catch (IOException e) {  
			e.printStackTrace();  
		}  

	}

	public static void NumWriter(String fileName,int QueryNum){
		try { 
			FileOutputStream outStream = new FileOutputStream(fileName); 
			PrintWriter outputStream = new PrintWriter(outStream);
			outputStream.println(QueryNum);   
			outputStream.close();
		}
		catch (IOException e) { 
			System.out.println("Can't write to file");
			e.printStackTrace();  
		}  
	}

	public static void main(String[] args){

		int QueryNum = 100;
		
		global.schema="fin";
		global.initialize();
		
		String Filename = "fin_0.txt";
		QueryGenerator.NumWriter(Filename, QueryNum);

		QueryGenerator qGen = new QueryGenerator(false);

		for (int i = 0; i< QueryNum;i++){
			int EviNodeNum=(int)(Math.random()*3)+1;

			Query query=qGen.generate(EviNodeNum);
			QueryGenerator.QueryWriter(Filename,query);
		}

		ArrayList<Query> qList=QueryGenerator.QueryReader(Filename);
		for(int j=0;j<qList.size();j++){
			qList.get(j).printAll();
		}

		//System.out.println(new QueryGenerator().isRelation("B(RA)"));
	}

	/**
	 * @param	a string of a table name
	 * @return	True if it is a relation table; False otherwise
	 */
	private boolean isRelation(String s)
	{

		if (s.startsWith("B(")) s=s.substring(2,s.length()-1);
		List<String> l = parser.getRelations();
		List<List<String>> l2 = parser.getRelation_att();
		for(List<String> templ : l2){
			for(String temps : templ){
				l.add(temps);				
			}			
		}
		//System.out.println(l);
		return l.contains(s);
	}

}
