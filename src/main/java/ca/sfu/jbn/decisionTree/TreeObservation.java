package ca.sfu.jbn.decisionTree;

import java.util.Enumeration;
import java.util.Hashtable;

public class TreeObservation {

	private Hashtable<String, Integer> observations = null;
	
	public TreeObservation()
	{
		observations = new Hashtable<String, Integer>();
	}
	
	public TreeObservation(TreeObservation ob)
	{
		Hashtable<String, Integer> obs = ob.getObservations();
		observations = new Hashtable<String, Integer>(obs);
	}
	
	public Hashtable<String, Integer>getObservations()
	{
		return observations;
	}
	
	public void addObservation(String node, Integer value)
	{
		observations.put(node, value);
	}
	
	public Integer observe(String node)
	{
		return observations.get(node);
	}
	
	@Override
	public String toString()
	{
		StringBuffer buf = new StringBuffer();
		buf.append("Observations:");
		Enumeration<String> keys = observations.keys();
		while (keys.hasMoreElements()) {
			String key = keys.nextElement();
			Integer value = observations.get(key);
			buf.append(key + "\t" + value);
		}
		return buf.toString(); 
	}
}
