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
import java.sql.SQLException;
import java.util.regex.Pattern;

/**
 * Jun 25, zqian
 * This is a useful program which could be used to call the sql scripte from Java.
 */
public class MySQLScriptRunner {
    private String databaseName;
    private Connection con2;


    public MySQLScriptRunner(String databaseName, Connection con2) {
        this.databaseName = databaseName;
        this.con2= con2;
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


    public void callSP(String spName) throws SQLException {
        CallableStatement callableStatement = con2.prepareCall("CALL " + spName + ";");
        callableStatement.executeUpdate();
        callableStatement.close();
    }


    /**
     * Execute the given MySQL script using ";" as the command delimiter.
     *
     * @param scriptFileName - the path to the MySQL script to execute.
     * @throws SQLException if there is an issue executing the command(s).
     * @throws IOException if there is an issue reading from the script.
     */
    public void runScript(String scriptFileName) throws SQLException, IOException {
        this.runScript(scriptFileName, ";");
    }


    /**
     * Execute the given MySQL script using the specified {@code String} as the command delimiter.
     *
     * Note: This method is good for scripts that define the creation of things like stored procedures.
     *
     * @param scriptFileName - the path to the MySQL script to execute.
     * @param delimiter - the delimiter to use when reading the commands from the given script.
     * @throws SQLException if there is an issue executing the command(s).
     * @throws IOException if there is an issue reading from the script.
     */
    public void runScript(String scriptFileName, String delimiter) throws SQLException, IOException {
        String newScriptFileName = prepareFile(scriptFileName);

        ScriptRunner runner = new ScriptRunner(con2);
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