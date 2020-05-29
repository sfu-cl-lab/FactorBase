package ca.sfu.cs.factorbase.util;

import org.apache.ibatis.jdbc.ScriptRunner;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.regex.Pattern;

/**
 * Class to help make any necessary database substitutions for template MySQL scripts and execute them on a database.
 */
public class MySQLScriptRunner {
    /**
     * Private constructor to prevent instantiation of the utility class.
     */
    private MySQLScriptRunner() {
    }


    /**
     * Parse through the given template file and create a copy of it with the "@database@"s replaced with the given
     * database name.
     *
     * @param fileName - the file to create a copy of with the variables filled in.
     * @param databaseName - the name of the database to replace instances of "@database@" with.
     * @throws IOException if there is an issue reading from the script.
     */
    private static String prepareFile(String fileName, String databaseName) throws IOException {
        InputStream inputStream = MySQLScriptRunner.class.getClassLoader().getResourceAsStream(fileName);
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
            if (!newFile.delete()) {
                throw new FileNotFoundException("Failed to delete file: " + newFile);
            }
        } catch (Exception e) {
        }

        RandomAccessFile output = new RandomAccessFile(newFileName, "rw");

        String line = input.readLine();
        String finalOutput = "";
        while (line != null) {
            line = line.replace("@database@", databaseName);
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


    /**
     * Execute the stored procedure with the given name.
     *
     * @param dbConnection - connection to the database to call the stored procedure on.
     * @param spName - the name of the stored procedure to call.
     * @throws SQLException if there is an issue executing the stored procedure.
     */
    public static void callSP(Connection dbConnection, String spName) throws SQLException {
        CallableStatement callableStatement = dbConnection.prepareCall("CALL " + spName + ";");
        callableStatement.executeUpdate();
        callableStatement.close();
    }


    /**
     * Execute the stored procedure with the given name and CSV passed to it.
     *
     * @param dbConnection - connection to the database to call the stored procedure on.
     * @param spName - the name of the stored procedure to call that returns results.
     * @param csv - a comma separated list of parameters to pass to the called stored procedure.
     * @return the results of calling the stored procedure.
     * @throws SQLException if there is an issue executing the stored procedure.
     */
    public static ResultSet callSP(Connection dbConnection, String spName, String csv) throws SQLException {
        CallableStatement callableStatement = dbConnection.prepareCall("CALL " + spName + "('" + csv + "');");
        return callableStatement.executeQuery();
    }


    /**
     * Execute the given MySQL script using ";" as the command delimiter.
     *
     * @param dbConnection - connection to the database to execute the script on.
     * @param scriptFileName - the path to the MySQL script to execute.
     * @param databaseName - the name of the database to replace instances of "@database@" with.
     * @throws SQLException if there is an issue executing the command(s).
     * @throws IOException if there is an issue reading from the script.
     */
    public static void runScript(Connection dbConnection, String scriptFileName, String databaseName) throws SQLException, IOException {
        runScript(dbConnection, scriptFileName, databaseName, ";");
    }


    /**
     * Execute the given MySQL script using the specified {@code String} as the command delimiter.
     *
     * Note: This method is good for scripts that define the creation of things like stored procedures.
     *
     * @param dbConnection - connection to the database to execute the script on.
     * @param scriptFileName - the path to the MySQL script to execute.
     * @param databaseName - the name of the database to replace instances of "@database@" with.
     * @param delimiter - the delimiter to use when reading the commands from the given script.
     * @throws SQLException if there is an issue executing the command(s).
     * @throws IOException if there is an issue reading from the script.
     */
    public static void runScript(Connection dbConnection, String scriptFileName, String databaseName, String delimiter) throws SQLException, IOException {
        String newScriptFileName = prepareFile(scriptFileName, databaseName);

        ScriptRunner runner = new ScriptRunner(dbConnection);
        try (
            FileReader newScriptFileReader = new FileReader(newScriptFileName);
            BufferedReader newBuf = new BufferedReader(newScriptFileReader)
        ) {
            runner.setLogWriter(null);
            runner.setAutoCommit(true);
            runner.setDelimiter(delimiter);
            runner.runScript(newBuf);
        }
    }
}