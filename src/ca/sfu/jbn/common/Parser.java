package ca.sfu.jbn.common;

import java.io.*;
import java.util.ArrayList;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.*;
import java.util.List;


//import edu.cmu.tetrad.graph.GraphNode;
//import edu.cmu.tetrad.graph.Node;

/**
 * <p>
 * Title: XML Parser
 * </p>
 * <p>
 * Description: This program is intended to parse XML documents!
 * </p>
 * <p>
 * Copyright: Copyright (c) 2007
 * </p>
 * <p>
 * Company: UT
 * </p>
 * 
 * @author not attributable
 * @version 1.0
 */
public class Parser
{

	private ArrayList<String> relation = new ArrayList<String>();
	// each element of this List is an arraylist of entities of corresponding
	// index of relation
	private ArrayList refEntities = new ArrayList();
	private ArrayList OneEntity_att = new ArrayList();
	private ArrayList OneRelation_att = new ArrayList();

	private ArrayList<String> entity = new ArrayList<String>();
	// each element of this List is an arraylist of entities of corresponding
	// index of relation
	private ArrayList entities_id = new ArrayList();
	private ArrayList relation_att = new ArrayList();
	private ArrayList entity_att = new ArrayList();
	

	public ArrayList cat;
	private String PARSEFile =global.WorkingDirectory + "/relation.xml";
	private List<String> correlatedEntities = new ArrayList<String>();

	
	
	public List<String> getCorrelatedEntities() {
		return correlatedEntities;
	}
	
	public String getPrimaryKeyForAttr(String attr){
		String entity =getTableOfField(attr);
		return getEntityPrimaryKey(entity).get(0);
		
	}

	public void setParseFile(String name){
		PARSEFile = global.WorkingDirectory+ "/"+name;
		makePrser();
	}
	
	public Parser() {
		PARSEFile = global.WorkingDirectory+ "/relation.xml";
		makePrser();
	}
	
	private static Parser parser = new Parser();
	
	public static Parser getInstance(){
		return parser;
		
	}
	
	public static void setInstance(Parser p){
		parser=p;
	}

	public void makePrser() {
		relation = new ArrayList();
		refEntities = new ArrayList();
		OneEntity_att = new ArrayList();
	    entity = new ArrayList();
	    entities_id = new ArrayList();
	    relation_att = new ArrayList();
	    entity_att = new ArrayList();
	    
		ArrayList res;
		ArrayList res1;
 // temp
		if (global.WorkingDirectory == null)
			PARSEFile = "relation.xml";
		try {
			FileInputStream f = new FileInputStream(PARSEFile);

			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(new InputSource((new InputStreamReader(f, "utf-8"))));

			Node root = document.getDocumentElement();
			
			
			NodeList rootChildNodes = root.getChildNodes();
			Node currentNode;
			

			for (int i = 0; i < rootChildNodes.getLength(); i++) {
				currentNode = rootChildNodes.item(i);
				if (currentNode.getNodeName().equals("relation")) {
					res = new ArrayList();
					res1 = new ArrayList();
					NodeList docRoot = currentNode.getChildNodes();
					for (int j = 0; j < docRoot.getLength(); j++) {
						String field = currentNode.getChildNodes().item(j).getNodeName(); 
						if (field.equals("name")) {
							String nameOfRelation = (currentNode.getChildNodes().item(j)).getTextContent().toString();
							relation.add(nameOfRelation);
						} else if (field.equals("ref_entity")) {
							String nameOfEntity = currentNode.getChildNodes().item(j)
									.getTextContent().toString();
							res.add(nameOfEntity);
						}
						 else if (field.equals("rel_att")){
						 String nameofAtt =
						 currentNode.getChildNodes().item(j).getTextContent().toString();
						 res1.add(nameofAtt);
						 }
					}
						refEntities.add(res); 
						relation_att.add(res1);
					}
					if (currentNode.getNodeName().equals("entity")) {
						res = new ArrayList();
						res1 = new ArrayList();
						NodeList docRoot2 = currentNode.getChildNodes();
						for (int j = 0; j < docRoot2.getLength(); j++) {
							String field = currentNode.getChildNodes().item(j).getNodeName();
							if (field.equals("entity_name")) {
								String nameOfEntity = currentNode.getChildNodes().item(j)
										.getTextContent().toString();
								entity.add(nameOfEntity);
								if(nameOfEntity.contains("_dummy")){
									correlatedEntities.add(nameOfEntity);
									
								}
							} else if (field.equals("entity_id")) {
								String IdofEntity = currentNode.getChildNodes().item(j)
										.getTextContent().toString();
								res.add(IdofEntity);
							} else if (field.equals("entity_att")) {
								String AttofEntity = currentNode.getChildNodes().item(j)
								.getTextContent().toString();
						res1.add(AttofEntity);
								
							}
						}
						entities_id.add(res);
						entity_att.add(res1);
					}

				}
			
		} catch (IOException ex) {
			System.err.println("File IO Error");
			ex.printStackTrace();
		} catch (SAXException ex) {
			System.err.println("2");
		} catch (ParserConfigurationException ex) {
			System.err.println("3");
		} catch (FactoryConfigurationError ex) {
			System.err.println("4");
		}

		
	}

