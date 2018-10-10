package ca.sfu.cs.factorbase.tables;

/*Aug 18, 2014, zqian
 * handle the extreme case when there's only `mult` columne
 * fixed the bug, found on July 6th. 
 * */

/*July 6th, 2014, zqian
 * bug for processing the following case:
 * star: mult1
 * flat: mult2
 * false: mult1-mult2 ?
 * try: Financial_std_Training1_db.`operation(trans0)_a_star`
 * */
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.logging.Logger;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.ResultSetMetaData;

/*
//sort merge version3
//here the compare function is also good
//without concatenating the order by columns

*/
public class Sort_merge3 {
	static Connection con;
	final static String database = "sort_merge";
	
	private static Logger logger = Logger.getLogger(Sort_merge3.class.getName());

	public static void main(String[] args) throws SQLException, IOException {
		
		connectDB();
		
		String table1="a2_0_star";
		String table2="a2_0_flat";
		String table3="final_table_csv";
		
		long time1=System.currentTimeMillis();
		
		sort_merge(table1,table2,table3,con);
		
		long time2=System.currentTimeMillis();
		
		logger.info("total time:"+(time2-time1));
		if(compare_tables(table3,"a2_0_false",con))
			logger.fine("true");
		else
			logger.fine("false");
	}
	
	 public static void sort_merge(String table1,String table2, String table3, Connection conn) throws SQLException, IOException{
		 //logger.fine("sort merge version 3");
			logger.info("\nGenerating false table by Subtration using Sort_merge, cur_false_Table is : " + table3);

			//!!!!!!!!!!!!!!!!!remember to change the path and file name
			File ftemp=new File("sort_merge.csv");
			if(ftemp.exists()) ftemp.delete();
			
			File file=new File("sort_merge.csv");
			BufferedWriter output = new BufferedWriter(new FileWriter(file));
			
			Statement st1=conn.createStatement();
			Statement st2=conn.createStatement();
			
			ArrayList<String> orderList=new ArrayList<String>();
			String order;
			/******************************************************************/ 
			//code for getting the order by sequence using TABLE1, IT DOES NOT MATTER WHICH TABLE WE USE AS BOTH TABLES HAVE SAME COLUMNS 
			
			ResultSet rst=st1.executeQuery("show columns from "+table1+" ;");
		     
			 while(rst.next()){
				 orderList.add("`"+rst.getString(1)+"` ");				 
			 }
			 rst.close();
			 
			 for(int i=0;i<orderList.size();i++){  // zqian, do not need this for loop, just filter the sql query by add condition clause: where field not like '%mult%'
				 
				 if((orderList.get(i)).contains("MULT"))
					 {
					 	//logger.fine("sort merge test: "+orderList.get(i));
					 	orderList.remove(i);
					 	break;
					 }
				 
			 }
			 
			 if (orderList.size()>0) //Aug 18, 2014 zqian, handle the extreme case when there's only `mult` column
			 {	order=" "+orderList.get(0)+" ";
			
				for(int i=1;i<orderList.size();i++){
					
					order=order+" , "+orderList.get(i);
					
					}
			/**************************************************************/
				String temp= "MULT decimal ";
				for(int i=0;i<orderList.size();i++){
					
					temp=temp+" , "+orderList.get(i)+" varchar(45) ";
				}
				
				st1.execute("drop table if exists "+table3+" ;");
				st1.execute("create table "+table3+" ( "+temp+") ;" );
				 /***********************************************************************/  
				//code for merging the two tables
				//BottleNeck, MOST expensive query for large table, more than 16 columns, zqian
				//SELECT * INTO OUTFILE '/tmp/result.txt'; //here the files are stored on the Server Side.
				// and then load these files into memory?
				long time1=System.currentTimeMillis();
				logger.info("\n rst1 : " + "select distinct mult, "+order+" from "+table1+" order by "+order+" ;");
			    ResultSet rst1=st1.executeQuery("select distinct mult, "+order+" from "+table1+" order by "+order+" ;"); 
			    ResultSet rst2=st2.executeQuery("select distinct mult, "+order+" from "+table2+" order by "+order+" ;");
			    long time2=System.currentTimeMillis();
			    //System.out.print("order by time:"+(time2-time1));
			    
			    //finding the no. of rows in each table
		        int size1=0,size2=0;
				
				while(rst1.next())size1++;
				while(rst2.next())size2++;
				
				
				
				//finding the no of columns in a table
				ResultSetMetaData rsmd=(ResultSetMetaData) rst1.getMetaData(); // do NOT need to run another query, it should be orderList.size()+1, zqian
				int no_of_colmns=rsmd.getColumnCount();
				
				
				int i=1;int j=1;//index variables for both tables
				rst1.absolute(1);rst2.absolute(1);
				long time3=System.currentTimeMillis();
				//merging starting here 
				while(i<=size1&&j<=size2){
				
				long val1=0,val2=0;
				    for(int k=2;k<=no_of_colmns;k++)
					{  
						try{
//				    	 val1=Integer.parseInt(rst1.getString(k));
//						 val2=Integer.parseInt(rst2.getString(k));
							 val1=Long.parseLong(rst1.getString(k));
							 val2=Long.parseLong(rst2.getString(k));
						}catch(java.lang.NumberFormatException e){
							
						}finally{
							if(rst1.getString(k).compareTo(rst2.getString(k))>0) { val1=1; val2=0;}
							else if(rst1.getString(k).compareTo(rst2.getString(k))<0){val1=0;val2=1;}
						}
						
						
						if(val1<val2){
							String quer=rst1.getString(1);
						       for(int c=2;c<=no_of_colmns;c++){
							    quer=quer+"$"+ rst1.getString(c);
						         }
						       
						         output.write((quer)+"\n");
							i++;break;}
						
						else if(val1>val2){j++;break;}
					}
						if(val1==val2){
					       //String query=""+(Integer.parseInt(rst1.getString(1))-Integer.parseInt(rst2.getString(1)));
					       String query=""+(Long.parseLong(rst1.getString(1))-Long.parseLong(rst2.getString(1)));
					       
					       for(int c=2;c<=no_of_colmns;c++){
						    query=query+"$"+rst1.getString(c);
					         }
					         output.write(query+"\n");
					         		         
					            i++;j++;  	  
				            }
					
				  rst1.absolute(i);rst2.absolute(j);
				}
				
				if(i>1) rst1.absolute(i-1);
				else rst1.beforeFirst();
				while(rst1.next()){
					String query=rst1.getString(1);
				       for(int c=2;c<=no_of_colmns;c++){
					    query=query+"$"+rst1.getString(c);
				         }
				         output.write((query)+"\n");
					
				}
				output.close();
				long time4=System.currentTimeMillis();
			   // System.out.print("\t insert time:"+(time4-time3));
			    
			    
			    st2.execute("drop table if exists "+ table3+"; ");
				st2.execute("create table " +table3+" like "+ table1+" ;");
				st2.execute("LOAD DATA LOcal INFILE 'sort_merge.csv' INTO TABLE "+ table3 +" FIELDS TERMINATED BY '$' LINES TERMINATED BY '\\n'  ;");
			 	
			rst1.close();
			rst2.close();
			st1.close();
			st2.close();
			 
			 
			long time5=System.currentTimeMillis();
			//System.out.print("\t export csv file to sql:"+(time5-time4));
			logger.info("\ntotal time: "+(time5-time1)+"\n");
			//delete the csv file, zqian
			if(ftemp.exists()) ftemp.delete();
			 }
			 
			 else { //Aug 18, 2014 zqian //handle the extreme case when there's only `mult` columne
				logger.fine("\n \t handle the extreme case when there's only `mult` columne \n");
				st2.execute("drop table if exists "+ table3+"; ");
				st2.execute("create table " +table3+" like "+ table1+" ;");
				logger.fine("insert into "+table3+" select ("+table1+".mult - "+table2+".mult ) as mult from "+table1 + ", "+ table2 + " ;");
				st2.execute("insert into "+table3+" select ("+table1+".mult - "+table2+".mult ) as mult from "+table1 + ", "+ table2 + " ;");
			

			 }
			 
}
	 
