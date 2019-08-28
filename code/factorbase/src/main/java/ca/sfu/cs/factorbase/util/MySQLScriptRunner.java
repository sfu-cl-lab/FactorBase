package ca.sfu.cs.factorbase.util;

import com.ibatis.common.jdbc.ScriptRunner;

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
import java.sql.Statement;
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
            if (!newFile.delete())
                throw new FileNotFoundException("Failed to delete file: " + newFile);
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
}