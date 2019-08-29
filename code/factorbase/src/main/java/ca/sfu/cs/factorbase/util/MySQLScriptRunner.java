package ca.sfu.cs.factorbase.util;

import com.ibatis.common.jdbc.ScriptRunner;

import java.io.*;
import java.sql.*;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Jun 25, zqian
 * This is a useful program which could be used to call the sql scripte from Java.
 **/
public class MySQLScriptRunner {
    private String databaseName;
    private String dbbase;
    private String largestRchain;
    private Connection con2;
    
	private static Logger logger = Logger.getLogger(MySQLScriptRunner.class.getName());

    public MySQLScriptRunner(String databaseName, Connection con2) {
        this.databaseName = databaseName;
        this.con2= con2;
    }

    public MySQLScriptRunner(String databaseName, String dbbase, Connection con2) {
        this.databaseName = databaseName;
        this.dbbase = dbbase;
        this.con2= con2;
    }

    public MySQLScriptRunner(String databaseName, Connection con2, String largestRchain) {
        this.databaseName = databaseName;
        this.con2 = con2;
        this.largestRchain = largestRchain;
    }

    private String prepareFile(String fileName) throws IOException, SQLException {
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(fileName);
        if (inputStream == null) {
            throw new FileNotFoundException("Unable to read the file: " + fileName);
        }
        InputStreamReader streamReader = new InputStreamReader(inputStream);
        BufferedReader input = new BufferedReader(streamReader);

        File file = new File(fileName);
        String newFileName = file.getParent() + File.separator + "_" + file.getName();
        File newFile = new File(newFileName);
        newFile.mkdirs();
        try {
            if (!newFile.delete())
                throw new FileNotFoundException("Failed to delete file: " + newFile);
        } catch (Exception e) {
        }

        RandomAccessFile output = new RandomAccessFile(newFileName, "rw");

        String line = input.readLine();
        String finalOutput = "";
        while (line != null) {
            line = line.replace("@database@", databaseName);
            if (dbbase != null) {
                line = line.replace("@databasebase@", dbbase);
            }
            if (largestRchain != null) {
                line = line.replace("@largestrchain@", largestRchain);
            }
            finalOutput += line + System.getProperty("line.separator");
            line = input.readLine();
        }

        Pattern commentPattern = Pattern.compile("/\\*.*?\\*/", Pattern.DOTALL);
        finalOutput = commentPattern.matcher(finalOutput).replaceAll("");
        output.writeBytes(finalOutput);

        output.close();
        input.close();

        return newFileName;
    }

    public void callSP(String spName) throws SQLException {
        CallableStatement callableStatement = con2.prepareCall("CALL " + spName + ";");
        callableStatement.executeUpdate();
        callableStatement.close();
    }

    public void createSP(String scriptFileName) throws IOException, SQLException {
        String newScriptFileName = prepareFile(scriptFileName);

       // con2 = connectDB();
        RandomAccessFile spInput = new RandomAccessFile(newScriptFileName, "r");
        String line = "";
        String sp = "";
        while (line != null) {
            sp += line + "\n";
            line = spInput.readLine();
        }

        Statement stmt = con2.createStatement();
        stmt.execute(sp);
        stmt.close();

        spInput.close();
    }

    /*specified method for CPGenerator*/
    public void CP_createSP(String scriptFileName) throws IOException, SQLException {
        String newScriptFileName = CP_prepareFile(scriptFileName);
        RandomAccessFile spInput = new RandomAccessFile(newScriptFileName, "r");
        String line = "";
        String sp = "";
        while (line != null) {
            sp += line + "\n";
            line = spInput.readLine();
        }

        Statement stmt = con2.createStatement();
        stmt.execute(sp);
        stmt.close();

        spInput.close();
    }

