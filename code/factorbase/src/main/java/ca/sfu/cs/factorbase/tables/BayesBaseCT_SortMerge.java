package ca.sfu.cs.factorbase.tables;

/*zqian, April 1st, 2014 fixed the bug of too many connections by adding con4.close()*/


/*Jun 25,2013 @zqian
 * 
 * Trying to conquer the bottleneck of creating false tables (the join issue) by implementing our sort_merge algorithm.
 * Great stuff!
 * 
 * Here we have some different versions.
 * for version 3, naive implementing of sort merge with "load into" command in terms of efficiency issue of mysql insertion.
 * for version 4, concatenating the order by columns into one column when version 3 can not finish the order by .
 * 
 * for version 5, it's a kind of more complicated approach by pre-compressing all the attribute columns into one column, and then employing concatenating trick again on order by part.
 *   this version still has some bugs that need to be investagiated.
 *   
 * Preconditions: database_BN  has been created with lattice information and functor information.
 *  
 * */

import java.io.IOException;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;

import ca.sfu.cs.common.Configuration.Config;
import ca.sfu.cs.factorbase.lattice.short_rnid_LatticeGenerator;
import ca.sfu.cs.factorbase.util.BZScriptRunner;
import ca.sfu.cs.factorbase.util.Sort_merge3;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.exceptions.jdbc4.MySQLSyntaxErrorException;

public class BayesBaseCT_SortMerge {

    private static Connection con_std;
    private static Connection con_BN;
    private static Connection con_CT;
    private static Connection con_setup;
    private static String databaseName_std;
    private static String databaseName_BN;
    private static String databaseName_CT;
    private static String databaseName_setup;
    private static String dbUsername;
    private static String dbPassword;
    private static String dbaddress;
    private static String linkCorrelation;
    /*
     * cont is Continuous
     * ToDo: Refactor
     */
    private static String cont;

    private static int maxNumberOfMembers = 0;
    
    private static Logger logger = Logger.getLogger(BayesBaseCT_SortMerge.class.getName());

    public static void main(String[] args) throws Exception {
              
        buildCT();

    }

    /**
     * @Overload
     * buildCT
     * @throws Exception
     */
    public static void buildCT() throws Exception {

        setVarsFromConfig();
        //connect to db using jdbc
        con_std = connectDB(databaseName_std);
        con_setup = connectDB(databaseName_setup);
        //build _BN copy from _setup Nov 1st, 2013 Zqiancompute the subset given fid and it's parents
        BZScriptRunner bzsr = new BZScriptRunner(databaseName_std,con_setup);
        bzsr.runScript(Config.SCRIPTS_DIRECTORY + "transfer.sql");

        con_BN = connectDB(databaseName_BN);
        con_CT = connectDB(databaseName_CT);

        //generate lattice tree
        //maxNumberOfMembers = LatticeGenerator.generate(con2);
        // rnid mapping. maxNumberofMembers = maximum size of lattice element. Should be called LatticeHeight
        maxNumberOfMembers = short_rnid_LatticeGenerator.generate(con_BN);


        logger.info(" ##### lattice is ready for use* ");

// may not need to run this script any more, using LatticeRnodes table OS August 25, 2017//
       // bzsr.runScript("scripts/add_orig_rnid.sql");
       // add_orig_rnid should be superseded by using Lattice Rnodes table in metaqueries script
       // OS Sep 12, 2017
        //build _BN part2: from metadata_2.sql

       // bzsr.runScript("scripts/metadata.sql");
       // this metadata is now created as views in db_setup, transferred by transfer.sql

        // empty query error,fixed by removing one duplicated semicolon. Oct 30, 2013
        //ToDo: No support for executing LinkCorrelation=0;
        if (cont.equals("1")) {
            bzsr.runScript(Config.SCRIPTS_DIRECTORY + "metaqueries_cont.sql");
        } else if (linkCorrelation.equals("1")) { //LinkCorrelations
            bzsr.runScript(Config.SCRIPTS_DIRECTORY + "metaqueries.sql");
        } else {
            bzsr.runScript(Config.SCRIPTS_DIRECTORY + "metaqueries.sql");
            // modified on Feb. 3rd, 2015, zqian, to include rnode as columns
        //          bzsr.runScript("scripts/metadata_2_nolink.sql");
        }
      //  bzsr.runScript("scripts/model_manager.sql");
        //why are we running the model manager first? // commenting this out for now August 22
        bzsr.runScript(Config.SCRIPTS_DIRECTORY + "metaqueries_RChain.sql");

        // building CT tables for Rchain
        CTGenerator();
        disconnectDB();
    }
 