	// ////////////////////////////////////////////////
	public String getTAbleofField(String field)
	{
		ArrayList OneEntity_att = new ArrayList();
		// ArrayList OneRelation_att = new ArrayList();

		for (int i = 0; i < entity.size(); i++) {
			// OneEntity_att = parser.getEntity_att(i);
			//OneEntity_att = entity.get(i);
			String tempEntity = entity.get(i);
			OneEntity_att =  (ArrayList)entity_att.get(i);
			if (OneEntity_att.contains(field)) {
				return entity.get(i).toString();
			}
		}

		for (int i = 0; i < relation.size(); i++) {
			OneRelation_att = getRel_att(i);
			if (OneRelation_att.contains(field)) {
				return relation.get(i).toString();
			}
		}
		for (int i = 0; i < relation.size(); i++) {
				if (relation.contains(field)) {
				return field;
			}
		}
		return null;
	}


	
	// //////////////////////////////////////////////////
	public String getTAbleofField(edu.cmu.tetrad.graph.Node node1)
	{
		ArrayList OneEntity_att = new ArrayList();
		// ArrayList OneRelation_att = new ArrayList();

		for (int i = 0; i < entity.size(); i++) {
			// OneEntity_att = parser.getEntity_att(i);
			//OneEntity_att = entity.get(i);
			String tempEntity = entity.get(i);
			OneEntity_att =  (ArrayList)entity_att.get(i);
			if (OneEntity_att.contains(node1.getName().toString())) {
				return entity.get(i).toString();
			}
		}

		for (int i = 0; i < relation.size(); i++) {
			OneRelation_att = getRel_att(i);
			if (OneRelation_att.contains(node1.getName().toString())) {
				return relation.get(i).toString();
			}

		}
		return null;
	}

	// ////////////////////////////////////////////////
	public ArrayList getEntities()
	{
		return entity;
	}

	// ////////////////////////////////////////////////
	public ArrayList<String> getRelations()
	{
		return relation;
	}

	// ////////////////////////////////////////////////
	public ArrayList getRelation_att()
	{
		return relation_att;
	}

	// ////////////////////////////////////////////////
	public ArrayList getEntity_att()
	{
		return entity_att;
	}

	// ////////////////////////////////////////////
	public ArrayList getEntity_att(int index)
	{
		return (ArrayList) entity_att.get(index);
	}

	// ////////////////////////////////////////////
	public ArrayList getRel_att(int index)
	{
		return (ArrayList) relation_att.get(index);
	}

	// ////////////////////////////////////////////
	public ArrayList getEntities(int index)
	{
		return (ArrayList) refEntities.get(index);
	}

