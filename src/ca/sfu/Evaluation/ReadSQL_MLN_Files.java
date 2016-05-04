package ca.sfu.Evaluation;

import java.io.PrintStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.io.*;
import ca.sfu.jbn.common.Parser;
import ca.sfu.jbn.common.global;

public class ReadSQL_MLN_Files {
	
	class Node{
		String name;
		ArrayList<String> attribute;
		ArrayList<String> parents;
		boolean isParent;
		
		Node( String s ) {
			name = s;
			attribute = new ArrayList<String>();
			parents = new ArrayList<String>();
			isParent = false;
		}
		
		public void setIsParent( boolean b ) {
			isParent=b;
		}
		
		public boolean isParent() {
			return isParent;
		}
		public String getName() {
			return name;
		}
		
		public void addAttribute( String a ) {
			if( !a.contains("_id"))
				a="+"+a.toLowerCase();
			attribute.add(a);
		}
		
		public ArrayList<String> getAttList() {
			return attribute;
		}
		
		public void addParent( String a ) {
			parents.add(a);
		}
		
		public ArrayList<String> getParents() {
			return parents;
		}
		
		public String print() {
			String result =  this.getName() + "(";
			int i=0;
			
			for( ; i<attribute.size()-1; i++ ){
				result = result.concat(attribute.get(i).substring(0,3)+"1, ");			
			}
			result = result.concat( attribute.get(i).substring(0,3) + "1)");
			return result;
		}
	}
	
	private Connection DBConnection;
	private ArrayList<Table> tables;
	private HashMap<String, Node> nodes;
	private Statement cmd;