    /*** this part we do need O.s. May 16, 2018 ***/
    /**
     *  Building the _CT tables for length >=2
     *  For each length
     *  1. find rchain, find list of members of rc-hain. Suppose first member is rnid1.
     *  2. initialize current_ct = rchain_counts after summing out the relational attributes of rnid1.
     *  3. Current list = all members of rchain minus rndi1. find ct(table) for current list = . Select rows where all members of current list are true. Add 1nodes of rnid1.
     *  4. Compute false table using the results of 2 and 3 (basically 2 - 3).
     *  5. Union false table with current_ct to get new ct where all members of current list are true.
     *  6. Repeat with current list as initial list until list is empty.
     *  Example:
     *  1. Rchain = R3,R2,R1. first rnid1 = R3.
     *  2. Find `R3,R2,R1_counts`. Sum out fields from R3 to get `R2,R1-R3_flat1`.
     *  3. Current list = R2,R1. Find `R2,R1_ct` where R1 = T, R2 = T. Add 1nodes of R3 (multiplying) to get `R2,R1-R3_star`.
     *  4. Compute `R2,R1-R3_false` = `R2,R1-R3_star - `R2,R1-R3_flat1` union (as before)
     *  5. Compute `R2,R1-R3_ct` = `R2,R1-R3_false` cross product `R3_join` union `R3,R2,R1_counts`.
     *  6. Current list = R1. Current rnid = R2. Current ct_table = `R2,R1-R3_ct`.
     *
     *  BuildCT_Rnodes_flat(len);
     *
     *  BuildCT_Rnodes_star(len);
     *
     *  BuildCT_Rnodes_CT(len);
     *
     * @throws Exception
     */
    public static void CTGenerator() throws Exception{
        
        long l = System.currentTimeMillis(); //@zqian : CT table generating time
           // handling Pvars, generating pvars_counts       
        BuildCT_Pvars();
        
        // preparing the _join part for _CT tables
        BuildCT_Rnodes_join();
        
        //building the RNodes_counts tables. should be called Rchains since it goes up the lattice.
        if(linkCorrelation.equals("1")) {
            long l_1 = System.currentTimeMillis(); //@zqian : measure structure learning time
            for(int len = 1; len <= maxNumberOfMembers; len++){
                BuildCT_Rnodes_counts(len);
            }
            long l2 = System.currentTimeMillis(); //@zqian : measure structure learning time
            logger.info("Building Time(ms) for Rnodes_counts: "+(l2-l_1)+" ms.\n");
        }
        else {
            logger.warning("link off !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            for(int len = 1; len <= maxNumberOfMembers; len++)
                BuildCT_Rnodes_counts2(len);
            //count2 simply copies the counts to the CT tables
            //copying the code seems very inelegant OS August 22

        }
                                                                      
        if (linkCorrelation.equals("1")) {
            // handling Rnodes with Lattice Moebius Transform
            //initialize first level of rchain lattice
            for(int len = 1; len <= 1; len++) {
                logger.info("Building Time(ms) for Rchain =1 \n");
                //building the _flat tables
                BuildCT_Rnodes_flat(len);
        
                //building the _star tables
                BuildCT_Rnodes_star(len);

                //building the _false tables first and then the _CT tables
                BuildCT_Rnodes_CT(len);
            }
            
            //building the _CT tables. Going up the Rchain lattice
            for(int len = 2; len <= maxNumberOfMembers; len++)
            { 
                logger.info("now we're here for Rchain!");
                logger.info("Building Time(ms) for Rchain >=2 \n");
                BuildCT_RChain_flat(len);
                logger.info(" Rchain! are done");
            }
        }
        

        //delete the tuples with MULT=0 in the biggest CT table
        String BiggestRchain="";
        Statement st_BN= con_BN.createStatement();
        ResultSet rs = st_BN.executeQuery("select name as RChain from lattice_set where lattice_set.length = (SELECT max(length)  FROM lattice_set);" );

        boolean RChainCreated = false;
        while(rs.next())
        {
            RChainCreated = true;
            BiggestRchain = rs.getString("RChain");
            logger.fine("\n BiggestRchain : " + BiggestRchain);
        }
        
        st_BN.close();
        
        if ( RChainCreated )
        {
            Statement st_CT = con_CT.createStatement();
            try
            {
                st_CT.execute("delete from `"+BiggestRchain.replace("`", "") +"_CT` where MULT='0';" );
                logger.fine("delete from `"+BiggestRchain.replace("`", "") +"_CT` where MULT='0';" );

            }
            catch ( MySQLSyntaxErrorException e )
            {
                //Do nothing
            }
            st_CT.close();
        }
        
        long l2 = System.currentTimeMillis();  //@zqian
        logger.info("Building Time(ms) for ALL CT tables:  "+(l2-l)+" ms.\n");
    }

    /**
     * setVarsFromConfig
     * ToDo : Remove Duplicate definitions across java files
     */
    public static void setVarsFromConfig(){
        Config conf = new Config();
        databaseName_std = conf.getProperty("dbname");
        databaseName_BN = databaseName_std + "_BN";
        databaseName_CT = databaseName_std + "_CT";
        databaseName_setup = databaseName_std + "_setup";
        dbUsername = conf.getProperty("dbusername");
        dbPassword = conf.getProperty("dbpassword");
        dbaddress = conf.getProperty("dbaddress");
        linkCorrelation = conf.getProperty("LinkCorrelations");
        cont = conf.getProperty("Continuous");
    }
  
    /**
     * Connect to database via MySQL JDBC driver
     */
    public static Connection connectDB(String databaseName) throws SQLException{
        String CONN_STR = "jdbc:" + dbaddress + "/" + databaseName;
        try {
            java.lang.Class.forName("com.mysql.jdbc.Driver");
        } catch (Exception ex) {
            logger.severe("Unable to load MySQL JDBC driver");
        }
        return (Connection) DriverManager.getConnection(CONN_STR, dbUsername, dbPassword);
    }
        
        
    /**
     * Building the _CT tables. Going up the Rchain lattice ( When rchain.length >=2)
     * @param int : length of the RChain
     * @throws  SQLException
     * @throws  IOException
     */
    public static void BuildCT_RChain_flat(int len) throws SQLException, IOException {
        logger.info("\n ****************** \n" +
                "Building the _CT tables for Length = "+len +"\n" );

        long l = System.currentTimeMillis(); 

        Statement st = con_BN.createStatement();
        ResultSet rs = st.executeQuery(
            "SELECT short_rnid AS short_RChain, orig_rnid AS RChain " +
            "FROM lattice_set " +
            "JOIN lattice_mapping " +
            "ON lattice_set.name = lattice_mapping.orig_rnid " +
            "WHERE lattice_set.length = " + len + ";"
        );
        int fc=0;
        while(rs.next())
        {
            long l1 = System.currentTimeMillis(); 

            //System.out.print("fc ::"+ fc);
            // Get the short and full form rnids for further use.
            String rchain = rs.getString("RChain");
            logger.fine("\n RChain : " + rchain);
            String shortRchain = rs.getString("short_RChain");
            logger.fine(" Short RChain : " + shortRchain);
            // Oct 16 2013
            // initialize the cur_CT_Table, at very beginning we will use _counts table to create the _flat table
            String cur_CT_Table = "`" + shortRchain.replace("`", "") + "_counts`";
            logger.fine(" cur_CT_Table : " + cur_CT_Table);
            // counts represents the ct tables where all relationships in Rchain are true

            //  create new statement
            Statement st1 = con_BN.createStatement();
            ResultSet rs1 = st1.executeQuery(
                "SELECT DISTINCT parent, removed, short_rnid " +
                "FROM lattice_rel " +
                "JOIN lattice_mapping " +
                "ON lattice_rel.removed = lattice_mapping.orig_rnid " +
                "WHERE child = '" + rchain + "' " +
                "ORDER BY removed ASC;"
            ); // members of rchain
            
            while(rs1.next())
            {       
                long l2 = System.currentTimeMillis(); 
                String parent = rs1.getString("parent");
                logger.fine("\n parent : " + parent);
                String removed = rs1.getString("removed");
                logger.fine("\n removed : " + removed);  
                String removedShort = rs1.getString("short_rnid");
                logger.fine("\n removed short : " + removedShort);
                String BaseName = "`" + shortRchain.replace("`", "") + "_" + removedShort.replace("`", "") + "`";
                logger.fine(" BaseName : " + BaseName );
                
                Statement st2 = con_BN.createStatement();
                Statement st3 = con_CT.createStatement();
                    
                //  create select query string  
                
                            
                ResultSet rs2 = st2.executeQuery("SELECT DISTINCT Entries FROM MetaQueries WHERE Lattice_Point = '" + rchain + "' and '"+removed+"' = EntryType and ClauseType = 'SELECT' and TableType = 'Star';");
                String selectString = makeCommaSepQuery(rs2, "Entries", " , ");         
                logger.fine("Select String : " + selectString);
                rs2.close();
                //  create mult query string
                ResultSet rs3 = st2.executeQuery("SELECT DISTINCT Entries FROM MetaQueries WHERE Lattice_Point = '" + rchain + "' and '"+removed+"' = EntryType and ClauseType = 'FROM' and TableType = 'Star';");
                String MultString = makeStarSepQuery(rs3, "Entries", " * ");
                logger.fine("Mult String : " + MultString+ " as `MULT`");
                rs3.close();
                //  create from query string
                ResultSet rs4 = st2.executeQuery("SELECT DISTINCT Entries FROM MetaQueries WHERE Lattice_Point = '" + rchain + "' and '"+removed+"' = EntryType and ClauseType = 'FROM' and TableType = 'Star';");
                String fromString = makeCommaSepQuery(rs4, "Entries", " , ");
                logger.fine("From String : " + fromString);          
                rs4.close();
                //  create where query string
                ResultSet rs5 = st2.executeQuery("SELECT DISTINCT Entries FROM MetaQueries WHERE Lattice_Point = '" + rchain + "' and '"+removed+"' = EntryType and ClauseType = 'WHERE' and TableType = 'Star';");
                String whereString = makeCommaSepQuery(rs5, "Entries", " and ");
               logger.fine("Where String : " + whereString);
                rs5.close();
                //  create the final query
                String queryString ="";
                if (!whereString.isEmpty())     
                    queryString = "Select " +  MultString+ " as `MULT` ,"+selectString + " from " + fromString  + " where " + whereString;
                else 
                    queryString = "Select " +  MultString+ " as `MULT` ,"+selectString + " from " + fromString;
                logger.fine("Query String : " + queryString );   
                
                //make the rnid shorter 
                String rnid_or=removedShort;
            
                String cur_star_Table = "`" + removedShort.replace("`", "") + len + "_" + fc + "_star`";
                String createStarString = "create table "+cur_star_Table +" as "+queryString;
                    
            
                logger.fine("\n create star String : " + createStarString );
                st3.execute(createStarString);      //create star table     
            
                 //adding  covering index May 21
                //create index string
                ResultSet rs15 = st2.executeQuery("select column_name as Entries from information_schema.columns where table_schema = '"+databaseName_CT+"' and table_name = '"+cur_star_Table.replace("`","")+"';");
                String IndexString = makeIndexQuery(rs15, "Entries", " , ");
                //logger.fine("Index String : " + IndexString);
                //logger.fine("alter table "+cur_star_Table+" add index "+cur_star_Table+"   ( "+IndexString+" );");
                st3.execute("alter table "+cur_star_Table+" add index "+cur_star_Table+"   ( "+IndexString+" );");       
                long l3 = System.currentTimeMillis(); 
                logger.info("Building Time(ms) for "+cur_star_Table+ " : "+(l3-l2)+" ms.\n");
                //staring to create the _flat table
                // Oct 16 2013
                // here is the wrong version that always uses _counts table to generate the _flat table. 
                //String    cur_CT_Table="`"+rchain.replace("`", "")+"_counts`";
                // cur_CT_Table should be the one generated in the previous iteration
                // for the very first iteration, it's _counts table
                logger.fine("cur_CT_Table is : " + cur_CT_Table);

                String cur_flat_Table = "`" + removedShort.replace("`", "") + len + "_" + fc + "_flat`";
                String queryStringflat = "select sum("+cur_CT_Table+".`MULT`) as 'MULT', "+selectString + " from " +cur_CT_Table+" group by  "+ selectString +";" ;
                String createStringflat = "create table "+cur_flat_Table+" as "+queryStringflat;
                logger.fine("\n create flat String : " + createStringflat );         
                st3.execute(createStringflat);      //create flat table
            
                 //adding  covering index May 21
                //create index string
                ResultSet rs25 = st2.executeQuery("select column_name as Entries from information_schema.columns where table_schema = '"+databaseName_CT+"' and table_name = '"+cur_flat_Table.replace("`","")+"';");
                String IndexString2 = makeIndexQuery(rs25, "Entries", " , ");
                //logger.fine("Index String : " + IndexString2);
                //logger.fine("alter table "+cur_flat_Table+" add index "+cur_flat_Table+"   ( "+IndexString2+" );");
                st3.execute("alter table "+cur_flat_Table+" add index "+cur_flat_Table+"   ( "+IndexString2+" );");
                long l4 = System.currentTimeMillis(); 
                logger.info("Building Time(ms) for "+cur_flat_Table+ " : "+(l4-l3)+" ms.\n");
                /**********starting to create _flase table***using sort_merge*******************************/
                // starting to create _flase table : part1
                String cur_false_Table = "`" + removedShort.replace("`", "") + len + "_" + fc + "_false`";
                
                //create false table                    
                //Sort_merge5.sort_merge(cur_star_Table,cur_flat_Table,cur_false_Table,con3);
                //Sort_merge4.sort_merge(cur_star_Table,cur_flat_Table,cur_false_Table,con3);
                Sort_merge3.sort_merge(cur_star_Table,cur_flat_Table,cur_false_Table,con_CT);
                 // a separate procedure for computing the false table as the mult difference between star and flat
                 // trying to optimize this big join
                
                 //adding  covering index May 21
                //create index string
                ResultSet rs35 = st2.executeQuery("select column_name as Entries from information_schema.columns where table_schema = '"+databaseName_CT+"' and table_name = '"+cur_false_Table.replace("`","")+"';");
                String IndexString3 = makeIndexQuery(rs35, "Entries", " , ");
                //logger.fine("Index String : " + IndexString3);
                //logger.fine("alter table "+cur_false_Table+" add index "+cur_false_Table+"   ( "+IndexString3+" );");
                st3.execute("alter table "+cur_false_Table+" add index "+cur_false_Table+"   ( "+IndexString3+" );");       
                long l5 = System.currentTimeMillis(); 
                logger.info("Building Time(ms) for "+cur_false_Table+ " : "+(l5-l4)+" ms.\n");
         
                // staring to create the CT table
                ResultSet rs_45 = st2.executeQuery("select column_name as Entries from information_schema.columns where table_schema = '"+databaseName_CT+"' and table_name = '"+cur_CT_Table.replace("`","")+"';");
                String CTJoinString = makeUnionSepQuery(rs_45, "Entries", " , ");
                //logger.fine("select column_name as Entries from information_schema.columns where table_schema = '"+databaseName3+"' and table_name = '"+cur_CT_Table.replace("`","")+"';");
                logger.fine("CT Join String : " + CTJoinString);
                
                //join false table with join table to add in rnid (= F) and 2nid (= n/a). then can union with CT table
                String QueryStringCT = "select "+CTJoinString+" from "+cur_CT_Table + " union " + "select "+CTJoinString+" from " + cur_false_Table +", `" + rnid_or.replace("`", "") +"_join`";
                //logger.fine("\n Query String for CT Table: "+ QueryStringCT);
                
                //String Next_CT_Table="OS_Dummy";
                String Next_CT_Table="";
                if (rs1.next())
                    Next_CT_Table="`"+BaseName.replace("`", "")+"_CT`";
                else                 
                    Next_CT_Table = "`" + shortRchain.replace("`", "") + "_CT`";
                    
                // Oct 16 2013
                // preparing the CT table for next iteration
                cur_CT_Table = Next_CT_Table;   
                
                //logger.fine("\n name for Next_CT_Table : "+Next_CT_Table);
             
                logger.fine("\n create CT table string: create table "+Next_CT_Table+" as " + QueryStringCT +"\n*****\n");
                st3.execute("create  table "+Next_CT_Table+" as " + QueryStringCT);  //create CT table  
                rs1.previous();

                //adding  covering index May 21
                //create index string
                ResultSet rs45 = st2.executeQuery("select column_name as Entries from information_schema.columns where table_schema = '"+databaseName_CT+"' and table_name = '"+Next_CT_Table.replace("`","")+"';");
                String IndexString4 = makeIndexQuery(rs45, "Entries", " , ");
                //logger.fine("Index String : " + IndexString4);
                //logger.fine("alter table "+Next_CT_Table+" add index "+Next_CT_Table+"   ( "+IndexString4+" );");
                st3.execute("alter table "+Next_CT_Table+" add index "+Next_CT_Table+"   ( "+IndexString4+" );");       

                fc++;   
                
                //  close statements
                st2.close();            
                st3.close();
                long l6 = System.currentTimeMillis(); 
                logger.info("Building Time(ms) for "+cur_CT_Table+ " : "+(l6-l5)+" ms.\n");
            }
            st1.close();
            rs1.close();
        }
        //logger.fine("count "+count+"\n");
        rs.close();
        st.close();
        long l2 = System.currentTimeMillis(); //@zqian : measure structure learning time
        //System.out.print("Building Time(ms): "+(l2-l)+" ms.\n");
        logger.info("\n Build CT_RChain_TABLES for length = "+len+" are DONE \n" );
    }

    /* building pvars_counts*/
    public static void BuildCT_Pvars() throws SQLException, IOException {
        long l = System.currentTimeMillis(); //@zqian : measure structure learning time
        Statement st = con_BN.createStatement();
        st.execute("Drop schema if exists " + databaseName_CT + ";");
        st.execute("Create schema if not exists " + databaseName_CT + ";");
        ResultSet rs = st.executeQuery("select * from PVariables;");

        while(rs.next()){
            //  get pvid for further use
            String pvid = rs.getString("pvid");
            logger.fine("pvid : " + pvid);
            //  create new statement
            Statement st2 = con_BN.createStatement();
            Statement st3 = con_CT.createStatement();
            //  create select query string
            ResultSet rs2 = st2.executeQuery("select distinct Entries from MetaQueries where Lattice_Point = '" + pvid + "' and ClauseType = 'SELECT' and TableType = 'Counts' and EntryType = 'aggregate' union select distinct Entries from MetaQueries where Lattice_Point = '" + pvid + "' and ClauseType = 'SELECT' and TableType = 'Counts' and EntryType <> 'aggregate';");
            String selectString = makeCommaSepQuery(rs2, "Entries", " , ");
            logger.fine("Select String : " + selectString);
            //  create from query string
            ResultSet rs3 = st2.executeQuery("select distinct Entries from MetaQueries where Lattice_Point = '" + pvid + "' and ClauseType = 'FROM' and TableType = 'Counts' ;");
            String fromString = makeCommaSepQuery(rs3, "Entries", " , ");
            //logger.fine("From String : " + fromString);and TableType = 'Counts'and TableType = 'Counts'

            ResultSet rs_6 = st2.executeQuery("select distinct Entries from MetaQueries where Lattice_Point = '" + pvid + "' and ClauseType = 'GROUPBY' and TableType = 'Counts' ;");
            String GroupByString = makeCommaSepQuery(rs_6, "Entries", " , ");
            //logger.fine("GroupBy String : " + GroupByString);

            /*
             *  Check for groundings on pvid
             *  If exist, add as where clause
             */
            logger.fine( "con_BN:SELECT id FROM Groundings WHERE pvid = '"+pvid+"';" );
            
            ResultSet rsGrounding = null;
            String whereString = "";
            
            try
            {
                rsGrounding = st2.executeQuery("select distinct Entries from MetaQueries where Lattice_Point = '" + pvid + "' and ClauseType = 'WHERE' and TableType = 'Counts' ;");
            }
            catch( MySQLSyntaxErrorException e )
            {
                logger.severe( "No WHERE clause for groundings" );
            }
            
            if ( null != rsGrounding )
            {

                whereString = makeCommaSepQuery(rsGrounding, "Entries", " AND ");

            }
            
            logger.fine( "whereString:" + whereString );

            //  create the final query
            String queryString = "Select " + selectString + " from " +
                                 fromString + whereString;
                                 
//this seems unnecessarily complicated even to deal with continuos variables. OS August 22, 2017

            if (!cont.equals("1"))
                if (!GroupByString.isEmpty()) queryString = queryString + " group by"  + GroupByString;

            //logger.fine("Query String : " + queryString );
            logger.fine("Create String : " + "create table "+pvid+"_counts"+" as "+queryString );
            st3.execute("create table "+pvid+"_counts"+" as "+queryString);
            //adding  covering index May 21
            //create index string
            ResultSet rs4 = st2.executeQuery("select column_name as Entries from information_schema.columns where table_schema = '"+databaseName_CT+"' and table_name = '"+pvid+"_counts';");
            String IndexString = makeIndexQuery(rs4, "Entries", " , ");
            //logger.fine("Index String : " + IndexString);
            st3.execute("alter table "+pvid+"_counts"+" add  index "+pvid+"_Index   ( "+IndexString+" );");

            //  close statements
            st2.close();
            st3.close();
        }

        rs.close();
        st.close();
        long l2 = System.currentTimeMillis(); //@zqian : measure structure learning time
        logger.info("Building Time(ms) for Pvariables counts: "+(l2-l)+" ms.\n");
        logger.info("\n Pvariables are DONE \n" );
    }

    /**
     * building the RNodes_counts tables
     *
     */
    public static void BuildCT_Rnodes_counts(int len) throws SQLException, IOException {

        Statement st = con_BN.createStatement();
        ResultSet rs = st.executeQuery(
            "SELECT short_rnid AS short_RChain, orig_rnid AS RChain " +
            "FROM lattice_set " +
            "JOIN lattice_mapping " +
            "ON lattice_set.name = lattice_mapping.orig_rnid " +
            "WHERE lattice_set.length = " + len + ";"
        );
        while(rs.next()){

            // Get the short and full form rnids for further use.
            String rchain = rs.getString("RChain");
            logger.fine("\n RChain : " + rchain);
            String shortRchain = rs.getString("short_RChain");
            logger.fine(" Short RChain : " + shortRchain);

            //  create new statement
            Statement st2 = con_BN.createStatement();
            Statement st3 = con_CT.createStatement();

            //  create select query stringt                                                                                                                                                                                         
            ResultSet rs2 = st2.executeQuery("select distinct Entries from MetaQueries where Lattice_Point = '" + rchain + "' and ClauseType = 'SELECT' and TableType = 'Counts'  and EntryType = 'aggregate' union select distinct Entries from MetaQueries where Lattice_Point = '" + rchain + "' and ClauseType = 'SELECT' and TableType = 'Counts'  and EntryType <> 'aggregate';");
                                              
            String selectString = makeCommaSepQuery(rs2, "Entries", " , ");
            logger.fine("Select String : " + selectString);

            //  create from query string
            ResultSet rs3 = st2.executeQuery("select distinct Entries from MetaQueries where Lattice_Point = '" + rchain + "' and ClauseType = 'FROM' and TableType = 'Counts' ;" );
            String fromString = makeCommaSepQuery(rs3, "Entries", " , ");
            logger.fine("From String : " + fromString);

            //  create where query string
            ResultSet rs4 = st2.executeQuery("select distinct Entries from MetaQueries where Lattice_Point = '" + rchain + "' and ClauseType = 'WHERE' and TableType = 'Counts' ;" );
            String whereString = makeCommaSepQuery(rs4, "Entries", " and ");
            //logger.fine("Where String : " + whereString);

            //  create the final query
            String queryString = "Select " + selectString + " from " + fromString + " where " + whereString;

            //  create group by query string
            // this seems unnecessarily complicated - isn't there always a group by clause? Okay not with continuous data, but still. Continuous probably requires a different approach. OS August 22
            if (!cont.equals("1")) {
                ResultSet rs_6 = st2.executeQuery("select distinct Entries from MetaQueries where Lattice_Point = '" + rchain + "' and ClauseType = 'GROUPBY' and TableType = 'Counts' ;");
                String GroupByString = makeCommaSepQuery(rs_6, "Entries", " , ");
                //logger.fine("GroupBy String : " + GroupByString);

                if (!GroupByString.isEmpty()) queryString = queryString + " group by"  + GroupByString;
                //logger.fine("Query String : " + queryString );
            }

            String createString = "CREATE TABLE `" + shortRchain.replace("`", "") + "_counts`" + " AS " + queryString;
            logger.fine("create String : " + createString );
            st3.execute(createString);

            //adding  covering index May 21
            //create index string
            ResultSet rs5 = st2.executeQuery(
                "SELECT column_name AS Entries " +
                "FROM information_schema.columns " +
                "WHERE table_schema = '" + databaseName_CT + "' " +
                "AND table_name = '" + shortRchain.replace("`", "") + "_counts';"
            );
            String IndexString = makeIndexQuery(rs5, "Entries", " , ");
            //logger.fine("Index String : " + IndexString);
            //logger.fine("alter table `"+rchain.replace("`", "") +"_counts`"+" add index `"+rchain.replace("`", "") +"_Index`   ( "+IndexString+" );");
            st3.execute(
                "ALTER TABLE `" + shortRchain.replace("`", "") + "_counts` " +
                "ADD INDEX `" + shortRchain.replace("`", "") +"_Index` (" + IndexString + ");"
            );

            //  close statements
            st2.close();
            st3.close();
        }

        rs.close();
        st.close();

        logger.info("\n Rnodes_counts are DONE \n" );
    }

    /**
     * building the RNodes_counts tables,count2 simply copies the counts to the CT tables
     */
    public static void BuildCT_Rnodes_counts2(int len) throws SQLException, IOException {
       
         Statement st = con_BN.createStatement();
        ResultSet rs = st.executeQuery("select name as RChain from lattice_set where lattice_set.length = " + len + ";");
        while(rs.next()){

            //  get rnid for further use
            String rchain = rs.getString("RChain");
            logger.fine("\n RChain : " + rchain);

            //  create new statement
            Statement st2 = con_BN.createStatement();
            Statement st3 = con_CT.createStatement();

            //  create select query stringt
            ResultSet rs2 = st2.executeQuery("select distinct Entries from MetaQueries where Lattice_Point = '" + rchain + "' and ClauseType = 'SELECT' and TableType = 'Counts' and EntryType = 'aggregate' union select distinct Entries from MetaQueries where Lattice_Point = '" + rchain + "' and ClauseType = 'SELECT' and TableType = 'Counts' and EntryType <> 'aggregate';;");
                                              
            String selectString = makeCommaSepQuery(rs2, "Entries", " , ");
            //logger.fine("Select String : " + selectString);

            //  create from query string
            ResultSet rs3 = st2.executeQuery("select distinct Entries from MetaQueries where Lattice_Point = '" + rchain + "' and ClauseType = 'FROM' and TableType = 'Counts';" );
            String fromString = makeCommaSepQuery(rs3, "Entries", " , ");
            //logger.fine("From String : " + fromString);

            //  create where query string
            ResultSet rs4 = st2.executeQuery("select distinct Entries from MetaQueries where Lattice_Point = '" + rchain + "' and ClauseType = 'WHERE' and TableType = 'Counts';" );
            String whereString = makeCommaSepQuery(rs4, "Entries", " and ");
            //logger.fine("Where String : " + whereString);

            //  create the final query
            String queryString = "Select " + selectString + " from " + fromString + " where " + whereString;

            //  create group by query string
            // this seems unnecessarily complicated - isn't there always a group by clause? Okay not with continuous data, but still. Continuous probably requires a different approach. OS August 22
            if (!cont.equals("1")) {
                ResultSet rs_6 = st2.executeQuery("select distinct Entries from MetaQueries where Lattice_Point = '" + rchain + "' and ClauseType = 'GROUPBY' and TableType = 'Counts';");
                String GroupByString = makeCommaSepQuery(rs_6, "Entries", " , ");
                //logger.fine("GroupBy String : " + GroupByString);

                if (!GroupByString.isEmpty()) queryString = queryString + " group by"  + GroupByString;
                //logger.fine("Query String : " + queryString );
            }

            String createString = "create table `"+rchain.replace("`", "") +"_counts`"+" as "+queryString;
            logger.fine("create String : " + createString );
            st3.execute(createString);

            //adding  covering index May 21
            //create index string
            ResultSet rs5 = st2.executeQuery("select column_name as Entries from information_schema.columns where table_schema = '"+databaseName_CT+"' and table_name = '"+rchain.replace("`", "") +"_counts';");
            String IndexString = makeIndexQuery(rs5, "Entries", " , ");
            //logger.fine("Index String : " + IndexString);
            //logger.fine("alter table `"+rchain.replace("`", "") +"_counts`"+" add index `"+rchain.replace("`", "") +"_Index`   ( "+IndexString+" );");
            st3.execute("alter table `"+rchain.replace("`", "") +"_counts`"+" add index `"+rchain.replace("`", "") +"_Index`   ( "+IndexString+" );");

            String createString_CT = "create table `"+rchain.replace("`", "") +"_CT`"+" as "+queryString;
            logger.fine("create String : " + createString_CT );
            st3.execute(createString_CT);

            //adding  covering index May 21
            //create index string
            
            ResultSet rs5_CT = st2.executeQuery("select column_name as Entries from information_schema.columns where table_schema = '"+databaseName_CT+"' and table_name = '"+rchain.replace("`", "") +"_CT';");
            String IndexString_CT = makeIndexQuery(rs5_CT, "Entries", " , ");
            //logger.fine("Index String : " + IndexString);
            //logger.fine("alter table `"+rchain.replace("`", "") +"_counts`"+" add index `"+rchain.replace("`", "") +"_Index`   ( "+IndexString+" );");
            st3.execute("alter table `"+rchain.replace("`", "") +"_CT`"+" add index `"+rchain.replace("`", "") +"_Index`   ( "+IndexString_CT+" );");


            //  close statements
            st2.close();
            st3.close();


        }

        rs.close();
        st.close();
        logger.info("\n Rnodes_counts are DONE \n" );
    }

    /**
     * building the _flat tables
     */
    public static void BuildCT_Rnodes_flat(int len) throws SQLException, IOException {
        long l = System.currentTimeMillis(); //@zqian : measure structure learning time
        Statement st = con_BN.createStatement();
        ResultSet rs = st.executeQuery(
            "SELECT short_rnid AS short_RChain, orig_rnid AS RChain " +
            "FROM lattice_set " +
            "JOIN lattice_mapping " +
            "ON lattice_set.name = lattice_mapping.orig_rnid " +
            "WHERE lattice_set.length = " + len + ";"
        );
        while(rs.next()){

            // Get the short and full form rnids for further use.
            String rchain = rs.getString("RChain");
            logger.fine("\n RChain : " + rchain);
            String shortRchain = rs.getString("short_RChain");
            logger.fine(" Short RChain : " + shortRchain);

            //  create new statement
            Statement st2 = con_BN.createStatement();
            Statement st3 = con_CT.createStatement();


            //  create select query string
            ResultSet rs2 = st2.executeQuery("select distinct Entries from MetaQueries where Lattice_Point = '" + rchain + "' and TableType = 'Flat' and ClauseType = 'SELECT';");
            String selectString = makeCommaSepQuery(rs2, "Entries", " , ");
            //logger.fine("Select String : " + selectString);

            //  create from query string
            ResultSet rs3 = st2.executeQuery("select distinct Entries from MetaQueries where Lattice_Point = '" + rchain + "' and TableType = 'Flat' and ClauseType = 'FROM';" );
            String fromString = makeCommaSepQuery(rs3, "Entries", " , ");
            //logger.fine("From String : " + fromString);

            //  create the final query
            String queryString = "Select " + selectString + " from " + fromString ;

            //  create group by query string
            if (!cont.equals("1")) {
                ResultSet rs_6 = st2.executeQuery("select distinct Entries from MetaQueries where Lattice_Point = '" + rchain + "' and TableType = 'Flat' and ClauseType = 'GROUPBY';");
                String GroupByString = makeCommaSepQuery(rs_6, "Entries", " , ");
                //logger.fine("GroupBy String : " + GroupByString);

                if (!GroupByString.isEmpty()) queryString = queryString + " group by"  + GroupByString;
                logger.fine("Query String : " + queryString );
            }

            String createString = "CREATE TABLE `" + shortRchain.replace("`", "") + "_flat`" + " AS " + queryString;
            logger.fine("\n create String : " + createString );
            st3.execute(createString);

            //adding  covering index May 21
            //create index string
            ResultSet rs5 = st2.executeQuery(
                "SELECT column_name AS Entries " +
                "FROM information_schema.columns " +
                "WHERE table_schema = '" + databaseName_CT + "' " +
                "AND table_name = '" + shortRchain.replace("`", "") + "_flat';"
            );
            String IndexString = makeIndexQuery(rs5, "Entries", " , ");
            //logger.fine("Index String : " + IndexString);
            //logger.fine("alter table `"+rchain.replace("`", "") +"_flat`"+" add index `"+rchain.replace("`", "") +"_flat`   ( "+IndexString+" );");
            st3.execute(
                "ALTER TABLE `" + shortRchain.replace("`", "") + "_flat` " +
                "ADD INDEX `" + shortRchain.replace("`", "") + "_flat` (" + IndexString + ");"
            );


            //  close statements
            st2.close();
            st3.close();

        }

        rs.close();
        st.close();
        long l2 = System.currentTimeMillis(); //@zqian : measure structure learning time
        logger.info("Building Time(ms) for Rnodes_flat: "+(l2-l)+" ms.\n");
        logger.info("\n Rnodes_flat are DONE \n" );
    }

    /**
     * building the _star tables
     */
    public static void BuildCT_Rnodes_star(int len) throws SQLException, IOException {
        long l = System.currentTimeMillis(); //@zqian : measure structure learning time
        Statement st = con_BN.createStatement();
        ResultSet rs = st.executeQuery(
            "SELECT short_rnid AS short_RChain, orig_rnid AS RChain " +
            "FROM lattice_set " +
            "JOIN lattice_mapping " +
            "ON lattice_set.name = lattice_mapping.orig_rnid " +
            "WHERE lattice_set.length = " + len + ";"
        );
        while(rs.next()){

            // Get the short and full form rnids for further use.
            String rchain = rs.getString("RChain");
            logger.fine("\n RChain : " + rchain);
            String shortRchain = rs.getString("short_RChain");
            logger.fine(" Short RChain : " + shortRchain);

            //  create new statement
            Statement st2 = con_BN.createStatement();
            Statement st3 = con_CT.createStatement();

            //  create select query string
            
            ResultSet rs2 = st2.executeQuery("select distinct Entries from MetaQueries where Lattice_Point = '" + rchain + "' and TableType = 'Star' and ClauseType = 'SELECT';");
            String selectString = makeCommaSepQuery(rs2, "Entries", " , ");
            //logger.fine("Select String : " + selectString);


            //  create from MULT string
            ResultSet rs3 = st2.executeQuery("select distinct Entries from MetaQueries where Lattice_Point = '" + rchain + "' and TableType = 'Star' and ClauseType = 'FROM';");
            String MultString = makeStarSepQuery(rs3, "Entries", " * ");
            //makes the aggregate function to be used in the select clause //
            //logger.fine("Mult String : " + MultString+ " as `MULT`");
            // looks like rs3 and rs4 contain the same data. Ugly! OS August 24, 2017

            //  create from query string
            ResultSet rs4 = st2.executeQuery("select distinct Entries from MetaQueries where Lattice_Point = '" + rchain + "' and TableType = 'Star' and ClauseType = 'FROM';");
            String fromString = makeCommaSepQuery(rs4, "Entries", " , ");
            //logger.fine("From String : " + fromString);

            //  create the final query
            String queryString = "";
            if (!selectString.isEmpty()) {
                queryString = "Select " +  MultString+ " as `MULT` ,"+selectString + " from " + fromString ;
            } else {
                queryString = "Select " +  MultString+ " as `MULT`  from " + fromString ;

            }
            //logger.fine("Query String : " + queryString );

            String createString = "CREATE TABLE `" + shortRchain.replace("`", "") + "_star`" + " as " + queryString;
            logger.fine("\n create String : " + createString );
            st3.execute(createString);

            //adding  covering index May 21
            //create index string
            ResultSet rs5 = st2.executeQuery(
                "SELECT column_name AS Entries " +
                "FROM information_schema.columns " +
                "WHERE table_schema = '" + databaseName_CT + "' " +
                "AND table_name = '" + shortRchain.replace("`", "") + "_star';"
            );
            String IndexString = makeIndexQuery(rs5, "Entries", " , ");
            //logger.fine("Index String : " + IndexString);
            //logger.fine("alter table `"+rchain.replace("`", "") +"_star`"+" add index `"+rchain.replace("`", "") +"_star`   ( "+IndexString+" );");
            st3.execute(
                "ALTER TABLE `" + shortRchain.replace("`", "") + "_star` " +
                "ADD INDEX `" + shortRchain.replace("`", "") + "_star` (" + IndexString + ");"
            );


            //  close statements
            st2.close();
            st3.close();


        }

        rs.close();
        st.close();
        long l2 = System.currentTimeMillis(); //@zqian : measure structure learning time
        logger.info("Building Time(ms) for Rnodes_star: "+(l2-l)+" ms.\n");
        logger.info("\n Rnodes_star are DONE \n" );
    }

    /**
     * building the _false tables first and then the _CT tables
     */
    public static void BuildCT_Rnodes_CT(int len) throws SQLException, IOException {
        long l = System.currentTimeMillis(); //@zqian : measure structure learning time
        Statement st = con_BN.createStatement();
        ResultSet rs = st.executeQuery(
            "SELECT short_rnid AS short_RChain, orig_rnid AS RChain " +
            "FROM lattice_set " +
            "JOIN lattice_mapping " +
            "ON lattice_set.name = lattice_mapping.orig_rnid " +
            "WHERE lattice_set.length = " + len + ";"
        );

        while(rs.next()) {
            // Get the short and full form rnids for further use.
            String rchain = rs.getString("RChain");
            logger.fine("\n RChain : " + rchain);
            String shortRchain = rs.getString("short_RChain");
            logger.fine(" Short RChain : " + shortRchain);

            //  create new statement
            Statement st2 = con_BN.createStatement();
            Statement st3 = con_CT.createStatement();
            /**********starting to create _flase table***using sort_merge*******************************/
            //Sort_merge5.sort_merge("`"+rchain.replace("`", "")+"_star`","`"+rchain.replace("`", "") +"_flat`","`"+rchain.replace("`", "") +"_false`",con3);
            //Sort_merge4.sort_merge("`"+rchain.replace("`", "")+"_star`","`"+rchain.replace("`", "") +"_flat`","`"+rchain.replace("`", "") +"_false`",con3);
            Sort_merge3.sort_merge(
                "`" + shortRchain.replace("`", "") + "_star`",
                "`" + shortRchain.replace("`", "") + "_flat`",
                "`" + shortRchain.replace("`", "") + "_false`",
                con_CT
            );
            // computing false table as mult difference between star and false. Separate procedure for optimizing this big join.
            
            //adding  covering index May 21
            //create index string
            ResultSet rs15 = st2.executeQuery(
                "SELECT column_name AS Entries " +
                "FROM information_schema.columns " +
                "WHERE table_schema = '" + databaseName_CT + "' " +
                "AND table_name = '" + shortRchain.replace("`", "") + "_false';"
            );
            String IndexString = makeIndexQuery(rs15, "Entries", " , ");
            //logger.fine("Index String : " + IndexString);
            //  logger.fine("alter table `"+rchain.replace("`", "") +"_false`"+" add index `"+rchain.replace("`", "") +"_false`   ( "+IndexString+" );");
            st3.execute(
                "ALTER TABLE `" + shortRchain.replace("`", "") + "_false` " +
                "ADD INDEX `" + shortRchain.replace("`", "") +"_false` (" + IndexString + ");"
            );

            //building the _CT table        //expanding the columns // May 16
            // must specify the columns, or there's will a mistake in the table that mismatch the columns
            ResultSet rs5 = st3.executeQuery(
                "SELECT column_name AS Entries " +
                "FROM information_schema.columns " +
                "WHERE table_schema = '" + databaseName_CT + "' " +
                "AND table_name = '" + shortRchain.replace("`", "") + "_counts';"
            );
            // reading the column names from information_schema.columns, and the output will remove the "`" automatically,
            // however some columns contain "()" and MySQL does not support "()" well, so we have to add the "`" back.
            String UnionColumnString = makeUnionSepQuery(rs5, "Entries", " , ");
            //logger.fine("Union Column String : " + UnionColumnString);

            //join false table with join table to introduce rnid (=F) and 2nids (= n/a). Then union result with counts table.
            String createCTString = "CREATE TABLE `" + shortRchain.replace("`", "") + "_CT` AS SELECT " + UnionColumnString +
                " FROM `" + shortRchain.replace("`", "") + "_counts`" +
                " UNION" +
                " SELECT " + UnionColumnString +
                " FROM `" + shortRchain.replace("`", "") + "_false`, `" + shortRchain.replace("`", "") + "_join`;";
            logger.fine("\n create CT table String : " + createCTString );
            st3.execute(createCTString);

            //adding  covering index May 21
            //create index string
            ResultSet rs25 = st2.executeQuery(
                "SELECT column_name AS Entries " +
                "FROM information_schema.columns " +
                "WHERE table_schema = '" + databaseName_CT + "' " +
                "AND table_name = '" + shortRchain.replace("`", "") + "_CT';"
            );
            String IndexString2 = makeIndexQuery(rs25, "Entries", " , ");
            //logger.fine("Index String : " + IndexString2);
            //logger.fine("alter table `"+rchain.replace("`", "") +"_CT`"+" add index `"+rchain.replace("`", "") +"_CT`   ( "+IndexString2+" );");
            st3.execute(
                "ALTER TABLE `" + shortRchain.replace("`", "") + "_CT` " +
                "ADD INDEX `" + shortRchain.replace("`", "") + "_CT` (" + IndexString2 + ");"
            );

            //  close statements
            st2.close();
            st3.close();
        }

        rs.close();
        st.close();
        long l2 = System.currentTimeMillis(); //@zqian : measure structure learning time
        logger.info("Building Time(ms) for Rnodes_false and Rnodes_CT: "+(l2-l)+" ms.\n");
        logger.info("\n Rnodes_false and Rnodes_CT  are DONE \n" );
    }

    /**
     * preparing the _join part for _CT tables
     * @throws SQLException
     * @throws IOException
     */
    public static void BuildCT_Rnodes_join() throws SQLException, IOException {
        //set up the join tables that represent the case where a relationship is false and its attributes are undefined //

        Statement st = con_BN.createStatement();
        ResultSet rs = st.executeQuery("select orig_rnid, short_rnid from LatticeRNodes ;");

        while(rs.next()){
        //  get rnid
            String short_rnid = rs.getString("short_rnid");
            logger.fine("\n short_rnid : " + short_rnid);
            String orig_rnid = rs.getString("orig_rnid");
            logger.fine("\n orig_rnid : " + orig_rnid);

            Statement st2 = con_BN.createStatement();
            Statement st3 = con_CT.createStatement();

            //  create ColumnString
            ResultSet rs2 = st2.executeQuery(
                "SELECT DISTINCT Entries " +
                "FROM MetaQueries " +
                "WHERE Lattice_Point = '" + orig_rnid + "' " +
                "AND TableType = 'Join';"
            );

            String ColumnString = makeCommaSepQuery(rs2, "Entries", " , ");
            ColumnString = orig_rnid + " varchar(5) ," + ColumnString;
            //if there's no relational attribute, then should remove the "," in the ColumnString
            if (ColumnString.length() > 0 && ColumnString.charAt(ColumnString.length()-1)==',')
            {
                ColumnString = ColumnString.substring(0, ColumnString.length()-1);
            }
                //logger.fine("Column String : " + ColumnString);
            String createString = "create table `"+short_rnid.replace("`", "") +"_join` ("+ ColumnString +");";
                logger.fine("create String : " + createString);

            st3.execute(createString);
            st3.execute("INSERT INTO `"+short_rnid.replace("`","")+"_join` ( "+orig_rnid + ") values ('F');");
// should probably make this an insertion in the script: add a column for rnid with default value 'F' OS August 24, 2017
            st2.close();
            st3.close();

        }
        rs.close();
        st.close();
        logger.info("\n Rnodes_joins are DONE \n" );
    }

    /**
     * for _star  adding "`"
     * @param rs
     * @param colName
     * @param del
     * @return
     * @throws SQLException
     */
    public static String makeStarSepQuery(ResultSet rs, String colName, String del) throws SQLException {
        ArrayList<String> parts = new ArrayList<String>();

        while(rs.next()){
            //stringQuery += rs.getString(colName) + del;
            String temp=rs.getString(colName);
            String temp1=temp.replace("`", "");
            temp = "`"+temp1+"`.`MULT` ";
            parts.add(temp);

        }
        //stringQuery = stringQuery.substring(0, stringQuery.length() - del.length());

        return StringUtils.join(parts,del);
        //return stringQuery;
    }


    /**
     * for _CT part, adding "`"
     * @param rs
     * @param colName
     * @param del
     * @return
     * @throws SQLException
     */
    public static String makeUnionSepQuery(ResultSet rs, String colName, String del) throws SQLException {
    
        ArrayList<String> parts = new ArrayList<String>();

        while(rs.next()){
            parts.add("`"+rs.getString(colName)+"`");
        }
        return StringUtils.join(parts,del);
    }

    /**
     * separate the entries by ","
     * @param rs
     * @param colName
     * @param del
     * @return
     * @throws SQLException
     */
    public static String makeCommaSepQuery(ResultSet rs, String colName, String del) throws SQLException {
        
        ArrayList<String> parts = new ArrayList<String>();
        while(rs.next()){
            parts.add(rs.getString(colName));
        }
        return StringUtils.join(parts,del);
    }

    /**
     * for len>1, false table, part 1, where string
     * @param rs
     * @param colName
     * @param del
     * @param cur_flat_Table
     * @param cur_star_Table
     * @return
     * @throws SQLException
     */
    public static String makeFalseWhereSepQuery(ResultSet rs, String colName, String del, String cur_flat_Table,String cur_star_Table) throws SQLException {
        ArrayList<String> parts = new ArrayList<String>();

        while(rs.next()){
            //stringQuery += rs.getString(colName) + del;
            String temp=rs.getString(colName);
            String temp1=temp.replace("`", "");
            temp = "`"+cur_flat_Table.replace("`", "") +"`.`"+temp1+"` = "+ "`"+cur_star_Table.replace("`", "") +"`.`"+temp1+"`";
            parts.add(temp);

        }
        //stringQuery = stringQuery.substring(0, stringQuery.length() - del.length());

        return StringUtils.join(parts,del);
        //return stringQuery;
    }

    /**
     * Making Index Query by adding "`" and appending ASC
     * @param rs
     * @param colName
     * @param del
     * @return
     * @throws SQLException
     */
    public static String makeIndexQuery(ResultSet rs, String colName, String del) throws SQLException {

        ArrayList<String> parts = new ArrayList<String>();
        int count=0;
        //String stringQuery = "";
        while(rs.next()&count<16){

                String temp =rs.getString(colName);
                if (temp.equals("MULT")) {
                    continue;
                }
                temp= "`"+temp+"`";
            parts.add(temp+ " ASC");
            count ++;
        }

        return StringUtils.join(parts,del);
    }

    /**
     * Get columns from the metaData
     * @param rs
     * @return
     * @throws SQLException
     */
    public static ArrayList<String> getColumns(ResultSet rs) throws SQLException {
        ArrayList<String> cols = new ArrayList<String>();
        ResultSetMetaData metaData = rs.getMetaData();
        rs.next();

        int columnCount = metaData.getColumnCount();
        for (int i = 1; i <= columnCount; i++) {
            cols.add(metaData.getColumnLabel(i));
        }
        return cols;
    }

    /**
     * Disconnect all the databases
     * @throws SQLException
     */
    public static void disconnectDB() throws SQLException {
        con_std.close();
        con_BN.close();
        con_CT.close();
        con_setup.close();
    }
}