	 public static boolean compare_tables(String table1 , String table2,Connection conn) throws SQLException{
			
			
			
			
			java.sql.Statement st1=conn.createStatement();
			java.sql.Statement st2=conn.createStatement();
			
			
			ResultSet rst1=st1.executeQuery("show columns from "+table1);
			ResultSet rst2=st2.executeQuery("show columns from "+table2);
			ArrayList<String> list1=new ArrayList<String>();
			ArrayList<String> list2=new ArrayList<String>();
			while(rst1.next()){
			list1.add(rst1.getString(1));
			}
			while(rst2.next()){
				list2.add(rst2.getString(1));
				}
			if(list1.size()!=list2.size()){
				logger.fine("not equal number of columns in "+table1+" and "+table2);
				return false;
			}
			
				for(int i=0;i<list1.size();i++){
				if(!list1.get(i).equals(list2.get(i))){
				logger.fine(list1.get(i)+" "+list2.get(i));	
				logger.fine("coulumns are not same or their order is not same");
				return false;
			}
				}
			rst1.absolute(1);
			String order=rst1.getString(1);
			while(rst1.next()){
				order=order+" , `"+rst1.getString(1)+"`";
			}
			
			 int size1=0;
			 int size2=0;
		     rst1=st1.executeQuery("select distinct * from "+table1+" order by "+order+" ;" );
		     rst2=st2.executeQuery("select distinct * from "+table2+ " order by "+order+" ;");	
		     while(rst1.next())size1++;
		     while(rst2.next())size2++;
		     if(size1!=size2)
		    	 logger.fine("not equal no. of rows in both the tables");
		     
		     rst1.beforeFirst();
		     rst2.beforeFirst();
		     
		     while(rst1.next()&&rst2.next()){
		     String temp1="",temp2="";
		    	
		    for(int i=1;i<=list1.size();i++)
		    {
			temp1=temp1+rst1.getString(i);
			temp2=temp2+rst2.getString(i);
		    }
			if(!temp1.equals(temp2)){
				logger.fine("tables not equal");
				return false;
			  }
		    }
		
		     
		     
	   logger.fine("tables are equal");
	   return true;
	 }
	
	
	public static void connectDB() throws SQLException {
        String CONN_STR = "jdbc:mysql://kripke.cs.sfu.ca/"+database;
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (Exception ex) {
            logger.severe("Unable to load MySQL JDBC driver");
        }
        con = (Connection) DriverManager.getConnection(CONN_STR, "root", "alibz");
    }
}