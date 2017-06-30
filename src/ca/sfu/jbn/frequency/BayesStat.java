package ca.sfu.jbn.frequency;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ca.sfu.jbn.common.Parser;
import ca.sfu.jbn.common.db;
import ca.sfu.jbn.common.global;
import edu.cmu.tetrad.bayes.BayesIm;

/**
 * 
 * @author	Yuke Zhu
 * @version	1.0
 * @since	Jan 16, 2012
 * 
 */
public class BayesStat {

	BayesIm finalIm = null;
	Parser parser = null;
	db database = null;
	
	List<String> predicates = null;

	/**
	 * Initializer for BayesStat
	 */
	public BayesStat()
	{
		finalIm = new BayesProbCounter().learnBayesModel();
		parser = Parser.getInstance();
		database = db.getInstance();
	}

	/**
	 * Initializer for BayesStat with a new BayesIm
	 */
	public BayesStat(BayesIm newIm)
	{
		finalIm = newIm;
		parser = Parser.getInstance();
		database = db.getInstance();
	}

	/**
	 * Initializer for BayesStat with a new BayesIm
	 */
	public BayesStat(BayesIm newIm,Parser newparser, db newdatabase)
	{
		finalIm = newIm;
		parser = newparser;
		database = newdatabase;
	}


	/**
	 * Calculate the Whole Set of Probability for a Row P(cName = * | queryName = queryValue)
	 * @param	Query
	 * @return	A List of Probabilities 
	 */
	public List<Double> getRowProbabilities(Query q)
	{
		List<Double> ans = new ArrayList<Double>();
		for(String value : q.queryValue)
		{
			double prob = getConditionalProbability(q.queryName, value, q.evidenceName, q.evidenceValue);
			ans.add(prob);
		}
		return ans;
	}

	/**
	 * Count the rows in database for given evidence
	 * @exception	IllegalArgumentException cannot count when there are more than 1 false relation!
	 * @param		queryName, queryValue
	 * @return		The number of rows that match the query 
	 */
	public int getBigJoinCount(List<String> aQueryName, List<String> aQueryValue)
	{
		List<String> queryTable = new ArrayList<String>();
		List<String> queryName = new ArrayList<String>(aQueryName);
		List<String> queryValue = new ArrayList<String>(aQueryValue);

		int size = queryName.size();
		int relationPos = -1;
		for(int i=0; i<size; i++)
		{
			String name = queryName.get(i);
			if (name.startsWith("B(")) {
				name = name.substring(2,name.length()-1);
				queryName.set(i,name);
			}
			/* Detect negative relations */
			if(isRelationTable(name) && queryValue.get(i).equals("false"))
			{
				if(relationPos != -1)
				{
					throw new java.lang.IllegalArgumentException("Multiple false relations detected.");
				} else
				{
					relationPos = i;
				}
			}
		}

		if(relationPos == -1) 
		{
			Set<String> relationRefTable = new HashSet<String>();
			List<String> relationTables = new ArrayList<String>();

			if(queryName.size() == 0) return 0;

			for(int i=0; i<size; i++)
			{
				String name = queryName.get(i);
				if(isRelationTable(name))
				{
					relationTables.add(name);
					queryTable.add(name);
					List<String> relationRefTables = getRelationRefTable(name);
					for(String refTable : relationRefTables)
						relationRefTable.add(refTable);
					relationRefTable.add(name);
				} else
				{
					String table = getTableForAttribute(name);
					queryTable.add(table);
					if(isRelationTable(table))
					{
						relationTables.add(table);
						List<String> relationRefTables = getRelationRefTable(table);
						for(String refTable : relationRefTables)
							relationRefTable.add(refTable);
					}
					relationRefTable.add(table);
				}
			}

			/* SQL Query Generator */
			String from = "";
			String where = "1";
			/* Making Propositions */
			for(int i=0; i<size; i++)
			{
				if(isRelationTable(queryName.get(i))) continue;
				where += " and " + queryName.get(i) + " = " + "\'" + queryValue.get(i) + "\'";
			}
			/* Natural Join */
			for(String refTable : relationRefTable)
				from += " natural JOIN " + refTable;
			from = from.substring(14);
			int numerator = database.count(from, where);

			//			System.out.println(from + "\n" + where);
			//			System.out.println(numerator);

			return numerator;
		}else
		{
			double p1, p2;
			int s;
			/* False relation probability */
			List<String> pName1 = new ArrayList<String>(queryName);
			List<String> pValue1 = new ArrayList<String>(queryValue);
			p1 = getProbability(pName1, pValue1);
			/* True relation probability */
			List<String> pName2 = new ArrayList<String>(queryName);
			List<String> pValue2 = new ArrayList<String>(queryValue);;
			pValue2.set(relationPos, "true");
			p2 = getProbability(pName2, pValue2);
			/* True relation count */
			s = getBigJoinCount(pName2, pValue2);
			return (int) ((p1 / p2) * s);
		}
	}