    /*specified method for CPGenerator*/
    private String CP_prepareFile(String fileName) throws IOException, SQLException {
        String Rchain="";
        Statement st = con2.createStatement();
        ResultSet rs = st.executeQuery(
            "SELECT name AS RChain " +
            "FROM lattice_set " +
            "WHERE lattice_set.length = (" +
                "SELECT MAX(length) " +
                "FROM lattice_set" +
            ");"
        );
        while (rs.next()) {
            Rchain = rs.getString("RChain");
            //logger.fine("\n Longest  RChain : " + Rchain);
        }
        rs.close();
        st.close();

        File file = new File(fileName);
        String newFileName = file.getParent() + File.separator + "_" + file.getName();
        File newFile = new File(newFileName);
        try {
            if (!newFile.delete()) {
                throw new FileNotFoundException("Failed to delete file: " + newFile);
            }
        } catch (Exception e) {
        }

        RandomAccessFile input = new RandomAccessFile(fileName, "r");
        RandomAccessFile output = new RandomAccessFile(newFileName, "rw");

        String line = input.readLine();
        String finalOutput = "";
        while (line != null) {
            line = line.replace("@database@", databaseName);
            line = line.replace("@RChain@", Rchain); //input the biggest rchain
            line = line.replace("@BNDatabaseName@", databaseName + "_BN");
            line = line.replace("@CTdatabase@", databaseName + "_CT");
            finalOutput += line + System.getProperty("line.separator");
            line = input.readLine();
        }

        Pattern commentPattern = Pattern.compile("/\\*.*?\\*/", Pattern.DOTALL);
        finalOutput = commentPattern.matcher(finalOutput).replaceAll("");
        output.writeBytes(finalOutput);

        output.close();
        input.close();

        return newFileName;
    }

    public void runScript(String scriptFileName) throws SQLException, IOException {
        //con2 = connectDB();
        String newScriptFileName = prepareFile(scriptFileName);

        ScriptRunner runner = new ScriptRunner(con2, false, false);
        //System.out.print("\n within runScript \n");
        // System.out.print(newScriptFileName);
        FileReader newScriptFileReader = new FileReader(newScriptFileName);
        BufferedReader newBuf = new BufferedReader(newScriptFileReader);
        runner.setLogWriter(null);
        runner.runScript(newBuf);
        //System.out.print("\n after runner.runScript \n");
        newBuf.close();
        newScriptFileReader.close();
    }

    public static int runScript(String scriptFileName, String setup, String work, String data, Connection con) {
        logger.fine("Executing script: " + scriptFileName);
        String newScriptFileName = prepareFile(scriptFileName, setup, work, data);

        if (newScriptFileName == null) {
            logger.severe("Failed to execute script: " + scriptFileName);
            return -1;
        }
        ScriptRunner runner = new ScriptRunner(con, false, false);
        FileReader newScriptFileReader = null;
        try {
            newScriptFileReader = new FileReader(newScriptFileName);
        } catch (FileNotFoundException e) {
            logger.severe("Failed to open script: " + newScriptFileName);
            e.printStackTrace();
            return -1;
        }

        BufferedReader newBuf = new BufferedReader(newScriptFileReader);
        try {
            runner.runScript(newBuf);
        } catch (IOException e) {
            logger.severe("IO error while running script: " + newScriptFileName);
            e.printStackTrace();
            return -2;
        } catch (SQLException e) {
            logger.severe("SQL error while running script: " + newScriptFileName);
            e.printStackTrace();
            return -3;
        }

        try {
            newBuf.close();
            newScriptFileReader.close();
        } catch (IOException e) {
            logger.severe("Failed to close files.");
            e.printStackTrace();
            return -1;
        }

        logger.fine("Finished executing script.");

        return 0;
    }

    private static String prepareFile(String fileName, String setup, String work, String data) {
        File file = new File(fileName);
        String newFileName = file.getParent() + File.separator + "_" + file.getName();
        File newFile = new File(newFileName);

        try {
            if (!newFile.delete()) {
                throw new FileNotFoundException("Failed to delete file: " + newFile);
            }
        } catch (Exception e) {
            //Do nothing
        }

        RandomAccessFile input = null;

        try {
            input = new RandomAccessFile(fileName, "r");
        } catch (FileNotFoundException e) {
            logger.severe("Failed to find file " + fileName);
            e.printStackTrace();
            return null;
        }

        RandomAccessFile output = null;

        try {
            output = new RandomAccessFile(newFileName, "rw");
        } catch (FileNotFoundException e) {
            logger.severe("Failed to find file " + newFileName);
            e.printStackTrace();

            // Attempt to close the input file if we have problems with the output file.
            try {
                input.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }

            return null;
        }

        String line = null;
        try {
            line = input.readLine();
            String finalOutput = "";

            while (line != null) {
                line = line.replace("@database_work@", work);
                line = line.replace("@database_setup@", setup);
                line = line.replace("@database_data@", data);
                finalOutput += line + System.getProperty("line.separator");
                line = input.readLine();
            }

            Pattern commentPattern = Pattern.compile("/\\*.*?\\*/", Pattern.DOTALL);
            finalOutput = commentPattern.matcher(finalOutput).replaceAll("");
            output.writeBytes(finalOutput);

            output.close();
            input.close();
        } catch (IOException e) {
            logger.severe("Failed to read file " + fileName);
            e.printStackTrace();
            return null;
        }

        return newFileName;
    }
}