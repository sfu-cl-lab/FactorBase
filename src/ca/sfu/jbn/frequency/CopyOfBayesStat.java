package ca.sfu.jbn.frequency;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ca.sfu.jbn.common.Parser;
import ca.sfu.jbn.common.db;
import edu.cmu.tetrad.bayes.BayesIm;

/**
 * 
 * @author	Yuke Zhu
 * @version	1.0
 * @since	Jan 16, 2012
 * 
 */
public class CopyOfBayesStat {

	BayesIm finalIm = null;
	Parser parser = null;
	db database = null;

	/**
	 * Initializer for BayesStat
	 */
	public CopyOfBayesStat()
	{
		finalIm = new BayesProbCounter().learnBayesModel();
		parser = Parser.getInstance();
		database = db.getInstance();
	}

	/**
	 * Initializer for BayesStat with a new BayesIm
	 */
	public CopyOfBayesStat(BayesIm newIm)
	{
		finalIm = newIm;
		parser = Parser.getInstance();
		database = db.getInstance();
	}

	/**
	 * Initializer for BayesStat with a new BayesIm
	 */
	public CopyOfBayesStat(BayesIm newIm,Parser newparser, db newdatabase)
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
			
//			System.out.println(pName0);
//			System.out.println(pValue0);
//			System.out.println("p1: "+p1);
//			
//			System.out.println(pName1);
//			System.out.println(pValue1);
//			System.out.println("p2: "+p2);
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
		
//		System.out.println(queryName);
//		System.out.println(queryValue);
//		System.out.println(relationRefTable);

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
//			if(queryValue.get(i).equals(global.theChar)) continue;
			where += " and " + queryName.get(i) + " = " + "\'" + queryValue.get(i) + "\'";
		}
		/* Natural Join */
		for(String refTable : relationRefTable)
			from += " natural JOIN " + refTable;
		
		from = from.substring(14);
		numerator *= database.count(from, where);
		
//		System.out.println(from + "\n" + where);
//		System.out.println(numerator + " / " + denominator);

		return (1.0 * numerator) / denominator;
	}

	public static void main(String[] args)
	{
		CopyOfBayesStat  stat = new CopyOfBayesStat();
		/* Construct a Query */
		Query query = new Query();
		query.evidenceName = new ArrayList<String>();
		query.evidenceValue = new ArrayList<String>();

//		query.evidenceName.add("registration");
//		query.evidenceValue.add("false");
//		query.evidenceName.add("RA");
//		query.evidenceValue.add("false");

		query.addE("moleatm","true");
		query.addE("bond","false");
		query.addE("logp","0");
		query.addE("elem","n");
		
		query.evidenceNum = 4;

		query.queryName = "charge";
		query.queryValNum = 2;
		query.queryValue = new ArrayList<String>();
		query.queryValue.add("0");
		query.queryValue.add("1");
		
		System.out.println(stat.getRowProbabilities(query));
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