	/**
	 * Calculate the Conditional probability P(cName = cValue | queryName = queryValue)
	 * @param	cName, cValue, queryName, queryValue
	 * @return	the conditional probability value
	 */
	public double getConditionalProbability(String cName, String cValue, List<String> queryName, List<String> queryValue)
	{
		List<String> kQueryName = new ArrayList<String>(queryName);
		List<String> kQueryValue = new ArrayList<String>(queryValue);
		double p1 = getProbability(new ArrayList<String>(queryName), new ArrayList<String>(queryValue));
		kQueryName.add(cName);
		kQueryValue.add(cValue);
		if (cValue.equals("*")) return 0;
		double p2 = getProbability(kQueryName, kQueryValue);
		return p2 / p1;
	}

	/**
	 * Main Entry to get joint probability
	 * @param	queryName	strings of attributes as left-value of propositions
	 * 			queryValue	strings of values as the right-value of propositions
	 * @return	the probability of the propositions
	 */
	public double getProbability(List<String> queryName, List<String> queryValue)
	{
		int size = queryName.size();
		int relationPos = -1;
		for(int i=0; i<size; i++)
		{
			String name = queryName.get(i);
			if (name.startsWith("B(")) {
				name = name.substring(2,name.length()-1);
				queryName.set(i,name);
			}
			/* Detect negative relations */
			if(isRelationTable(name) && queryValue.get(i).equals("false")) 
				relationPos = i;
		}
		if(relationPos == -1) 
		{
			return getProbabilityWithPositionRelation(queryName, queryValue);
		}else
		{
			double p1, p2;
			List<String> pName0 = new ArrayList<String>(queryName);
			List<String> pValue0 = new ArrayList<String>(queryValue);
			pName0.remove(relationPos);
			pValue0.remove(relationPos);
			p1 = getProbability(pName0, pValue0);

			List<String> pName1 = new ArrayList<String>(queryName);
			List<String> pValue1 = new ArrayList<String>(queryValue);;
			pValue1.set(relationPos, "true");
			p2 = getProbability(pName1, pValue1);

			return (p1 - p2);
		}
	}

	/**
	 * Relation with False value is not acceptable for this function
	 * @param	queryName	strings of attributes as left-value of propositions
	 * 			queryValue	strings of values as the right-value of propositions
	 * @return	the probability of the propositions
	 */
	private double getProbabilityWithPositionRelation(List<String> aQueryName, List<String> aQueryValue)
	{
		List<String> queryTable = new ArrayList<String>();
		List<String> queryName = new ArrayList<String>(aQueryName);
		List<String> queryValue = new ArrayList<String>(aQueryValue);

		Set<String> relationRefTable = new HashSet<String>();
		List<String> relationTables = new ArrayList<String>();

		if(queryName.size() == 0) /* No proposition */
			return 1.0;

		int size = queryName.size();
		int numerator = 1, denominator = 1;

		for(int i=0; i<size; i++)
		{
			String name = queryName.get(i);
			if(isRelationTable(name))
			{
				relationTables.add(name);
				queryTable.add(name);
				List<String> relationRefTables = getRelationRefTable(name);
				for(String refTable : relationRefTables)
					relationRefTable.add(refTable);
				relationRefTable.add(name);
			} else
			{
				String table = getTableForAttribute(name);
				queryTable.add(table);
				if(isRelationTable(table))
				{
					relationTables.add(table);
					List<String> relationRefTables = getRelationRefTable(table);
					for(String refTable : relationRefTables)
						relationRefTable.add(refTable);
				}
				relationRefTable.add(table);
			}
		}

		/* Calculate the Size of Sample Space */
		for(String table : relationRefTable)
		{
			if(!relationTables.contains(table))
			{
				int k = database.count(table, "1");
				denominator *= k;
			}
		}

		/* SQL Query Generator */
		String from = "";
		String where = "1";
		/* Making Propositions */
		for(int i=0; i<size; i++)
		{
			if(isRelationTable(queryName.get(i))) continue;
			where += " and " + queryName.get(i) + " = " + "\'" + queryValue.get(i) + "\'";
		}
		/* Natural Join */
		for(String refTable : relationRefTable)
			from += " natural JOIN " + refTable;

		from = from.substring(14);
		numerator *= database.count(from, where);

		return (1.0 * numerator) / denominator;
	}