	// //////////////////////////////////////
	public int getEntityIndex(String entityName)
	{
		if (entity.contains(entityName))
			return entity.indexOf(entityName);
		else
			return -1;
	}

	// ////////////////////////////////////////////////////
	public ArrayList getEntities(String name)
	{
		if(name.startsWith("B(")){
			name= name.substring(2,name.length()-1);
		}
		
		int i = relation.indexOf(name);
		return (ArrayList) refEntities.get(i);
	}

	// /////////////////////////////////////////////////////////////////////
	public ArrayList getEntityId(int index)
	{
		return (ArrayList) entities_id.get(index);
	}
	/////////////////////////
	public ArrayList<String> getEntityPrimaryKey(String tableName){
		ArrayList res = (ArrayList)entities_id.get(getEntityIndex(tableName));
		return res;
	}
	
	public List<String> getAttributes(){
		List<String> attributes = new ArrayList<String>();
		for(List<String>atts: (List<List<String>>)entity_att){
			attributes.addAll(atts);
		}
		for(List<String>atts: (List<List<String>>)relation_att){
			attributes.addAll(atts);
		}
		return attributes;
	}
	
	public ArrayList getRefEntities(String relation)
	{
		int index=this.relation.indexOf(relation);
		return (ArrayList) refEntities.get(index);
	}
	public ArrayList getRel_att()
	{
		return  relation_att;
	}
	
	public String getTableOfField(String field)
	{
		ArrayList OneEntity_att = new ArrayList();
		// ArrayList OneRelation_att = new ArrayList();

		for (int i = 0; i < entity.size(); i++) {
			// OneEntity_att = parser.getEntity_att(i);
			//OneEntity_att = entity.get(i);
			String tempEntity = entity.get(i);
			OneEntity_att =  (ArrayList)entity_att.get(i);
			if (OneEntity_att.contains(field)) {
				return entity.get(i).toString();
			}
		}

		for (int i = 0; i < relation.size(); i++) {
			OneRelation_att = getRel_att(i);
			if (OneRelation_att.contains(field)) {
				return relation.get(i).toString();
			}
		}
		for (int i = 0; i < relation.size(); i++) {
				if (relation.contains(field)) {
				return field;
			}
		}
		return null;
	}
	public String getTableofField(edu.cmu.tetrad.graph.Node node1)
	{
		ArrayList OneEntity_att = new ArrayList();
		// ArrayList OneRelation_att = new ArrayList();

		for (int i = 0; i < entity.size(); i++) {
			// OneEntity_att = parser.getEntity_att(i);
			//OneEntity_att = entity.get(i);
			String tempEntity = entity.get(i);
			OneEntity_att =  (ArrayList)entity_att.get(i);
			if (OneEntity_att.contains(node1.getName().toString())) {
				return entity.get(i).toString();
			}
		}

		for (int i = 0; i < relation.size(); i++) {
			OneRelation_att = getRel_att(i);
			if (OneRelation_att.contains(node1.getName().toString())) {
				return relation.get(i).toString();
			}

		}
		return null;
	}
	
	

	public ArrayList getRel_att(String relation)
	{
		int index=this.relation.indexOf(relation);
		return (ArrayList) relation_att.get(index);
	}
	
	public List<String> getEntity_att(String tableName)
	{
		if(getEntityIndex(tableName)==-1){

			return null;
		}
		return (List) entity_att.get(getEntityIndex(tableName));
	}

	public ArrayList<String> getTableNames() {
		ArrayList<String> tableNames = new ArrayList<String>();
		tableNames.addAll(entity);
		tableNames.addAll(relation);
		return tableNames;
	}
	
	public int getEntityCount() {
		return entity.size();

	}
	
	public String getEntity(int index) {
		return entity.get(index);
	}
	
	public static void initialize(){
		parser = new Parser();
	}
	public ArrayList getRefEntities() {
		return refEntities;
	}

}