	public ReadSQL_MLN_Files() {
		tables = new ArrayList<Table>();
		nodes = new HashMap<String, Node>();
		try {
			DriverManager.registerDriver(new com.mysql.jdbc.Driver());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void setConnection(String url, String userID, String passWord) {
		try {
			DBConnection = DriverManager.getConnection(url,
					userID, passWord);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void initialize() throws SQLException {
		setConnection(global.dbURL  +global.schema, global.dbUser, global.dbPassword);
		cmd = DBConnection.createStatement();
//		ResultSet queriedTables = cmd.executeQuery("show tables");
//		//get the table names
//		while (queriedTables.next()) {
//			Table tempTable = new Table(queriedTables.getString(1));
//			tables.add(tempTable);
//		}
//		queriedTables.close();
		
		ArrayList<String> tableNames=Parser.getInstance().getTableNames();
		
		for (String tableName : tableNames) {
			Table tempTable = new Table(tableName);
			tables.add(tempTable);
		}
		
		
		
		//get detailed table info
		for(Iterator<Table> i=tables.iterator();i.hasNext();){
			Table oneTable=i.next();
			ResultSet tableQuery = cmd.executeQuery("show columns from "+oneTable.getName());
			while (tableQuery.next()) {
				Table.KeyType keyType;
				if(tableQuery.getString("KEY").equals("PRI")){
					keyType=Table.KeyType.PRIMARY;
				}
				else if(tableQuery.getString("KEY").equals("MUL")){
					keyType=Table.KeyType.FOREIGH;
				}
				else{
					keyType=Table.KeyType.NONE;
				}
				oneTable.addKey(tableQuery.getString("Field"), keyType);
			}
			tableQuery.close();
		}
	}
	
	public void moralization( PrintStream output )
	{
	      try{
	  	    // FileInputStream fstream = new FileInputStream("graph1.txt");
	    	FileInputStream fstream = new FileInputStream("movielens.txt");
		   	DataInputStream in = new DataInputStream(fstream);
	        BufferedReader br = new BufferedReader(new InputStreamReader(in));
	   	    String strLine;
	   	    while ((strLine = br.readLine()) != null )   {
	   	    	strLine = strLine.replace(" ", "");
	    	    int start = strLine.indexOf(".");
	    	    if( start<0 )
	    	    	continue;
	    	    String[] rule = strLine.substring(start+1).split("-->");
	    	    nodes.get(rule[1]).addParent(rule[0]);
	    	    nodes.get(rule[0]).setIsParent(true);
	   	    }
	   	    in.close();
	   	    fstream.close();
	   	    }catch (Exception e){//Catch exception if any
	   	      System.err.println("Error: " + e.getMessage());
	     }
	      
		for(Iterator<String> i=nodes.keySet().iterator();i.hasNext();){
			String name = i.next();
			Node cnode = nodes.get(name);
			if( !cnode.getParents().isEmpty() ) {
				output.append( cnode.print());
				// output.append("*" + cnode.print());
				for(Iterator<String> s=cnode.getParents().iterator();s.hasNext();) {
					String pname = s.next();
					Node pnode = nodes.get(pname);
					if( pnode==null ) {
						System.out.println("error in linking children "+ cnode.getName() + " with parent " + pname);
					}
					else {
						output.append(" ^ "+pnode.print());
					}
				}
				output.append("\n");
			} else if( cnode.isParent() ) {
				output.append(cnode.print()+"\n");
			}
		}
	}
	
	public void read(PrintStream output1,PrintStream output2,PrintStream output3) throws SQLException, IOException{
		for(Iterator<Table> i=tables.iterator();i.hasNext();){
			Table oneTable=i.next();
			ArrayList<String> primarykeys=oneTable.getPrimaryKeys();
			ArrayList<String> foreignKeys =oneTable.getForeignKeys();
			ArrayList<String> otherKeys =oneTable.getOtherKeys();
			String primaryKey="";
			if(!primarykeys.isEmpty()){
				primaryKey=primarykeys.get(0);
				ResultSet oneQuery = cmd.executeQuery("SELECT DISTINCT "+primaryKey+" FROM "+oneTable.getName());
				while(oneQuery.next()){
					output1.append(primaryKey+"("+primaryKey.toUpperCase()+"_"+oneQuery.getString(primaryKey)+")\n");
				}
				oneQuery.close();
//				System.out.println(primaryKey);
				output2.append(primaryKey+"("+primaryKey+"_type)\n");
				output3.append(primaryKey+"("+primaryKey+"_inst)\n");
				
				if(!otherKeys.isEmpty()){
					for(int j=0;j<otherKeys.size();j++){
						String otherKey=otherKeys.get(j);
						Node nnode = new Node(otherKeys.get(j));
						ResultSet secondQuery = cmd.executeQuery("SELECT DISTINCT "+primaryKey+", "+otherKey+" FROM "+oneTable.getName());
						while(secondQuery.next()){
							output1.append(otherKey+"("+primaryKey.toUpperCase()+"_"+secondQuery.getString(primaryKey)+","+otherKey.toUpperCase()+"_" + secondQuery.getString(otherKey)+")\n");
						}
						secondQuery.close();
						
//						System.out.println(otherKey);
						output2.append(otherKey+"("+primaryKey+"_type,"+otherKey+"_type!)\n");
						output3.append(otherKey+"("+primaryKey+"_inst,"+otherKey+"_inst)\n");
						nnode.addAttribute(primaryKey);
						nnode.addAttribute(otherKey);
						nodes.put(otherKeys.get(j),nnode);
					}
					
				}
			}
			if(!foreignKeys.isEmpty()){
				String foreignKeyList="";
				// if it is relational table, add table attr
				output2.append( "B_"+oneTable.getName() + "(" );
				output3.append( "B_"+oneTable.getName() + "(" );
				
				Node Bnode = new Node("*B_"+oneTable.getName() );
				for (int j=0;j<foreignKeys.size();j++){
					foreignKeyList=foreignKeyList+foreignKeys.get(j)+",";
					Bnode.addAttribute(foreignKeys.get(j));
					if( j==foreignKeys.size()-1 ){
						output2.append( foreignKeys.get(j).replaceAll("_dummy","") + "_type)\n");
						output3.append( foreignKeys.get(j) + "_inst)\n");
					}
					
					else{
						output2.append( foreignKeys.get(j).replaceAll("_dummy","") + "_type, ");
						output3.append( foreignKeys.get(j) + "_inst, ");
					}
				}
				nodes.put("B_"+oneTable.getName(), Bnode); 
				
				String query= "SELECT DISTINCT "+foreignKeyList.substring(0, foreignKeyList.length()-1 )+" FROM "+oneTable.getName();
				ResultSet thirdQuery = cmd.executeQuery(query);
				while(thirdQuery.next()){
					output1.append("B_"+oneTable.getName() + "(");
					for (int k=0;k<foreignKeys.size();k++){
						
						if( k==foreignKeys.size()-1 )
							output1.append(foreignKeys.get(k).toUpperCase().replaceAll("_DUMMY","")+"_"+thirdQuery.getString(foreignKeys.get(k))+")\n");
						else
							output1.append(foreignKeys.get(k).toUpperCase().replaceAll("_DUMMY","")+"_"+thirdQuery.getString(foreignKeys.get(k))+",");
					}
				}

				if(!otherKeys.isEmpty()){
					for(int j=0;j<otherKeys.size();j++){
						String otherKey=otherKeys.get(j);
						ResultSet secondQuery = cmd.executeQuery("SELECT DISTINCT "+foreignKeyList+otherKey+" FROM "+oneTable.getName());
						while(secondQuery.next()){
							output1.append(otherKey+"(");
							for (int k=0;k<foreignKeys.size();k++){
								output1.append(foreignKeys.get(k).toUpperCase().replaceAll("_DUMMY","")+"_"+secondQuery.getString(foreignKeys.get(k))+",");
							}
							String s=secondQuery.getString(otherKey);
							s=otherKey.toUpperCase().replaceAll("_DUMMY","")+"_"+s.replace("\"", "");
							output1.append(s+")\n");	
						}
						secondQuery.close();
						Node nnode = new Node(otherKey );
						output2.append(otherKey+"(");
						output3.append(otherKey+"(");
						
						for (int k=0;k<foreignKeys.size();k++){
							nnode.addAttribute(foreignKeys.get(k));
							output2.append(foreignKeys.get(k)+"_type,");
							output3.append(foreignKeys.get(k)+"_inst,");
						}
						nnode.addAttribute(otherKey);
						output2.append(otherKey+"_type)\n");
						output3.append(otherKey+"_inst)\n");
						nodes.put(otherKey,nnode);

					}
				}
			}	
		}
		output1.close();
		output2.close();
		output3.close();
	}
	
	public static void main(String[] args) throws Exception {
		ReadSQL_MLN_Files read=new ReadSQL_MLN_Files();
		String dbName = "Muta";
		
		read.setConnection("jdbc:mysql://kripke.cs.sfu.ca/:3306/"+dbName, "sfu", "");
		read.initialize();
		
		PrintStream out1=new PrintStream(new FileOutputStream(dbName + ".db"));
		PrintStream out2=new PrintStream(new FileOutputStream(dbName + "_input.mln"));
		
		//read.read(out1, out2);
		// out2.append("\n// rules after moralization\n");
		// read.moralization(out2);
	}
}