	public String getUnitClauseString()
	{
		int nodeNum = finalIm.getNumNodes();
		String output = "";
		
		String filename = global.WorkingDirectory + "/" + global.schema + "predicate_temp.mln";
		BufferedReader in;
		try {
			in = new BufferedReader(new FileReader(filename));
			predicates = new ArrayList<String>();
			String s = in.readLine();
			while (s != null) {
				if (s.startsWith("//")) {
					continue;
				}
				predicates.add(s);
				s = in.readLine();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		for(int i=0; i<nodeNum; i++)
		{
			
			int colNum = finalIm.getNumColumns(i);
			String nodeName = finalIm.getNode(i).getName();
			if (nodeName.contains("_dummy")) continue;
			List<String> domain = new ArrayList<String>();
			for (int k = 0; k < colNum; k++) {
				String value = finalIm.getBayesPm().getCategory(finalIm.getNode(i), k);
				if(value.equals(global.theChar) || value.equals("*")) continue;
				domain.add(value);
			}
			List<String> queryName = new ArrayList<String>(), queryValue = new ArrayList<String>();
			List<String> valueName = new ArrayList<String>();
			List<Integer> valueCnt = new ArrayList<Integer>();
			queryName.add(nodeName);
			int sum = 0, cnt = 0, numValues = domain.size();
			for (int j=0; j<numValues; j++)
			{
				String value = domain.get(j);
				queryValue.add(value);
				String table = parser.getTableofField(finalIm.getNode(i));
				if(table != null && !nodeName.startsWith("B("))
				{
					cnt = database.count(table, nodeName + "= '" + value + "'");
					sum += cnt;
					valueName.add(value);
					valueCnt.add(cnt);
				}
				queryValue.remove(value);
			}
			numValues = valueName.size();
			for (int j=0; j<numValues; j++)
			{
				double p = 1.0 * valueCnt.get(j) / sum; 
				p = (p + 0.01) * 100 / (100 + numValues);
				double weight = Math.log(p);
				NumberFormat formatter = new DecimalFormat("0.0000000000");
				
				String number = formatter.format(weight);
				
				String primaryKey = findSentence(i);
				
				output = output + number + " " + nodeName + "(" + primaryKey + ", " 
						+ nodeName.toUpperCase() + "_" + valueName.get(j) 
						+ ")" + "\n";
			}
		}
//		System.out.println(output);
		return output;
	}
	
	private String findSentence(int nodeIndex) {
		String sentence = null;
		String name = finalIm.getNode(nodeIndex).getName();

		boolean isDummy=false;
		//if this is dummy variable
		if(name.contains("_dummy")){
			isDummy=true;
			//get rid of dummy
			name=name.replaceAll("_dummy","");

		}
		name = name.charAt(0)
		+ name.subSequence(1, name.length()).toString();
		for (String s : predicates) {
			if (s.contains(name + "(")) {
				if(isDummy){
					//locate the name of the primary key we need to take care of
					String primaryKey = Parser.getInstance().getPrimaryKeyForAttr(name);
					s=s.replaceAll(primaryKey+"_inst", primaryKey+"_dummy"+"_inst");
				}
				String temp = s.substring(s.indexOf('(')+1);
				temp = temp.substring(0,temp.indexOf(','));
				return temp;
			}
		}
		return sentence;
	}

	public static void main(String[] args) throws SQLException, IOException
	{
		/* Test UnitClause API */ 
		BayesProbCounter counter = new BayesProbCounter();
		BayesIm finalIm = counter.learnBayes();

		BayesStat stat = new BayesStat(finalIm);
		System.out.println(stat.getUnitClauseString());
	}

	/**
	 * @param	a string of a table name
	 * @return	True if it is a relation table; False otherwise
	 */
	private boolean isRelationTable(String s)
	{
		List<String> l = parser.getRelations();
		return l.contains(s);
	}

	/**
	 * @param	a string of a relation table name
	 * @return	a list of referenced tables for the relation table
	 */
	@SuppressWarnings("unchecked")
	private List<String> getRelationRefTable(String s)
	{
		return parser.getRefEntities(s);
	}

	/**
	 * @param	a string of an attribute
	 * @return	a string of table where the attribute locates
	 */
	private String getTableForAttribute(String attr)
	{
		return parser.getTableOfField(attr);
	}
}
